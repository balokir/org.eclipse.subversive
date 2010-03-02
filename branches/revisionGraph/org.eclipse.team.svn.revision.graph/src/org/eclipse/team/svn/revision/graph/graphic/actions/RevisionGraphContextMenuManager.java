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

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * 
 * @author Igor Burilo
 */
public class RevisionGraphContextMenuManager extends ContextMenuProvider {

	public static final String GROUP_OPEN = "open";
	public static final String GROUP_COMPARE = "compare";
	public static final String GROUP_1 = "group1";
	public static final String GROUP_2 = "group2";
	public static final String GROUP_3 = "group3";
	
	protected ActionRegistry actionRegistry;
	
	public RevisionGraphContextMenuManager(EditPartViewer viewer, ActionRegistry actionRegistry) {
		super(viewer);
		this.actionRegistry = actionRegistry;
	}

	@Override
	public void buildContextMenu(IMenuManager menu) {
		menu.add(new Separator(RevisionGraphContextMenuManager.GROUP_OPEN));
		menu.add(new Separator(RevisionGraphContextMenuManager.GROUP_COMPARE));
		menu.add(new Separator(RevisionGraphContextMenuManager.GROUP_1));
		menu.add(new Separator(RevisionGraphContextMenuManager.GROUP_2));
		menu.add(new Separator(RevisionGraphContextMenuManager.GROUP_3));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		
		IAction action = this.actionRegistry.getAction(OpenAction.OpenAction_ID);
		menu.appendToGroup(RevisionGraphContextMenuManager.GROUP_OPEN, action);
		
		action = this.actionRegistry.getAction(CompareWithEachOtherAction.CompareWithEachOtherAction_ID);
		menu.appendToGroup(RevisionGraphContextMenuManager.GROUP_COMPARE, action);
		
		action = this.actionRegistry.getAction(CompareWithHeadAction.CompareWithHeadAction_ID);
		menu.appendToGroup(RevisionGraphContextMenuManager.GROUP_COMPARE, action);
		
		action = this.actionRegistry.getAction(CompareWithPreviousAction.CompareWithPreviousAction_ID);
		menu.appendToGroup(RevisionGraphContextMenuManager.GROUP_COMPARE, action);	
		
		action = this.actionRegistry.getAction(ShowHistoryAction.ShowHistoryAction_ID);
		menu.appendToGroup(RevisionGraphContextMenuManager.GROUP_1, action);
		
		action = this.actionRegistry.getAction(ShowPropertiesAction.ShowPropertiesAction_ID);
		menu.appendToGroup(RevisionGraphContextMenuManager.GROUP_1, action);
		
		action = this.actionRegistry.getAction(ComparePropertiesAction.ComparePropertiesAction_ID);
		menu.appendToGroup(RevisionGraphContextMenuManager.GROUP_1, action);	
		
		action = this.actionRegistry.getAction(ExportAction.ExportAction_ID);
		menu.appendToGroup(RevisionGraphContextMenuManager.GROUP_2, action);
		
		action = this.actionRegistry.getAction(CreatePatchAction.CreatePatchAction_ID);
		menu.appendToGroup(RevisionGraphContextMenuManager.GROUP_2, action);
		
		action = this.actionRegistry.getAction(ExtractAction.ExtractAction_ID);
		menu.appendToGroup(RevisionGraphContextMenuManager.GROUP_2, action);
		
		action = this.actionRegistry.getAction(CreateBranchTagAction.CreateBranchAction_ID);
		menu.appendToGroup(RevisionGraphContextMenuManager.GROUP_3, action);
		
		action = this.actionRegistry.getAction(CreateBranchTagAction.CreateTagAction_ID);
		menu.appendToGroup(RevisionGraphContextMenuManager.GROUP_3, action);
		
		action = this.actionRegistry.getAction(AddRevisionLinksAction.AddRevisionLinksAction_ID);
		menu.appendToGroup(RevisionGraphContextMenuManager.GROUP_3, action);	
	}

}
