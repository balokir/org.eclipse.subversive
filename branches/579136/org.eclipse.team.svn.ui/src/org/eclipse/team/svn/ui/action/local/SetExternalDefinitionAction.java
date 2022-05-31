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

package org.eclipse.team.svn.ui.action.local;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.property.ConcatenateProperyDataOperation;
import org.eclipse.team.svn.core.operation.local.property.GenerateExternalsPropertyOperation;
import org.eclipse.team.svn.core.operation.local.property.SetPropertiesOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.action.AbstractNonRecursiveTeamAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.view.property.ExternalsEditPanel;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Set externals properties operation
 * 
 * @author Igor Burilo
 */
public class SetExternalDefinitionAction extends AbstractNonRecursiveTeamAction {

	public void runImpl(IAction action) {
		IResource resource = this.getSelectedResources()[0];	
		IActionOperation op = SetExternalDefinitionAction.getAction(resource, this.getShell());
		if (op != null) {
			this.runScheduled(op);
		}
	}
		
	public boolean isEnabled() {
		return this.getSelectedResources().length == 1 && this.checkForResourcesPresence(IStateFilter.SF_VERSIONED_FOLDERS);
	}
	
	public static IActionOperation getAction(IResource resource, Shell shell) {		
		IRepositoryResource repositoryResource = SVNRemoteStorage.instance().asRepositoryResource(resource);
		ExternalsEditPanel panel = new ExternalsEditPanel("SetExternalDefinitionPage", "RepositoryResourceOnlySelectionComposite_URL", resource, repositoryResource); //$NON-NLS-1$ //$NON-NLS-2$
		DefaultDialog dialog = new DefaultDialog(shell != null ? shell : UIMonitorUtility.getShell(), panel);
		if (dialog.open() == 0) {						 									
			GenerateExternalsPropertyOperation generatePropOp = new GenerateExternalsPropertyOperation(resource, panel.getUrl(), panel.getRevision(), panel.getLocalPath(), panel.isPriorToSVN15Format());			
			ConcatenateProperyDataOperation concatOp = new ConcatenateProperyDataOperation(resource, SVNProperty.BuiltIn.EXTERNALS, generatePropOp);
			concatOp.setStringValuesSeparator(""); //$NON-NLS-1$
			SetPropertiesOperation setPropsOp = new SetPropertiesOperation(new IResource[] {resource}, concatOp, false);			
			CompositeOperation op = new CompositeOperation(setPropsOp.getId(), setPropsOp.getMessagesClass());
			op.add(generatePropOp);
			op.add(concatOp, new IActionOperation[]{generatePropOp});
			op.add(setPropsOp, new IActionOperation[]{concatOp});
			op.add(new RefreshResourcesOperation(new IResource[] {resource}, IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_ALL), new IActionOperation[] {setPropsOp});
			return op;			
		}
		return null;
	}
		  
}
