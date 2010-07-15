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

import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.team.svn.revision.graph.graphic.RevisionGraphEditor;
import org.eclipse.team.svn.revision.graph.graphic.RevisionRootNode;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.ActionFactory;

/**
 * @author Igor Burilo
 */
public class RevisionGraphActionBarContributor extends GraphActionBarContributor {

	protected RevisionGraphEditor editor;	
	
	protected ChangeModeAction changeModetAction;
	protected RefreshRevisionGraphAction refreshAction;
	protected ClearMergesAction clearMergesAction;
	protected TruncatePathsAction truncatePathsAction;
	protected FindRevisionNodeAction findAction;
	
	@Override
	public void setActiveEditor(IEditorPart editor) {
		super.setActiveEditor(editor);
		
		this.editor = (RevisionGraphEditor)editor;
		this.changeModetAction.setActiveEditor(this.editor);
		this.refreshAction.setActiveEditor(this.editor);
		this.clearMergesAction.setActiveEditor(this.editor);
		this.truncatePathsAction.setActiveEditor(this.editor);
		this.findAction.setActiveEditor(this.editor);
		
		if (!(this.editor.getModel() instanceof RevisionRootNode)) {
			this.changeModetAction.setEnabled(false);
			this.refreshAction.setEnabled(false);
			this.clearMergesAction.setEnabled(false);
			this.truncatePathsAction.setEnabled(false);
			this.findAction.setEnabled(false);
		} else {
			RevisionRootNode rootNode = (RevisionRootNode) this.editor.getModel();
			this.changeModetAction.setChecked(rootNode.isSimpleMode());
			this.truncatePathsAction.setChecked(rootNode.isTruncatePaths());
			
			//TODO disable clear merges action if there'are no merge lines
//			if (!rootNode.isIncludeMergeInfo() || !rootNode.hasNodesWithMerges()) {
//				this.clearMergesAction.setEnabled(false);
//			} else {
//				this.clearMergesAction.setEnabled(true);
//			}
		}				
	}
	
	@Override
	public void init(IActionBars bars, IWorkbenchPage page) {	
		super.init(bars, page);
		
		bars.setGlobalActionHandler(ActionFactory.FIND.getId(), this.findAction);
	}
	
	@Override
	protected void buildActions() {
		this.changeModetAction = new ChangeModeAction(this.editor);		
		addAction(this.changeModetAction);
		
		this.refreshAction = new RefreshRevisionGraphAction(this.editor);
		addAction(this.refreshAction);
		
		this.clearMergesAction = new ClearMergesAction(this.editor);
		addAction(this.clearMergesAction);
		
		this.truncatePathsAction = new TruncatePathsAction(this.editor);
		addAction(this.truncatePathsAction);
		
		this.findAction = new FindRevisionNodeAction(this.editor);
		addAction(this.findAction);
	}
	
	@Override
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		super.contributeToToolBar(toolBarManager);
					
		toolBarManager.add(this.refreshAction);  
		toolBarManager.add(this.changeModetAction);
		toolBarManager.add(this.truncatePathsAction);
		toolBarManager.add(this.clearMergesAction);
		
		//toolBarManager.add(new Separator());
			
		toolBarManager.add(new ZoomComboContributionItem(getPage()));
	}

	@Override
	protected void declareGlobalActionKeys() {
		
	}

}
