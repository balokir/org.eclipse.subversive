/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.repository;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.team.svn.ui.annotate.AnnotateView;
import org.eclipse.team.svn.ui.history.SVNHistoryPage;
import org.eclipse.team.svn.ui.properties.PropertiesView;
import org.eclipse.team.svn.ui.repository.browser.RepositoryBrowser;

/**
 * Repository view perspective 
 * 
 * @author Alexander Gurov
 */
public class RepositoryPerspective implements IPerspectiveFactory {
	public static final String ID = RepositoryPerspective.class.getName();

	public RepositoryPerspective() {

	}

	public void createInitialLayout(IPageLayout layout) {
		// Add new Repository Location wizard
		layout.addNewWizardShortcut("org.eclipse.team.svn.ui.wizard.NewRepositoryLocationWizard");
		
		// Add "show views". They will be present in "show view" menu
		layout.addShowViewShortcut(RepositoriesView.VIEW_ID);
		layout.addShowViewShortcut(RepositoryBrowser.VIEW_ID);
		layout.addShowViewShortcut(AnnotateView.VIEW_ID);
		layout.addShowViewShortcut(SVNHistoryPage.VIEW_ID);
//		layout.addShowViewShortcut(HistoryView.VIEW_ID);
		layout.addShowViewShortcut(PropertiesView.VIEW_ID);
		
		// Add  "perspective short cut". They will be present in "open perspective" menu
		layout.addPerspectiveShortcut(RepositoryPerspective.ID);
		layout.addPerspectiveShortcut("org.eclipse.jdt.ui.JavaPerspective");
		layout.addPerspectiveShortcut("org.eclipse.ui.resourcePerspective");
		layout.addPerspectiveShortcut("org.eclipse.team.ui.TeamSynchronizingPerspective");
		
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);

		IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, 0.26f, editorArea);
		left.addView(RepositoriesView.VIEW_ID);
		left.addView(AnnotateView.VIEW_ID);
		
		IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, 0.66f, editorArea);
		bottom.addView(RepositoryBrowser.VIEW_ID);
		bottom.addView(SVNHistoryPage.VIEW_ID);
//		bottom.addView(HistoryView.VIEW_ID);
		bottom.addView(PropertiesView.VIEW_ID);
	}

}
