/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.revision.graph.graphic.actions;

import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;
import org.eclipse.team.svn.revision.graph.graphic.editpart.RevisionEditPart;
import org.eclipse.ui.IWorkbenchPart;

/**
 * TODO correctly implement
 *  
 * @author Igor Burilo
 */
public class CollapseExpandAction extends BaseRevisionGraphAction {

	public final static String CollapseNextAction_ID = "CollapseNext";
	public final static String CollapsePreviousAction_ID = "CollapsePrevious";
	public final static String CollapseCopiedToAction_ID = "CollapseCopiedTo";
	public final static String CollapseCopiedFromAction_ID = "CollapseCopiedFrom";
	
	public final static String ExpandNextAction_ID = "ExpandNext";
	public final static String ExpandPreviousAction_ID = "ExpandPrevious";
	public final static String ExpandCopiedToAction_ID = "ExpandCopiedTo";
	public final static String ExpandCopiedFromAction_ID = "ExpandCopiedFrom";		
	
	public CollapseExpandAction(IWorkbenchPart part, String actionId) {
		super(part);
		
		setId(actionId);
		
		String text = "";
		if (CollapseNextAction_ID.equals(actionId)) {
			text = "Collapse Next";
		} else if (CollapsePreviousAction_ID.equals(actionId)) {
			text = "Collapse Previous";
		} else if (CollapseCopiedToAction_ID.equals(actionId)) {
			text = "Collapse Copied To";
		} else if (CollapseCopiedFromAction_ID.equals(actionId)) {
			text = "Collapse Copied From";
		} else if (ExpandNextAction_ID.equals(actionId)) {
			text = "Expand Next";
		} else if (ExpandPreviousAction_ID.equals(actionId)) {
			text = "Expand Previous";
		} else if (ExpandCopiedToAction_ID.equals(actionId)) {
			text = "Expand Copied To";
		} else if (ExpandCopiedFromAction_ID.equals(actionId)) {
			text = "Expand Copied From";
		}
		
		setText(text);	
		setToolTipText(text);		
	}
		
	@Override
	protected boolean calculateEnabled() {
		RevisionEditPart[] editParts = this.getSelectedEditParts();
		if (editParts.length == 1) {
			RevisionNode node = editParts[0].getCastedModel();
			
			if (CollapseNextAction_ID.equals(this.getId())) {
				return node.getNext() != null;					
			} else if (CollapsePreviousAction_ID.equals(this.getId())) {
				return node.getPrevious() != null;
			} else if (CollapseCopiedToAction_ID.equals(this.getId())) {
				return node.getCopiedTo().length > 0;
			} else if (CollapseCopiedFromAction_ID.equals(this.getId())) {
				return node.getCopiedFrom() != null;
			} else if (ExpandNextAction_ID.equals(this.getId())) {
				return true;
			} else if (ExpandPreviousAction_ID.equals(this.getId())) {
				return true;
			} else if (ExpandCopiedToAction_ID.equals(this.getId())) {
				return true;
			} else if (ExpandCopiedFromAction_ID.equals(this.getId())) {
				return true;
			}				
		}
		return false;
	}
	
	@Override
	public void run() {
		RevisionEditPart editPart = this.getSelectedEditPart();
		
		if (CollapseNextAction_ID.equals(this.getId())) {
			editPart.getRevisionRootNode().collapseNext(editPart.getCastedModel());	
		} else if (CollapsePreviousAction_ID.equals(this.getId())) {
			editPart.getRevisionRootNode().collapsePrevious(editPart.getCastedModel());
		} else if (CollapseCopiedToAction_ID.equals(this.getId())) {
			editPart.getRevisionRootNode().collapseCopiedTo(editPart.getCastedModel());
		} else if (CollapseCopiedFromAction_ID.equals(this.getId())) {
			editPart.getRevisionRootNode().collapseCopiedFrom(editPart.getCastedModel());
		} else if (ExpandNextAction_ID.equals(this.getId())) {
			editPart.getRevisionRootNode().expandNext(editPart.getCastedModel());
		} else if (ExpandPreviousAction_ID.equals(this.getId())) {
			editPart.getRevisionRootNode().expandPrevious(editPart.getCastedModel());
		} else if (ExpandCopiedToAction_ID.equals(this.getId())) {
			editPart.getRevisionRootNode().expandCopiedTo(editPart.getCastedModel());
		} else if (ExpandCopiedFromAction_ID.equals(this.getId())) {
			editPart.getRevisionRootNode().expandCopiedFrom(editPart.getCastedModel());
		}				
	}	
	
}
