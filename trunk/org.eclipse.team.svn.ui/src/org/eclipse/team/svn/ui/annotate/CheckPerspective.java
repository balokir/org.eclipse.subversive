/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.annotate;

import java.text.MessageFormat;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.dialog.PromptOptionDialog;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;

/**
 * Check on which perspective AnnotateView should be opened. 
 * 
 * @author Alexander Gurov
 */
public class CheckPerspective {
	public static void run(IWorkbenchWindow window) {
		IViewDescriptor viewDescriptor = PlatformUI.getWorkbench().getViewRegistry().find(AnnotateView.VIEW_ID);
		
		String perspectiveId = SVNTeamPreferences.getAnnotateString(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.ANNOTATE_PERSPECTIVE_NAME);
		
		IWorkbenchPage page = window.getActivePage();
		IPerspectiveDescriptor current = page == null ? null : page.getPerspective();
		if (current == null || !current.getId().equals(perspectiveId)) {
	    	final int []perspectiveType = new int[] {SVNTeamPreferences.getAnnotateInt(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.ANNOTATE_CHANGE_PERSPECTIVE_NAME)};
	    	
	    	if (perspectiveType[0] == SVNTeamPreferences.ANNOTATE_PROMPT_PERSPECTIVE) {
	    		IPerspectiveDescriptor descriptor = PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(perspectiveId);
	    		new PromptOptionDialog(
	    				window.getShell(), 
	    				SVNTeamUIPlugin.instance().getResource("CheckPerspective.ConfirmOpenPerspective.Title"),
	    				MessageFormat.format(SVNTeamUIPlugin.instance().getResource("CheckPerspective.ConfirmOpenPerspective.Message"), new String[] {viewDescriptor.getLabel(), descriptor.getLabel(), descriptor.getDescription()}), 
	    				SVNTeamUIPlugin.instance().getResource("CheckPerspective.ConfirmOpenPerspective.Remember"), 
	    				new PromptOptionDialog.AbstractOptionManager() {
							public void buttonPressed(IPreferenceStore store, int idx, boolean toggle) {
								perspectiveType[0] = idx == 0 ? SVNTeamPreferences.ANNOTATE_DEFAULT_PERSPECTIVE : SVNTeamPreferences.ANNOTATE_CURRENT_PERSPECTIVE;
								if (toggle) {
									SVNTeamPreferences.setAnnotateInt(store, SVNTeamPreferences.ANNOTATE_CHANGE_PERSPECTIVE_NAME, perspectiveType[0]);
								}
							}
						}).open();
	    	}
	    	
	    	if (perspectiveType[0] == SVNTeamPreferences.ANNOTATE_DEFAULT_PERSPECTIVE) {
	        	try {
					PlatformUI.getWorkbench().showPerspective(perspectiveId, window);
				} 
	        	catch (WorkbenchException e) {
					new RuntimeException(e);
				}
	    	}
		}
	}
	
}
