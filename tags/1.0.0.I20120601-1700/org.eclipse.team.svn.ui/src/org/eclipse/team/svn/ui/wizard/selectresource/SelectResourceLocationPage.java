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

package org.eclipse.team.svn.ui.wizard.selectresource;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.composite.RepositoryTreeComposite;
import org.eclipse.team.svn.ui.repository.model.IRepositoryContentFilter;
import org.eclipse.team.svn.ui.repository.model.RepositoryFile;
import org.eclipse.team.svn.ui.repository.model.RepositoryResource;
import org.eclipse.team.svn.ui.repository.model.RepositoryRevisions;
import org.eclipse.team.svn.ui.wizard.AbstractVerifiedWizardPage;
import org.eclipse.ui.PlatformUI;

/**
 * Select repository resource wizard page
 * 
 * @author Igor Burilo
 */
public class SelectResourceLocationPage extends AbstractVerifiedWizardPage {

	protected RepositoryTreeComposite repositoryTree;
	
	protected IRepositoryResource resource;
	
	protected Object modelRoot;
	//if 'true',  then don't show files
	//if 'false', then don't allow to select folders
	protected boolean showOnlyFolders;
	
	public SelectResourceLocationPage(boolean showOnlyFolders, Object modelRoot) {
		super(SelectResourceLocationPage.class.getName(), SVNUIMessages.SelectResourceLocationPage_Title, SVNTeamUIPlugin.instance().getImageDescriptor("icons/wizards/newconnect.gif")); //$NON-NLS-1$
		this.setDescription(SVNUIMessages.SelectResourceLocationPage_Description);
		this.showOnlyFolders = showOnlyFolders;
		this.modelRoot = modelRoot;
	}
	
	protected Composite createControlImpl(Composite parent) {
		GridLayout layout = null;
		GridData data = null;
		this.initializeDialogUnits(parent);
		
		Composite composite = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		composite.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		this.repositoryTree = new RepositoryTreeComposite(composite, SWT.BORDER);
		data = new GridData(GridData.FILL_BOTH);
		this.repositoryTree.setLayoutData(data);
		
		this.repositoryTree.getRepositoryTreeViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				SelectResourceLocationPage.this.resource = null;				
				IStructuredSelection sel = (IStructuredSelection) SelectResourceLocationPage.this.repositoryTree.getRepositoryTreeViewer().getSelection();
				IRepositoryResource rr;
				if (!sel.isEmpty() && sel.getFirstElement() instanceof RepositoryResource) {
					rr = ((RepositoryResource)sel.getFirstElement()).getRepositoryResource();
					if (SelectResourceLocationPage.this.showOnlyFolders && !(rr instanceof IRepositoryFile)) {
						SelectResourceLocationPage.this.resource = rr;
					} else if (!SelectResourceLocationPage.this.showOnlyFolders && rr instanceof IRepositoryFile) {
						SelectResourceLocationPage.this.resource = rr;
					}
				}								
				SelectResourceLocationPage.this.validateContent();
			}
		});
		
		//filter out revisions and files (if necessary)
		this.repositoryTree.setFilter(new IRepositoryContentFilter() {
			public boolean accept(Object obj) {
				if (obj instanceof RepositoryRevisions || SelectResourceLocationPage.this.showOnlyFolders && obj instanceof RepositoryFile) {
					return false;
				}
				return true;								
			}			
		});	
		
		if (this.modelRoot != null) {
			this.repositoryTree.setModelRoot(this.modelRoot);
		} 	
		
		//Setting context help
        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, "org.eclipse.team.svn.help.selectResourceContext"); //$NON-NLS-1$
		
		return composite;
	}
	
	public void setModelRoot(Object modelRoot) {
		this.modelRoot = modelRoot;
		this.repositoryTree.setModelRoot(this.modelRoot);
	}
	
	public IRepositoryResource getRepositoryResource() {
		return this.resource;
	}

}
