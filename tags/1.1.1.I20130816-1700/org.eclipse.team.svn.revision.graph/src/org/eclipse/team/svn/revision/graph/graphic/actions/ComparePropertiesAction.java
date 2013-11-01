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

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.revision.graph.graphic.editpart.RevisionEditPart;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.compare.TwoWayPropertyCompareInput;
import org.eclipse.ui.IWorkbenchPart;

/**
 * 
 * @author Igor Burilo
 */
public class ComparePropertiesAction extends BaseRevisionGraphAction {

	public final static String ComparePropertiesAction_ID = "CompareProperties"; //$NON-NLS-1$
	
	public ComparePropertiesAction(IWorkbenchPart part) {
		super(part);
	
		setText(SVNUIMessages.SynchronizeActionGroup_CompareProperties);
		setId(ComparePropertiesAction_ID);
		setToolTipText(SVNUIMessages.SynchronizeActionGroup_CompareProperties);
	}

	@Override
	protected boolean calculateEnabled() {
		return this.isEnable(BaseRevisionGraphAction.NOT_DELETED_ACTION_FILTER, 2);
	}	
	
	@Override
	public void run() {						
		RevisionEditPart[] editParts = this.getSelectedEditParts(BaseRevisionGraphAction.NOT_DELETED_ACTION_FILTER);
		IRepositoryResource[] resources = BaseRevisionGraphAction.convertToResources(editParts);		
		IRepositoryResource first = resources[0];
		IRepositoryResource second = resources[1];
										
		if (((SVNRevision.Number)first.getSelectedRevision()).getNumber() < ((SVNRevision.Number) second.getSelectedRevision()).getNumber()) {				
			IRepositoryResource tmp = second;
			second = first;
			first = tmp;			
		}
		
		TwoWayPropertyCompareInput input = new TwoWayPropertyCompareInput(
				new CompareConfiguration(),
				SVNUtility.getEntryRevisionReference(first),
				SVNUtility.getEntryRevisionReference(second),
				this.getRepositoryLocation(editParts[0]));
		CompareUI.openCompareEditor(input);									
	}
}
