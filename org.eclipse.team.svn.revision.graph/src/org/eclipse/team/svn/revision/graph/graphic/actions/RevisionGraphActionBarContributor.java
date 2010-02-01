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

import org.eclipse.gef.ui.actions.ActionBarContributor;
import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphPlugin;
import org.eclipse.team.svn.revision.graph.graphic.RevisionGraphEditor;
import org.eclipse.team.svn.revision.graph.graphic.RevisionRootNode;
import org.eclipse.ui.IEditorPart;

/**
 * TODO !!Warning:  This class is subject to change.
 * 
 * @author Igor Burilo
 */
public class RevisionGraphActionBarContributor extends ActionBarContributor {

	protected RevisionGraphEditor editor;	
	
	protected ChangeLayoutAction changeLayoutAction;
	
	//TODO see FlyoutChangeLayoutAction in scheme editor
	protected static class ChangeLayoutAction extends Action {

		protected IEditorPart editor;
		
		public ChangeLayoutAction(IEditorPart editor) {
			super("Change Layout", Action.AS_CHECK_BOX);
			this.editor = editor;
		}
		
		public void setActiveEditor(IEditorPart editor) {
			this.editor = editor;
		}
		
		@Override
		public void run() {			
			if (this.editor instanceof RevisionGraphEditor) {
				RevisionGraphEditor graphEditor = (RevisionGraphEditor) editor;
				RevisionRootNode model = graphEditor.getModel();
				model.setMode(this.isChecked());			
			}			
		}
	}
	
	@Override
	public void setActiveEditor(IEditorPart editor) {
		super.setActiveEditor(editor);
		
		this.editor = (RevisionGraphEditor)editor;
		this.changeLayoutAction.setActiveEditor(this.editor);
	}
	
	@Override
	protected void buildActions() {
		this.changeLayoutAction = new ChangeLayoutAction(this.editor);
		this.changeLayoutAction.setToolTipText("Change Layout");
		this.changeLayoutAction.setImageDescriptor(SVNRevisionGraphPlugin.instance().getImageDescriptor("/icons/mode.gif"));
		changeLayoutAction.setId("org.eclipse.team.svn.revision.graph.graphic.actions.ChangeLayoutAction");		
		//changeLayoutAction.setDisabledImageDescriptor(create("icons/", "layout_disabled.gif"));
		addAction(this.changeLayoutAction);
	}
	
	@Override
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		super.contributeToToolBar(toolBarManager);
					
		toolBarManager.add(this.changeLayoutAction);  
		
		//toolBarManager.add(new Separator());
			
		toolBarManager.add(new ZoomComboContributionItem(getPage()));
	}

	@Override
	protected void declareGlobalActionKeys() {
		
	}

}
