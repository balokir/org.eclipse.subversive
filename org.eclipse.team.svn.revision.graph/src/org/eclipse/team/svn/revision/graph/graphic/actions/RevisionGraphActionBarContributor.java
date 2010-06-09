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
import org.eclipse.ui.IEditorPart;

/**
 * @author Igor Burilo
 */
public class RevisionGraphActionBarContributor extends GraphActionBarContributor {

	protected RevisionGraphEditor editor;	
	
	protected ChangeModeAction changeModetAction;
	protected RefreshRevisionGraphAction refreshAction;
	protected ClearMergesAction clearMergesAction;
	
	@Override
	public void setActiveEditor(IEditorPart editor) {
		super.setActiveEditor(editor);
		
		this.editor = (RevisionGraphEditor)editor;
		this.changeModetAction.setActiveEditor(this.editor);
		this.refreshAction.setActiveEditor(this.editor);
		this.clearMergesAction.setActiveEditor(this.editor);
		
		if (!(this.editor.getModel() instanceof RevisionRootNode)) {
			this.changeModetAction.setEnabled(false);
			this.refreshAction.setEnabled(false);
			this.clearMergesAction.setEnabled(false);
		} else {
			RevisionRootNode rootNode = (RevisionRootNode) this.editor.getModel();
			this.changeModetAction.setChecked(rootNode.isSimpleMode());
			
			if (!rootNode.isIncludeMergeInfo() || !rootNode.hasNodesWithMerges()) {
				this.clearMergesAction.setEnabled(false);
			}
		}				
	}
	
	@Override
	protected void buildActions() {
		this.changeModetAction = new ChangeModeAction(this.editor);		
		addAction(this.changeModetAction);
		
		this.refreshAction = new RefreshRevisionGraphAction(this.editor);
		addAction(this.refreshAction);
		
		this.clearMergesAction = new ClearMergesAction(this.editor);
		addAction(this.clearMergesAction);
	}
	
	@Override
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		super.contributeToToolBar(toolBarManager);
					
		toolBarManager.add(this.refreshAction);  
		toolBarManager.add(this.changeModetAction);  
		toolBarManager.add(this.clearMergesAction);
		
		//toolBarManager.add(new Separator());
			
		toolBarManager.add(new ZoomComboContributionItem(getPage()));
	}

	@Override
	protected void declareGlobalActionKeys() {
		
	}

}
