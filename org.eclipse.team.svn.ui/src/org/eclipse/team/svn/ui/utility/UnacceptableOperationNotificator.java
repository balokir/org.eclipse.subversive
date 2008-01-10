/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Vladimir Bykov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.utility;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.local.ResourceListPanel;

/**
 * Unacceptable Operation class verifier implementation
 * 
 * @author Vladimir Bykov
 */
public class UnacceptableOperationNotificator {
	protected static final String PARENT_NOT_ON_REPOSITORY_CONFLICT_DESCRIPTION_MESSAGE = "The parent{0} of the following resource{1} {2} not on the repository. Do you want to continue?";
	protected static final String PARENT_NOT_ON_REPOSITORY_CONFLICT_DEFAULT_MESSAGE = "The operation will be applied to the parent{0} of {1} resource{2} as well.";
	
	public static IResource[] shrinkResourcesWithNotOnRespositoryParents(final Shell shell, IResource []resources) {
		HashSet resultResources = new HashSet();
		final Map unsupportedResources = new HashMap();
		if (resources == null) {
			return null;
		}
		for (int i = 0; i < resources.length; i++) {
			IResource []parents = FileUtility.getOperableParents(new IResource[] {resources[i]}, IStateFilter.SF_NOTONREPOSITORY, true);
			
			ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resources[i]);
			if (local == null || parents.length > 0 && IStateFilter.SF_ONREPOSITORY.accept(local)) {
				unsupportedResources.put(resources[i], parents);
			}
			else {
				resultResources.add(resources[i]);
			}
		}
		//delete from unsupported set resources which parents already exist in resultResources
		if (!resultResources.isEmpty() && !unsupportedResources.isEmpty()) {
			for (Iterator iter = unsupportedResources.keySet().iterator(); iter.hasNext();) {
				IResource res = (IResource) iter.next();
				List listOfParents = Arrays.asList((IResource [])unsupportedResources.get(res));
				if (resultResources.containsAll(listOfParents)) {
					iter.remove();
				}
			}
		}
		final boolean []isCanceled = new boolean[] {false};
		if (!unsupportedResources.isEmpty()) {
			final HashSet parents = new HashSet();
			for (Iterator iter = unsupportedResources.keySet().iterator(); iter.hasNext();) {
				IResource res = (IResource) iter.next();
				parents.addAll(Arrays.asList((IResource [])unsupportedResources.get(res)));
			}
			shell.getDisplay().syncExec(new Runnable() {
				public void run() {
					boolean oneParent = parents.size() == 1;
					boolean oneResource = unsupportedResources.size() == 1;
					String description;
					String defaultMessage;
					if (oneParent && oneResource) {
						description = SVNTeamUIPlugin.instance().getResource("UnacceptableOperation.Description.1");
						defaultMessage = SVNTeamUIPlugin.instance().getResource("UnacceptableOperation.Message.1");
					}
					else if (!oneParent && oneResource) {
						description = SVNTeamUIPlugin.instance().getResource("UnacceptableOperation.Description.2");
						defaultMessage = SVNTeamUIPlugin.instance().getResource("UnacceptableOperation.Message.2");
					}
					else if (oneParent && !oneResource) {
						description = SVNTeamUIPlugin.instance().getResource("UnacceptableOperation.Description.3");
						defaultMessage = SVNTeamUIPlugin.instance().getResource("UnacceptableOperation.Message.3");
					}
					else {
						description = SVNTeamUIPlugin.instance().getResource("UnacceptableOperation.Description.4");
						defaultMessage = SVNTeamUIPlugin.instance().getResource("UnacceptableOperation.Message.4");
					}
					ResourceListPanel panel = new ResourceListPanel((IResource [])unsupportedResources.keySet().toArray(new IResource[unsupportedResources.keySet().size()]), 
							SVNTeamUIPlugin.instance().getResource("UnacceptableOperation.Title"), description, 
							defaultMessage, new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL});
					DefaultDialog dialog = new DefaultDialog(shell, panel);
					if (dialog.open() != 0) {
						isCanceled[0] = true;
					}
				}
			});
			if (isCanceled[0]) {
				return null;
			}
			else {
				resultResources.addAll(parents);
			}
		}
		return (IResource [])resultResources.toArray(new IResource[resultResources.size()]);
	}

}
