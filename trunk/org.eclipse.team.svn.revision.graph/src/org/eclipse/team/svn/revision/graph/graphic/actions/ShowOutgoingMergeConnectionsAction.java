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

import org.eclipse.team.svn.revision.graph.PathRevision.RevisionNodeAction;
import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;
import org.eclipse.team.svn.revision.graph.graphic.editpart.RevisionEditPart;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Show outgoing merge connections
 * 
 * @author Igor Burilo
 */
public class ShowOutgoingMergeConnectionsAction extends BaseRevisionGraphAction {

	public final static String ShowOutgoingMergeConnectionsAction_ID = "ShowOutgoingMergeConnections"; //$NON-NLS-1$
	
	protected static AbstractRevisionEditPartFilter filter = new AbstractRevisionEditPartFilter() {		
		public boolean accept(RevisionEditPart editPart) {
			RevisionNode node = editPart.getCastedModel();
			return node.getAction() != RevisionNodeAction.NONE && node.hasOutgoingMerges();
		}		
	};
	
	public ShowOutgoingMergeConnectionsAction(IWorkbenchPart part) {
		super(part);
		
		setText("Show Outgoing Merges");
		setId(ShowOutgoingMergeConnectionsAction_ID);
		setToolTipText("Show Outgoing Merges");		
	}

	protected boolean calculateEnabled() {
		return this.getSelectedEditParts(filter).length > 0;
	}
	
	public void run() {				
		RevisionEditPart[] editParts = this.getSelectedEditParts(filter);
		for (RevisionEditPart editPart : editParts) {
			editPart.getCastedModel().addAllOutgoingMergeConnections();
		}	
	}
	
}
