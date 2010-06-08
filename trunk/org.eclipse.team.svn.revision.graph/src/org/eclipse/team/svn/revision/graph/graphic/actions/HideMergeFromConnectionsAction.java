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

import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;
import org.eclipse.team.svn.revision.graph.graphic.editpart.RevisionEditPart;
import org.eclipse.ui.IWorkbenchPart;

/**
 * TODO make correct implementation
 * 
 * @author Igor Burilo
 */
public class HideMergeFromConnectionsAction extends BaseRevisionGraphAction {

	public final static String HideMergeFromConnectionsAction_ID = "HideMergeFromConnections"; //$NON-NLS-1$
	
	public HideMergeFromConnectionsAction(IWorkbenchPart part) {
		super(part);
		
		setText("Hide Merge From Connections");
		setId(HideMergeFromConnectionsAction_ID);
		setToolTipText("Hide Merge From Connections");		
	}

	protected boolean calculateEnabled() {
		RevisionEditPart editPart = this.getSelectedEditPart();
		if (editPart != null) {
			RevisionNode node = editPart.getCastedModel();
			return node.isAddedAllMergeTargetConnections();
		}
		return false;
	}
	
	public void run() {		
		RevisionEditPart editPart = this.getSelectedEditPart();						
		RevisionNode node = editPart.getCastedModel();
		node.removeAllMergeTargetConnections();
	}

}
