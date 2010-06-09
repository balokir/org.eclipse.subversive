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
 * Hide outgoing merge connections
 * 
 * @author Igor Burilo
 */
public class HideOutgoingMergeConnectionsAction extends BaseRevisionGraphAction {

	public final static String HideOutgoingMergeConnectionsAction_ID = "HideOutgoingMergeConnections"; //$NON-NLS-1$
	
	protected static AbstractRevisionEditPartFilter filter = new AbstractRevisionEditPartFilter() {		
		public boolean accept(RevisionEditPart editPart) {
			RevisionNode node = editPart.getCastedModel();
			return node.getAction() != RevisionNodeAction.NONE && node.hasMergeSourceConnections();
		}		
	};
	
	public HideOutgoingMergeConnectionsAction(IWorkbenchPart part) {
		super(part);
		
		setText("Hide Outgoing Merges");
		setId(HideOutgoingMergeConnectionsAction_ID);
		setToolTipText("Hide Outgoing Merges");		
	}

	protected boolean calculateEnabled() {
		return this.getSelectedEditParts(filter).length > 0;
	}
	
	public void run() {
		RevisionEditPart[] editParts = this.getSelectedEditParts(filter);
		for (RevisionEditPart editPart : editParts) {
			editPart.getCastedModel().removeAllMergeSourceConnections();
		}	
	}

}
