/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.action.remote;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.connector.SVNConnectorException;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.operation.remote.PreparedBranchTagOperation;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.dialog.OperationErrorDialog;
import org.eclipse.team.svn.ui.operation.GetRemoteFolderChildrenOperation;
import org.eclipse.team.svn.ui.operation.RefreshRemoteResourcesOperation;
import org.eclipse.team.svn.ui.panel.common.AbstractBranchTagPanel;
import org.eclipse.team.svn.ui.panel.common.BranchPanel;
import org.eclipse.team.svn.ui.panel.common.TagPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.DefaultOperationWrapperFactory;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Branch or tag action on repository
 * 
 * @author Sergiy Logvin
 */
public class BranchTagAction extends AbstractRepositoryTeamAction {
	public static final int BRANCH_ACTION = 0;
	public static final int TAG_ACTION = 1;
	
	protected int nodeType;
	
	public BranchTagAction(int nodeType) {
		super();
		this.nodeType = nodeType;
	}
	
	public void runImpl(IAction action) {
		boolean respectProjectStructure = SVNTeamPreferences.getRepositoryBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.BRANCH_TAG_CONSIDER_STRUCTURE_NAME);
		
		IRepositoryResource []resources = this.getSelectedRepositoryResources();
		PreparedBranchTagOperation op = BranchTagAction.getBranchTagOperation(resources, this.getShell(), this.nodeType, respectProjectStructure);

		if (op != null) {
			CompositeOperation composite = new CompositeOperation(op.getId());
			composite.add(op);
			RefreshRemoteResourcesOperation refreshOp = new RefreshRemoteResourcesOperation(new IRepositoryResource[] {op.getDestination().getParent()});
			composite.add(refreshOp, new IActionOperation[] {op});
			this.runScheduled(composite);
		}
	}

	/*
	 * Processes resources which are selected to be branched/tagged
	 * If structure detection is enabled for the current location:
	 * - replaces project root with its trunk;
	 * - returns true if the selected resource is located in the trunk and the location layout is not single-project
	 * - returns false if only content of the selected resources should be branched/tagged
	 */
	public static boolean replaceProjectRootsWithTrunk(final IRepositoryResource resources[]) {
		IRepositoryLocation location = resources[0].getRepositoryLocation();
		if (location.isStructureEnabled() && resources[0] instanceof IRepositoryContainer) {
			IRepositoryResource parent = resources[0].getParent();
			if (parent instanceof IRepositoryRoot && ((IRepositoryRoot)parent).getKind() == IRepositoryRoot.KIND_TRUNK) {
				return !BranchTagAction.isSingleProjectLayout((IRepositoryRoot)parent);
			}
			IRepositoryResource []children = BranchTagAction.getRemoteChildren((IRepositoryContainer)resources[0]);
			if (children != null) {
				for (int i = 0; i < children.length; i++) {
					if (children[i] instanceof IRepositoryRoot && 
						((IRepositoryRoot)children[i]).getKind() == IRepositoryRoot.KIND_TRUNK) {
						resources[0] = children[i];
						break;
					}
				}
			}
		}
		return false;
	}
	
	public static Set<String> getExistingNodeNames(IRepositoryContainer parent) {
		HashSet<String> nodeNames = new HashSet<String>();
		IRepositoryResource []existentNodes = BranchTagAction.getRemoteChildren(parent);
		if (existentNodes != null) {
			for (int i = 0; i < existentNodes.length; i++) {
				nodeNames.add(existentNodes[i].getName());
			}
		}
		return nodeNames;
	}
	
	public static PreparedBranchTagOperation getBranchTagOperation(IRepositoryResource []resources, Shell shell, int nodeType, boolean respectProjectStructure) {
		boolean multipleProjLayout = BranchTagAction.replaceProjectRootsWithTrunk(resources);
		if (!OperationErrorDialog.isAcceptableAtOnce(resources, SVNTeamUIPlugin.instance().getResource(nodeType == BranchTagAction.BRANCH_ACTION ? "BranchTagAction.Error.Branch" : "BranchTagAction.Error.Tag"), shell)) {
			return null;
		}
		Set<String> nodeNames = null;
		if (respectProjectStructure && resources[0].getRepositoryLocation().isStructureEnabled()) {
			nodeNames = BranchTagAction.getExistingNodeNames(nodeType == BranchTagAction.BRANCH_ACTION ? SVNUtility.getBranchesLocation(resources[0]) : SVNUtility.getTagsLocation(resources[0]));
		}
		else {
			nodeNames = new HashSet<String>();
		}
		
		AbstractBranchTagPanel panel = nodeType == BranchTagAction.BRANCH_ACTION ? (AbstractBranchTagPanel)new BranchPanel(SVNUtility.getBranchesLocation(resources[0]), false, nodeNames) : new TagPanel(SVNUtility.getTagsLocation(resources[0]), false, nodeNames);
		DefaultDialog dialog = new DefaultDialog(shell, panel);
		if (dialog.open() == 0) {
			IRepositoryResource destination = panel.getDestination();
			return new PreparedBranchTagOperation(nodeType == BranchTagAction.BRANCH_ACTION ? "Branch" : "Tag", resources, destination, panel.getMessage(), multipleProjLayout);
		}
		
		return null;
	}
	
	protected static boolean isSingleProjectLayout(IRepositoryRoot trunk) {
		try {
			return trunk.asRepositoryFile(trunk.getUrl() + "/.project", false).exists();
		}
		catch (SVNConnectorException ex) {
			return false;
		}
	}
	
	protected static IRepositoryResource []getRemoteChildren(final IRepositoryContainer parent) {
		GetRemoteFolderChildrenOperation op = new GetRemoteFolderChildrenOperation(parent, false);
		UIMonitorUtility.doTaskBusy(op, new DefaultOperationWrapperFactory() {
			public IActionOperation getLogged(IActionOperation operation) {
				return new LoggedOperation(operation);
			}
		});
		return op.getChildren();
	}
	
	public boolean isEnabled() {
		IRepositoryResource []resources = this.getSelectedRepositoryResources();
		if (resources.length == 0) {
			return false;
		}
		// disable branching/tagging resources from different repositories
		IRepositoryLocation first = resources[0].getRepositoryLocation();
		for (int i = 1; i < resources.length; i++) {
			if (first != resources[i].getRepositoryLocation()) {
				return false;
			}
		}
		return true;
	}

}
