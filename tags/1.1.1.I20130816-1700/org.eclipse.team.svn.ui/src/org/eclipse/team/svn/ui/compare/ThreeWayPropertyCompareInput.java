/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.property.RemovePropertiesOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.action.local.SetPropertyAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.properties.ResourcePropertyEditPanel;
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
				SVNRemoteStorage.instance().asLocalResource(left).getPropStatus() != IStateFilter.ST_CONFLICTING ? right : null,
				ancestor,
				location);
		this.baseRevisionNumber = baseRevisionNumber;
		this.leftResource = left;
	}

	protected void fillMenu(IMenuManager manager, TreeSelection selection) {
		final PropertyCompareNode selectedNode = (PropertyCompareNode)selection.getFirstElement();
		Action tAction = null;
		manager.add(tAction = new Action(SVNUIMessages.SetPropertyAction_label) {		
			public void run() {
				IResource [] resources = new IResource[] {ThreeWayPropertyCompareInput.this.leftResource};
				ResourcePropertyEditPanel panel = new ResourcePropertyEditPanel(null, resources, false);
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
		
		manager.add(tAction = new Action(SVNUIMessages.RemovePropertyAction_label) {		
			public void run() {
				ThreeWayPropertyCompareInput.this.removeProperty(selectedNode);
			}
		});
		tAction.setEnabled(((PropertyElement)selectedNode.getLeft()).getValue() != null);
	}
	
	protected void removeProperty(PropertyCompareNode currentNode){	
		//perform property removing
		final String propName = currentNode.getName();
		IActionOperation op = new RemovePropertiesOperation(new IResource[] {this.leftResource}, new SVNProperty[] {new SVNProperty(propName, "")}, false);
		CompositeOperation cmpOp = new CompositeOperation(op.getId(), op.getMessagesClass());
		cmpOp.add(op);
		cmpOp.add(new RefreshResourcesOperation(new IResource[] {this.leftResource}, IResource.DEPTH_ZERO, RefreshResourcesOperation.REFRESH_CHANGES));
		UIMonitorUtility.doTaskNowDefault(cmpOp, false);
		
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
		return SVNUIMessages.format(SVNUIMessages.PropertyCompareInput_Title3,
													  new String []	{
													  this.left.path.substring(this.left.path.lastIndexOf("/")+1) //$NON-NLS-1$
													  + " [" + this.getRevisionPart(this.left), //$NON-NLS-1$
													  this.getRevisionPart(this.ancestor),
													  this.getRevisionPart(this.right)+ "] " //$NON-NLS-1$
													  });
	}
	
	protected String getRevisionPart(SVNEntryRevisionReference reference) {
		if (reference == null) {
			return SVNUIMessages.ResourceCompareInput_PrejFile;
		}
		if (reference.revision == SVNRevision.WORKING) {
			return SVNUIMessages.ResourceCompareInput_LocalSign;
		}
		else if (reference.revision == SVNRevision.BASE) {
			if (this.ancestor == null) {
				return SVNUIMessages.ResourceCompareInput_ResourceIsNotAvailable;
			}
			return SVNUIMessages.format(SVNUIMessages.ResourceCompareInput_BaseSign, new String [] {String.valueOf(this.baseRevisionNumber)});
		}
		return SVNUIMessages.format(SVNUIMessages.ResourceCompareInput_RevisionSign, new String [] {String.valueOf(reference.revision)});
	}

}
