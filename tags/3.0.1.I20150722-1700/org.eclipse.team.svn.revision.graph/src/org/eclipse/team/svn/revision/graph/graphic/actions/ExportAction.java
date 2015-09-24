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

import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.team.svn.core.connector.SVNDepth;
import org.eclipse.team.svn.core.operation.remote.ExportOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.ui.IWorkbenchPart;

/**
 * 
 * @author Igor Burilo
 */
public class ExportAction extends BaseRevisionGraphAction {

	public final static String ExportAction_ID = "Export"; //$NON-NLS-1$
	
	public ExportAction(IWorkbenchPart part) {
		super(part);
		
		setText(SVNUIMessages.ExportCommand_label);
		setId(ExportAction_ID);
		setToolTipText(SVNUIMessages.ExportCommand_label);		
		setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/export.gif")); //$NON-NLS-1$
	}

	@Override
	protected boolean calculateEnabled() {		
		return this.isEnable(BaseRevisionGraphAction.NOT_DELETED_ACTION_FILTER, 1);
	}
	
	@Override
	public void run() {		
		DirectoryDialog fileDialog = new DirectoryDialog(this.getWorkbenchPart().getSite().getShell());
		fileDialog.setText(SVNUIMessages.ExportPanel_ExportFolder);
		fileDialog.setMessage(SVNUIMessages.ExportPanel_ExportFolder_Msg);
		String path = fileDialog.open();
		if (path != null) {
			IRepositoryResource resource = BaseRevisionGraphAction.convertToResource(this.getSelectedEditPart());
			boolean ignoreExternals = SVNTeamPreferences.getBehaviourBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.BEHAVIOUR_IGNORE_EXTERNALS_NAME);
			ExportOperation op = new ExportOperation(new IRepositoryResource[]{resource}, path, SVNDepth.INFINITY, ignoreExternals);						
			this.runOperation(op);	
		}							
	}

}
