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

import org.eclipse.jface.action.Action;
import org.eclipse.team.svn.revision.graph.graphic.RevisionGraphEditor;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.ui.IEditorPart;

/**
 * Refresh revision graph
 * 
 * @author Igor Burilo
 */
public class RefreshRevisionGraphAction extends Action {

	public final static String RefreshRevisionGraphAction_ID = "RefreshRevisionGraph";
	
	protected IEditorPart editor;
	
	public RefreshRevisionGraphAction(IEditorPart editor) {
		super("Refresh");
		this.editor = editor;
		
		this.setToolTipText("Refresh");
		this.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/refresh.gif"));
		this.setId(RefreshRevisionGraphAction.RefreshRevisionGraphAction_ID);		
	}
	
	public void setActiveEditor(IEditorPart editor) {
		this.editor = editor;
	}
	
	@Override
	public void run() {			
		if (this.editor instanceof RevisionGraphEditor) {
			((RevisionGraphEditor) this.editor).handleRefresh();			
		}			
	}
}
