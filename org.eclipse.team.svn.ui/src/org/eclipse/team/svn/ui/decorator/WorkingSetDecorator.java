/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.decorator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.decorator.wrapper.AbstractDecoratorWrapper;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.ui.IWorkingSet;

/**
 * Working set decorator
 * 
 * @author Sergiy Logvin
 */
public class WorkingSetDecorator extends LabelProvider implements ILightweightLabelDecorator {

	public void decorate(Object element, IDecoration decoration) {
		try {
			if (element instanceof IWorkingSet) {
				boolean svnSharedPresents = false;
				boolean hasOutgoingChanges = false;
				IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
				IAdaptable []adaptables = ((IWorkingSet)element).getElements();
				for (int i = 0; i < adaptables.length; i++) {
					IProject project = (IProject)adaptables[i].getAdapter(IProject.class);
					if (AbstractDecoratorWrapper.isSVNShared(project)) {
						svnSharedPresents = true;
						if (FileUtility.checkForResourcesPresenceRecursive(new IResource[] {project}, IStateFilter.SF_ANY_CHANGE)) {
							hasOutgoingChanges = true;
							break;
						}
					}
				}
				if (svnSharedPresents) {
					boolean addVersioned = false;
					if (SVNTeamPreferences.getDecorationBoolean(store, SVNTeamPreferences.DECORATION_ICON_REMOTE_NAME)) {
						addVersioned = true;
					}
					if (hasOutgoingChanges) {
						decoration.addPrefix(SVNTeamPreferences.getDecorationString(store, SVNTeamPreferences.DECORATION_FLAG_OUTGOING_NAME) + " ");
						if (SVNTeamPreferences.getDecorationBoolean(store, SVNTeamPreferences.DECORATION_ICON_MODIFIED_NAME)) {
							decoration.addOverlay(AbstractResourceDecorator.OVR_MODIFIED);
							addVersioned = false;
						}
					}
					if (addVersioned) {
						decoration.addOverlay(AbstractResourceDecorator.OVR_VERSIONED);
					}
				}
			}
		}
		catch (Throwable ex) {
			//decorator should not throw any exceptions
		}
	}

}
