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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.connector.SVNConnectorException;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.PreparedBranchTagOperation;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
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
		IRepositoryResource []resources = this.getSelectedRepositoryResources();
		PreparedBranchTagOperation op = BranchTagAction.getBranchTagOperation(resources, this.getShell(), this.nodeType);

		if (op != null) {
			CompositeOperation composite = new CompositeOperation(op.getId());
			composite.add(op);
			composite.add(new RefreshRemoteResourcesOperation(new IRepositoryResource[] {op.getDestination().getParent()}), new IActionOperation[] {op});
			this.runScheduled(composite);
		}
	}

	public static PreparedBranchTagOperation getBranchTagOperation(IRepositoryResource []resources, Shell shell, int nodeType) {
		if (!OperationErrorDialog.isAcceptableAtOnce(resources, nodeType == BranchTagAction.BRANCH_ACTION ? SVNUIMessages.BranchTagAction_Error_Branch : SVNUIMessages.BranchTagAction_Error_Tag, shell)) {
			return null;
		}
		
		resources = SVNUtility.shrinkChildNodes(resources);
		boolean isStructureEnabled = resources[0].getRepositoryLocation().isStructureEnabled()&& SVNTeamPreferences.getRepositoryBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.BRANCH_TAG_CONSIDER_STRUCTURE_NAME);
		// no structure -> copy content to destination
		// structure detection disabled -> copy selection content to destination
		// consider structure disabled -> copy selection content to destination
		
		// single-project layout:trunk selected -> copy selection content to destination
		// single-project layout:child of trunk selected -> copy selection content to destination
	
		// multiple-project layout:trunk selected -> copy selection content to destination
		// multiple-project layout:trunk children selected -> copy selection to destination
		// multiple-project layout:one child of trunk selected -> copy selection to destination (forceCreate)
		
		// tagging from branch and branching from tag uses the same rules, i.e. respects a project layout.
		Set<String> nodeNames = Collections.emptySet();
		boolean forceCreate = false;
		if (isStructureEnabled) {
			// allows to use project root for branching and tagging of the whole project
			int kind = ((IRepositoryRoot)resources[0].getRoot()).getKind();
			if (kind == IRepositoryRoot.KIND_LOCATION_ROOT || kind == IRepositoryRoot.KIND_ROOT) {
				try {
					IRepositoryContainer supposedTrunk = resources[0].asRepositoryContainer(resources[0].getRepositoryLocation().getTrunkLocation(), false);
					if (supposedTrunk.exists()) {
						resources[0] = supposedTrunk;
					}
				}
				catch (SVNConnectorException ex) {
					// do nothing
				}
			}
			nodeNames = BranchTagAction.getExistingNodeNames(nodeType == BranchTagAction.BRANCH_ACTION ? SVNUtility.getBranchesLocation(resources[0]) : SVNUtility.getTagsLocation(resources[0]));
			forceCreate = 
				resources.length == 1 && 
				!(resources[0] instanceof IRepositoryRoot && ((IRepositoryRoot)resources[0]).getKind() == IRepositoryRoot.KIND_TRUNK) &&  
				!BranchTagAction.isSingleProjectLayout(resources[0]) && 
				BranchTagAction.isProjectFileExists(resources[0]);
		}
		
		AbstractBranchTagPanel panel = nodeType == BranchTagAction.BRANCH_ACTION ? (AbstractBranchTagPanel)new BranchPanel(SVNUtility.getBranchesLocation(resources[0]), false, nodeNames, resources) : new TagPanel(SVNUtility.getTagsLocation(resources[0]), false, nodeNames, resources);
		DefaultDialog dialog = new DefaultDialog(shell, panel);
		if (dialog.open() == 0) {
			IRepositoryResource[] resourcesWithSpecifiedRevision = new IRepositoryResource[resources.length];
			for (int i = 0; i < resources.length; i ++) {
				resourcesWithSpecifiedRevision[i] = SVNUtility.copyOf(resources[i]);				
				resourcesWithSpecifiedRevision[i].setSelectedRevision(panel.getRevisionForRemoteResources());
				resourcesWithSpecifiedRevision[i].setPegRevision(resources[i].getPegRevision());
			}
			return new PreparedBranchTagOperation(nodeType == BranchTagAction.BRANCH_ACTION ? "Branch" : "Tag", resourcesWithSpecifiedRevision, panel.getDestination(), panel.getMessage(), forceCreate);
		} else {
			return null;
		}
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

	protected static IRepositoryResource []getRemoteChildren(final IRepositoryContainer parent) {
		GetRemoteFolderChildrenOperation op = new GetRemoteFolderChildrenOperation(parent, false);
		UIMonitorUtility.doTaskBusy(op, new DefaultOperationWrapperFactory() {
			public IActionOperation getLogged(IActionOperation operation) {
				return operation;
			}
		});
		return op.getChildren();
	}
	
	public static boolean isSingleProjectLayout(IRepositoryResource resource) {
		return BranchTagAction.isProjectFileExists(SVNUtility.getTrunkLocation(resource));
	}
	
	protected static boolean isProjectFileExists(IRepositoryResource resource) {
		try {
			return resource.asRepositoryFile(".project", false).exists(); //$NON-NLS-1$
		}
		catch (SVNConnectorException ex) {
			return false;
		}
	}
	
}
