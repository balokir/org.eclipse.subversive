/*******************************************************************************
 * Copyright (c) 2005-2010 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo (Polarion Software) - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.revision.graph.graphic.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphMessages;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphPlugin;
import org.eclipse.team.svn.revision.graph.graphic.RevisionGraphEditor;
import org.eclipse.team.svn.revision.graph.graphic.RevisionRootNode;
import org.eclipse.ui.IEditorPart;

/**
 * Action to truncate revision node paths
 * 
 * @author Igor Burilo
 */
public class TruncatePathsAction extends Action {

	public final static String TruncatePathsAction_ID = "TruncatePaths"; //$NON-NLS-1$
	
	protected IEditorPart editor;
	
	public TruncatePathsAction(IEditorPart editor) {
		super(SVNRevisionGraphMessages.TruncatePathsAction, Action.AS_CHECK_BOX);
		this.editor = editor;
		
		this.setToolTipText(SVNRevisionGraphMessages.TruncatePathsAction);
		this.setImageDescriptor(SVNRevisionGraphPlugin.instance().getImageDescriptor("icons/dots.gif")); //$NON-NLS-1$
		this.setId(TruncatePathsAction.TruncatePathsAction_ID);		
	}
	
	public void setActiveEditor(IEditorPart editor) {
		this.editor = editor;
	}
	
	@Override
	public void run() {			
		if (this.editor instanceof RevisionGraphEditor) {
			RevisionGraphEditor graphEditor = (RevisionGraphEditor) this.editor;
			Object objModel = graphEditor.getModel();
			if (objModel instanceof RevisionRootNode) {
				RevisionRootNode model = (RevisionRootNode) objModel;
				model.setTruncatePaths(this.isChecked());
			}
		}			
	}
	
}
