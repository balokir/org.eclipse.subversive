/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.wizard.copymove;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.RepositoryTreeComposite;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.panel.common.RepositoryTreePanel;
import org.eclipse.team.svn.ui.panel.common.RepositoryTreePanel.ProjectRoot;
import org.eclipse.team.svn.ui.repository.model.RepositoryFile;
import org.eclipse.team.svn.ui.repository.model.RepositoryFolder;
import org.eclipse.team.svn.ui.repository.model.RepositoryLocationFilter;
import org.eclipse.team.svn.ui.repository.model.RepositoryResource;
import org.eclipse.team.svn.ui.repository.model.RepositoryRevisions;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;
import org.eclipse.team.svn.ui.verifier.ResourceNameVerifier;
import org.eclipse.team.svn.ui.wizard.AbstractVerifiedWizardPage;

/**
 * A page for selecting location to copy or move resource to.
 * 
 * @author Alexei Goncharov
 */
public class SelectDestinationPage extends AbstractVerifiedWizardPage {

	protected IRepositoryResource [] resources;
	protected RepositoryTreeComposite repositoryTree;
	protected Text newResourceName;
	
	public SelectDestinationPage(IRepositoryResource[] resources) {
		super(SelectDestinationPage.class.getName(), 
				SVNTeamUIPlugin.instance().getResource("RepositoryTreePanel.Description"),
				SVNTeamUIPlugin.instance().getImageDescriptor("icons/wizards/newconnect.gif"));
		this.setDescription(AbstractDialogPanel.makeToBeOperatetMessage(resources));
		this.resources = resources;
	}

	protected Composite createControlImpl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		this.repositoryTree = new RepositoryTreeComposite(composite, SWT.BORDER, false, new RepositoryTreePanel.ProjectRoot(this.resources[0]));
		ProjectRoot root = (ProjectRoot)this.repositoryTree.getRepositoryTreeViewer().getInput();
		this.repositoryTree.getRepositoryTreeViewer().setExpandedElements(new Object[] {root.getChildren(null)[0]});
		String url = this.resources[0].getRepositoryLocation().getRepositoryRootUrl();
		this.repositoryTree.setFilter(new RepositoryLocationFilter(url) {
			public boolean accept(Object obj) {
				if (obj instanceof RepositoryFile || 
					obj instanceof RepositoryRevisions || 
					obj instanceof RepositoryFolder && this.acceptYourself(((RepositoryFolder)obj).getRepositoryResource())) {
					return false;
				}
				return super.accept(obj);
			}
			
			boolean acceptYourself(IRepositoryResource resource) { 
				for (int i = 0; i < SelectDestinationPage.this.resources.length; i++) {
					if (resource.equals(SelectDestinationPage.this.resources[i])) {
						return true;
					}
				}
				return false;
			}
		});
		this.repositoryTree.setLayoutData(data);
		this.repositoryTree.getRepositoryTreeViewer().addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					SelectDestinationPage.this.validateContent();	
				}
			}
		);
		if (this.resources.length == 1) {
			data = new GridData();
			Label name = new Label(composite, SWT.NONE);
			name.setText("New resource name:");
			name.setLayoutData(data);
			data = new GridData();
			data.horizontalAlignment = SWT.FILL;
			this.newResourceName = new Text(composite, SWT.BORDER);
			this.newResourceName.setText(this.resources[0].getName());
			this.newResourceName.setLayoutData(data);
			CompositeVerifier verifier = new CompositeVerifier();
			verifier.add(new ResourceNameVerifier(name.getText(),
					CoreExtensionsManager.instance().getSVNConnectorFactory().getSVNAPIVersion() == ISVNConnectorFactory.APICompatibility.SVNAPI_1_5_x));
			verifier.add(new NonEmptyFieldVerifier(name.getText()));
			this.attachTo(this.newResourceName, verifier);
		}
		return composite;
	}
	
	public boolean isPageComplete() {
		return super.isPageComplete() && !this.repositoryTree.getRepositoryTreeViewer().getSelection().isEmpty();
	}
	
	public IRepositoryResource getDestination() {
		IStructuredSelection sel = (IStructuredSelection)this.repositoryTree.getRepositoryTreeViewer().getSelection();
		return ((RepositoryResource)sel.getFirstElement()).getRepositoryResource();
	}
	
	public String getNewResourceName() {
		return this.newResourceName.getText();
	}

}
