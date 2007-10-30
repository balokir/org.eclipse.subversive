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

package org.eclipse.team.svn.ui.wizard.checkoutas;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.RepositoryTreeComposite;
import org.eclipse.team.svn.ui.repository.model.IRepositoryContentFilter;
import org.eclipse.team.svn.ui.repository.model.RepositoryLocation;
import org.eclipse.team.svn.ui.repository.model.RepositoryResource;
import org.eclipse.team.svn.ui.wizard.AbstractVerifiedWizardPage;

/**
 * "Import from SVN" wizard resource selection page
 * 
 * @author Alexander Gurov
 */
public class SelectCheckoutResourcePage extends AbstractVerifiedWizardPage {
	protected RepositoryTreeComposite selectionComposite;
	protected IRepositoryResource []selectedResources;

	public SelectCheckoutResourcePage() {
		super(
			SelectCheckoutResourcePage.class.getName(), 
			SVNTeamUIPlugin.instance().getResource("SelectCheckoutResourcePage.Title"), 
			SVNTeamUIPlugin.instance().getImageDescriptor("icons/wizards/newconnect.gif"));
		this.setDescription(SVNTeamUIPlugin.instance().getResource("SelectCheckoutResourcePage.Description"));
	}
	
	public void setRepositoryLocation(IRepositoryLocation location) {
		this.selectionComposite.setModelRoot(new RepositoryLocation(location));
		this.setPageComplete(false);
	}
	
	public IRepositoryResource []getSelectedResources() {
		return this.selectedResources;
	}
	
	protected Composite createControlImpl(Composite parent) {
		GridData data = null;
		GridLayout layout = null;
		
		Composite composite = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 4;
		composite.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		this.selectionComposite = new RepositoryTreeComposite(composite, SWT.BORDER, true);
		data = new GridData(GridData.FILL_BOTH);
		this.selectionComposite.setLayoutData(data);
		this.selectionComposite.setFilter(new IRepositoryContentFilter() {
			public boolean accept(Object obj) {
				return !(obj instanceof IRepositoryFile);
			}
		});
		
		this.selectionComposite.getRepositoryTreeViewer().setAutoExpandLevel(1);
		
		this.selectionComposite.getRepositoryTreeViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)SelectCheckoutResourcePage.this.selectionComposite.getRepositoryTreeViewer().getSelection();
				ArrayList resources = new ArrayList();
				for (Iterator it = selection.iterator(); it.hasNext(); ) {
					Object item = it.next();
					if (item instanceof RepositoryResource) {
						resources.add(((RepositoryResource)item).getRepositoryResource());
					}
				}
				SelectCheckoutResourcePage.this.selectedResources = (IRepositoryResource [])resources.toArray(new IRepositoryResource[resources.size()]);
				SelectCheckoutResourcePage.this.setPageComplete(resources.size() > 0);
			}
		});
		
//		Setting context help
        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, "org.eclipse.team.svn.help.selectCheckResourcesContext");
		
		return composite;
	}

}
