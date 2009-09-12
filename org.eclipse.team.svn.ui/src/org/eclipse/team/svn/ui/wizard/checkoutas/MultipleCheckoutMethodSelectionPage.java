/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.wizard.checkoutas;

import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.composite.DepthSelectionComposite;
import org.eclipse.team.svn.ui.composite.RevisionComposite;
import org.eclipse.team.svn.ui.utility.ArrayStructuredContentProvider;
import org.eclipse.team.svn.ui.wizard.AbstractVerifiedWizardPage;
import org.eclipse.team.svn.ui.wizard.CheckoutAsWizard;
import org.eclipse.ui.PlatformUI;

/**
 * Checkout method selection page if several repository resources were selected
 *
 * @author Sergiy Logvin
 */
public class MultipleCheckoutMethodSelectionPage extends AbstractVerifiedWizardPage {
	protected static final int FIND_PROJECTS = 0;
	protected static final int CHECKOUT_AS_FOLDER = 1;
	protected static final int CHECKOUT_AS_PROJECTS = 2;
	
	protected int checkoutType;
	protected DepthSelectionComposite recureDepthSelector;
	protected RevisionComposite revisionComposite;
	protected IRepositoryResource[] selectedResources;

	public MultipleCheckoutMethodSelectionPage(IRepositoryResource[] selectedResources) {
		super(MultipleCheckoutMethodSelectionPage.class.getName(), 
			SVNUIMessages.MultipleCheckoutMethodSelectionPage_Title, 
			SVNTeamUIPlugin.instance().getImageDescriptor("icons/wizards/newconnect.gif")); //$NON-NLS-1$
		this.selectedResources = selectedResources;
		this.checkoutType = MultipleCheckoutMethodSelectionPage.FIND_PROJECTS;
		
		this.setDescription(SVNUIMessages.MultipleCheckoutMethodSelectionPage_Description);
	}
	
	public boolean isFindProjectsSelected() {
		return this.checkoutType == MultipleCheckoutMethodSelectionPage.FIND_PROJECTS;
	}
	
	public boolean isCheckoutAsFolderSelected() {
		return this.checkoutType == MultipleCheckoutMethodSelectionPage.CHECKOUT_AS_FOLDER;
	}
	
	public int getRecureDepth() {
		return this.recureDepthSelector.getDepth();
	}
	
	public SVNRevision getSelectedRevision() {
		return this.revisionComposite.getSelectedRevision();
	}
	
	public Composite createControlImpl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		
		// GridLayout
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		
		// GridData
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);

		Button findProjectsButton = new Button(composite, SWT.RADIO);
		data = new GridData(GridData.FILL_HORIZONTAL);
		findProjectsButton.setLayoutData(data);
		findProjectsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				MultipleCheckoutMethodSelectionPage.this.checkoutType = MultipleCheckoutMethodSelectionPage.FIND_PROJECTS;
				MultipleCheckoutMethodSelectionPage.this.validateContent();
			}
		});
		findProjectsButton.setText(SVNUIMessages.MultipleCheckoutMethodSelectionPage_Find);
		findProjectsButton.setSelection(true);
		
		Button checkoutAsFolder = new Button(composite, SWT.RADIO);
		data = new GridData(GridData.FILL_HORIZONTAL);
		checkoutAsFolder.setLayoutData(data);
		checkoutAsFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				MultipleCheckoutMethodSelectionPage.this.checkoutType = MultipleCheckoutMethodSelectionPage.CHECKOUT_AS_FOLDER;
				MultipleCheckoutMethodSelectionPage.this.validateContent();
			}
		});
		checkoutAsFolder.setText(SVNUIMessages.MultipleCheckoutMethodSelectionPage_Folders);
		
		Button simpleCheckoutButton = new Button(composite, SWT.RADIO);
		data = new GridData(GridData.FILL_HORIZONTAL);
		simpleCheckoutButton.setLayoutData(data);
		simpleCheckoutButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				MultipleCheckoutMethodSelectionPage.this.checkoutType = MultipleCheckoutMethodSelectionPage.CHECKOUT_AS_PROJECTS;
				MultipleCheckoutMethodSelectionPage.this.validateContent();
			}
		});
		simpleCheckoutButton.setText(SVNUIMessages.MultipleCheckoutMethodSelectionPage_Projects);
		
		data = new GridData(GridData.FILL_BOTH);
		
		Table table = new Table(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER);
		TableLayout tLayout = new TableLayout();
		table.setLayout(tLayout);
		
		TableViewer tableViewer = new TableViewer(table);
		tableViewer.getTable().setLayoutData(data);
		
		int maxLength = FileUtility.getMaxStringLength(SVNUtility.asURLArray(this.selectedResources, false));
		this.initializeDialogUnits(composite);
		int width = this.convertWidthInCharsToPixels(maxLength + 12);
		
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);		
		ColumnLayoutData cData = null;
		if (width > CheckoutAsWizard.SIZING_WIZARD_WIDTH) {
			cData = new ColumnPixelData(width, true);
		}
		else {
			cData = new ColumnWeightData(100, true);
		}		
		tLayout.addColumnData(cData);
		
		tableViewer.setLabelProvider(new ITableLabelProvider() {
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}

			public String getColumnText(Object element, int columnIndex) {
				IRepositoryResource resource = (IRepositoryResource)element;
				return resource.getUrl();
			}

			public void addListener(ILabelProviderListener listener) {
			}
			public void dispose() {
			}
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}
			public void removeListener(ILabelProviderListener listener) {
			}
		});
		
		tableViewer.setContentProvider(new ArrayStructuredContentProvider());
		
		tableViewer.setInput(this.selectedResources);
		
		this.recureDepthSelector = new DepthSelectionComposite(composite, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.recureDepthSelector.setLayoutData(data);
		
		this.revisionComposite = new RevisionComposite(composite, this, false, new String[]{SVNUIMessages.RevisionComposite_Revision, SVNUIMessages.RevisionComposite_HeadRevision}, SVNRevision.HEAD, false);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.revisionComposite.setLayoutData(data);
		this.revisionComposite.setSelectedResource(this.selectedResources[0].getRoot());		
		
//		Setting context help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, "org.eclipse.team.svn.help.multiSelectionContext"); //$NON-NLS-1$
		
		return composite;
    }

}

