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
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.decorator.wrapper.AbstractDecoratorWrapper;
import org.eclipse.team.svn.ui.decorator.wrapper.WorkingSetDecoratorWrapper;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.ITheme;

/**
 * Working set decorator
 * 
 * @author Sergiy Logvin
 */
public class WorkingSetDecorator extends LabelProvider implements ILightweightLabelDecorator {
	protected boolean useFonts;
	
	protected IPropertyChangeListener configurationListener;
	
	protected Color changedForegroundColor;
	protected Color changedBackgroundColor;
	protected Font changedFont;
	
	public WorkingSetDecorator() {
		this.configurationListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().startsWith(SVNTeamPreferences.DECORATION_BASE) || 
					event.getProperty().startsWith(SVNTeamPreferences.DATE_FORMAT_BASE)) {
					WorkingSetDecorator.this.loadConfiguration();
					String decoratorId = WorkingSetDecoratorWrapper.class.getName();
					SVNTeamUIPlugin.instance().getWorkbench().getDecoratorManager().update(decoratorId);
				}
			}
		};
		
		SVNTeamUIPlugin.instance().getPreferenceStore().addPropertyChangeListener(this.configurationListener);
		PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().addPropertyChangeListener(this.configurationListener);
	}
	
	public void dispose() {
		PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().removePropertyChangeListener(this.configurationListener);
		SVNTeamUIPlugin.instance().getPreferenceStore().removePropertyChangeListener(this.configurationListener);
		
		super.dispose();
	}

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
						if (this.changedFont == null) {
							this.loadConfiguration();
						}
						decoration.addPrefix(SVNTeamPreferences.getDecorationString(store, SVNTeamPreferences.DECORATION_FLAG_OUTGOING_NAME) + " ");
						if (this.useFonts) {
							decoration.setBackgroundColor(this.changedBackgroundColor);
							decoration.setForegroundColor(this.changedForegroundColor);
							decoration.setFont(this.changedFont);
						}
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

	protected void loadConfiguration() {
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		
		this.useFonts = SVNTeamPreferences.getDecorationBoolean(store, SVNTeamPreferences.DECORATION_USE_FONT_COLORS_DECOR_NAME);
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				ITheme current = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
				WorkingSetDecorator.this.changedFont = current.getFontRegistry().get(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.NAME_OF_OUTGOING_FONT));
				WorkingSetDecorator.this.changedForegroundColor = current.getColorRegistry().get(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.NAME_OF_OUTGOING_FOREGROUND_COLOR));
				WorkingSetDecorator.this.changedBackgroundColor = current.getColorRegistry().get(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.NAME_OF_OUTGOING_BACKGROUND_COLOR));
			}
		});
	}
	
}
