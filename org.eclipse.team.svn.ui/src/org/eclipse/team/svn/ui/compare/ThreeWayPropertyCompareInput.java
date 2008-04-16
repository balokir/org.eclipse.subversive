/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.compare;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.local.SetPropertyAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.view.property.PropertyEditPanel;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Compare input for comparison of local and remote SVN properties.
 * 
 * @author Alexei Goncharov
 */
public class ThreeWayPropertyCompareInput extends PropertyCompareInput {
	
	protected long baseRevisionNumber;
	protected IResource leftResource;

	public ThreeWayPropertyCompareInput(CompareConfiguration configuration,
										IResource left,
										SVNEntryRevisionReference right,
										SVNEntryRevisionReference ancestor,
										IRepositoryLocation location,
										long baseRevisionNumber) {
		super(configuration,
				new SVNEntryRevisionReference(FileUtility.getWorkingCopyPath(left), null, SVNRevision.WORKING),
				right,
				ancestor,
				location);
		this.baseRevisionNumber = baseRevisionNumber;
		this.leftResource = left;
	}

	protected void fillMenu(IMenuManager manager, TreeSelection selection) {
		final PropertyCompareNode selectedNode = (PropertyCompareNode)selection.getFirstElement();
		Action tAction = null;
		manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("SetPropertyAction.label")) {		
			public void run() {
				IResource [] resources = new IResource[] {ThreeWayPropertyCompareInput.this.leftResource};
				PropertyEditPanel panel = new PropertyEditPanel(null, resources, false);
				DefaultDialog dialog = new DefaultDialog(UIMonitorUtility.getShell(), panel);
				if (dialog.open() == Dialog.OK) {
					SetPropertyAction.doSetProperty(resources, panel, null);
					boolean notContained = true;
					RootCompareNode root = (RootCompareNode)ThreeWayPropertyCompareInput.this.getCompareResult();
					IDiffElement [] nodes = root.getChildren();
					for (int i = 0; i < nodes.length; i++) {
						PropertyCompareNode current = (PropertyCompareNode)nodes[i];
						if (current.getName().equals(panel.getPropertyName())) {
							notContained = false;
							((PropertyElement)current.getLeft()).setValue(panel.getPropertyValue());
							current.setKind(ThreeWayPropertyCompareInput.this.calculateDifference(
									panel.getPropertyValue(),
									((PropertyElement)current.getRight()).getValue(),
									((PropertyElement)current.getAncestor()).getValue()));
							current.fireChange();
						}
					}
					if (notContained) {
						new PropertyCompareNode(
								root,
								ThreeWayPropertyCompareInput.this.calculateDifference(panel.getPropertyValue(), null, null),
								new PropertyElement(panel.getPropertyName(), null, false),
								new PropertyElement(panel.getPropertyName(), panel.getPropertyValue(), true),
								new PropertyElement(panel.getPropertyName(), null, false));
					}
					ThreeWayPropertyCompareInput.this.viewer.refresh();
				}
			}
		});
		tAction.setEnabled(true);
		
		manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("RemovePropertyAction.label")) {		
			public void run() {
				ThreeWayPropertyCompareInput.this.removeProperty(selectedNode);
			}
		});
		tAction.setEnabled(((PropertyElement)selectedNode.getLeft()).getValue() != null);
	}
	
	protected void removeProperty(PropertyCompareNode currentNode){	
		//perform property removing
		final String propName = currentNode.getName();
		IActionOperation op = null;
		UIMonitorUtility.doTaskNowDefault(op = new AbstractActionOperation("Operation.RemoveProperties") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				final ISVNConnector proxy = ThreeWayPropertyCompareInput.this.location.acquireSVNProxy();
				try {
					proxy.removeProperty(ThreeWayPropertyCompareInput.this.left.path, propName, Depth.EMPTY, null, new SVNProgressMonitor(this, monitor, null));
				}
				finally {
					ThreeWayPropertyCompareInput.this.location.releaseSVNProxy(proxy);
				}
			}
		}, false);
		
		if (op.getExecutionState() != IActionOperation.OK) {
			return;
		}
		//refresh UI
		if ((currentNode.getKind() & Differencer.CHANGE_TYPE_MASK) == Differencer.ADDITION && (currentNode.getKind() & Differencer.DIRECTION_MASK) == Differencer.LEFT) {
			((RootCompareNode)this.getCompareResult()).remove(currentNode);
		}
		else {
			((PropertyElement)currentNode.getLeft()).setValue(null);
			currentNode.setKind(this.calculateDifference(null, ((PropertyElement)currentNode.getRight()).getValue(), ((PropertyElement)currentNode.getAncestor()).getValue()));
		}
		currentNode.fireChange();
		this.viewer.refresh();
	}
	
	public String getTitle() {
		return SVNTeamUIPlugin.instance().getResource("PropertyCompareInput.Title3",
													  new String []	{
													  this.left.path.substring(this.left.path.lastIndexOf("/")+1)
													  + " [" + this.getRevisionPart(this.left),
													  this.getRevisionPart(this.ancestor),
													  this.getRevisionPart(this.right)+ "] "
													  });
	}
	
	protected String getRevisionPart(SVNEntryRevisionReference reference) {
		if (reference.revision == SVNRevision.WORKING) {
			return SVNTeamUIPlugin.instance().getResource("ResourceCompareInput.LocalSign");
		}
		else if (reference.revision == SVNRevision.BASE) {
			if (this.ancestor == null) {
				return SVNTeamUIPlugin.instance().getResource("ResourceCompareInput.ResourceIsNotAvailable");
			}
			return SVNTeamUIPlugin.instance().getResource("ResourceCompareInput.BaseSign", new String [] {String.valueOf(this.baseRevisionNumber)});
		}
		return SVNTeamUIPlugin.instance().getResource("ResourceCompareInput.RevisionSign", new String [] {String.valueOf(reference.revision)});
	}

}
