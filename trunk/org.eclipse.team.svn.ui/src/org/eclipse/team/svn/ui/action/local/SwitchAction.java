/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.action.local;

import java.util.HashMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RestoreProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.SaveProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.SwitchOperation;
import org.eclipse.team.svn.core.operation.local.property.GetPropertiesOperation;
import org.eclipse.team.svn.core.operation.local.property.SetPropertiesOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.action.AbstractNonRecursiveTeamAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.dialog.OperationErrorDialog;
import org.eclipse.team.svn.ui.operation.NotifyUnresolvedConflictOperation;
import org.eclipse.team.svn.ui.panel.local.SwitchPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Switch working copy to the new URL action implementation
 * 
 * @author Alexander Gurov
 */
public class SwitchAction extends AbstractNonRecursiveTeamAction {

	public SwitchAction() {
		super();
	}
	
	public void runImpl(IAction action) {
		IResource []resources = this.getSelectedResources(IStateFilter.SF_EXCLUDE_DELETED);
		
		if (!OperationErrorDialog.isAcceptableAtOnce(resources, SVNUIMessages.SwitchAction_Error, this.getShell())) {
			return;
		}
		
		// in case of multiple switch selected repository resource should be recognized as branch/tag/trunk (using copied from). If "copied from" inaccessible multi switch will fails, single switch for project should be performed like for folders 
		
	//FIXME peg revision, revision limitation ???
		SwitchPanel panel;
		if (resources.length > 1) {
			boolean containFolders = false;
			for (IResource current : resources) {
				if (current instanceof IContainer) {
					containFolders = true;
					break;
				}
			}
			IRepositoryResource remote = SVNRemoteStorage.instance().asRepositoryResource(resources[0]);
			remote = SVNUtility.getTrunkLocation(remote);
			panel = new SwitchPanel(remote, SVNRevision.INVALID_REVISION_NUMBER, containFolders);
		}
		else {
			IResource resource = resources[0];
			IRepositoryResource remote = SVNRemoteStorage.instance().asRepositoryResource(resource);
			ILocalResource local = SVNRemoteStorage.instance().asLocalResourceAccessible(resource);
			panel = new SwitchPanel(remote, local.getRevision(), resources[0] instanceof IContainer);
		}
			
		DefaultDialog dialog = new DefaultDialog(this.getShell(), panel);
		if (dialog.open() == 0) {
			IRepositoryResource []destinations = panel.getSelection(resources);
			boolean ignoreExternals = SVNTeamPreferences.getBehaviourBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.BEHAVIOUR_IGNORE_EXTERNALS_NAME);
			SwitchOperation mainOp = new SwitchOperation(resources, destinations, panel.getDepth(), panel.isStickyDepth(), ignoreExternals);
			
			CompositeOperation op = new CompositeOperation(mainOp.getId(), mainOp.getMessagesClass());

			// see bug #406580
			HashMap<IResource, String> externalsMap = new HashMap<IResource, String>();
			for (int i = 0; i < resources.length; i++) {
				ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resources[i]);
				if ((local.getChangeMask() & ILocalResource.IS_SVN_EXTERNALS) != 0) {
					IResource externalsPropNode = null, parent = resources[i];
					while (externalsPropNode == null && parent.getType() != IResource.PROJECT) {
						parent = parent.getParent();
						GetPropertiesOperation propOp = new GetPropertiesOperation(parent); // failure! it could be not a direct parent!
						if (UIMonitorUtility.doTaskBusyDefault(propOp).getOperation().getExecutionState() == IActionOperation.OK) {
							for (SVNProperty prop : propOp.getProperties()) {
								if (SVNProperty.BuiltIn.EXTERNALS.equals(prop.name)) {
									String tVal = externalsMap.containsKey(parent) ? externalsMap.get(parent) : prop.value;
									SVNUtility.SVNExternalPropertyData []data = SVNUtility.SVNExternalPropertyData.parse(tVal);
									for (SVNUtility.SVNExternalPropertyData entry : data) {
										IPath localPath = FileUtility.getResourcePath(parent).append(entry.localPath);
										if (localPath.equals(FileUtility.getResourcePath(resources[i]))) {
											entry.url = SVNUtility.encodeURL(destinations[i].getUrl());
											String propVal = SVNUtility.SVNExternalPropertyData.serialize(data);
											externalsPropNode = parent;
											externalsMap.put(parent, propVal);
											break;
										}
									}
									break;
								}
							}
						}
					}
				}
			}
			for (IResource propNode : externalsMap.keySet()) {
				SetPropertiesOperation setOp = new SetPropertiesOperation(new IResource[] {propNode}, new SVNProperty[] {new SVNProperty(SVNProperty.BuiltIn.EXTERNALS, externalsMap.get(propNode))}, false);
				op.add(setOp);
			}
			if (externalsMap.size() > 0) {
				op.add(new RefreshResourcesOperation(externalsMap.keySet().toArray(new IResource[externalsMap.size()]), IResource.DEPTH_ZERO, RefreshResourcesOperation.REFRESH_CACHE));
			}
			
			SaveProjectMetaOperation saveOp = new SaveProjectMetaOperation(resources);
			op.add(saveOp);
			op.add(mainOp);
			op.add(new RestoreProjectMetaOperation(saveOp));
			op.add(new RefreshResourcesOperation(resources));
			op.add(new NotifyUnresolvedConflictOperation(mainOp));
			
			this.runScheduled(op);
		}
	}

	public boolean isEnabled() {
		return this.checkForResourcesPresence(IStateFilter.SF_EXCLUDE_DELETED);
	}
	
	protected boolean needsToSaveDirtyEditors() {
		return true;
	}
	
}
