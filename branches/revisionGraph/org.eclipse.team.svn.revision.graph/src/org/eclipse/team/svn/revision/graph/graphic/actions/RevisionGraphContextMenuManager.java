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

	public static final String GROUP_COMPARE = "compare";
	//TODO rename
	public static final String GROUP_SINGLE_REVISIONS = "singleRevision";
	
	protected ActionRegistry actionRegistry;
	
	public RevisionGraphContextMenuManager(EditPartViewer viewer, ActionRegistry actionRegistry) {
		super(viewer);
		this.actionRegistry = actionRegistry;
	}

	@Override
	public void buildContextMenu(IMenuManager menu) {
		menu.add(new Separator(RevisionGraphContextMenuManager.GROUP_COMPARE));
		menu.add(new Separator(RevisionGraphContextMenuManager.GROUP_SINGLE_REVISIONS));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		
		IAction action = this.actionRegistry.getAction(CompareWithEachOtherAction.CompareWithEachOtherAction_ID);
		menu.appendToGroup(RevisionGraphContextMenuManager.GROUP_COMPARE, action);
		
		action = this.actionRegistry.getAction(CompareWithHeadAction.CompareWithHeadAction_ID);
		menu.appendToGroup(RevisionGraphContextMenuManager.GROUP_COMPARE, action);
		
		action = this.actionRegistry.getAction(CompareWithPreviousAction.CompareWithPreviousAction_ID);
		menu.appendToGroup(RevisionGraphContextMenuManager.GROUP_COMPARE, action);	
		
		action = this.actionRegistry.getAction(ShowHistoryAction.ShowHistoryAction_ID);
		menu.appendToGroup(RevisionGraphContextMenuManager.GROUP_SINGLE_REVISIONS, action);		
	}

}
