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
 * Clear all merge connections from graph 
 * 
 * @author Igor Burilo
 */
public class ClearMergesAction extends Action {

	public final static String ClearMergesAction_ID = "ClearMerges"; //$NON-NLS-1$
	
	protected IEditorPart editor;
	
	public ClearMergesAction(IEditorPart editor) {
		super(SVNRevisionGraphMessages.ClearMergesAction_ClearConnections);
		this.editor = editor;
		
		this.setToolTipText(SVNRevisionGraphMessages.ClearMergesAction_ClearConnections);
		//TODO make correct icon
		this.setImageDescriptor(SVNRevisionGraphPlugin.instance().getImageDescriptor("icons/clearmerges.gif")); //$NON-NLS-1$
		this.setId(ClearMergesAction.ClearMergesAction_ID);		
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
				((RevisionRootNode) objModel).clearAllMerges();
			}
		}
	}
}
