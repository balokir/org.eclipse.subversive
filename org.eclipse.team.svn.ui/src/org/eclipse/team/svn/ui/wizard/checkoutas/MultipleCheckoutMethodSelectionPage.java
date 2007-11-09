/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
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
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
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
	protected boolean checkoutRecursively;
	protected boolean ignoreExternals;
	protected IRepositoryResource[] selectedResources;

	public MultipleCheckoutMethodSelectionPage(IRepositoryResource[] selectedResources) {
		super(MultipleCheckoutMethodSelectionPage.class.getName(), 
			SVNTeamUIPlugin.instance().getResource("MultipleCheckoutMethodSelectionPage.Title"), 
			SVNTeamUIPlugin.instance().getImageDescriptor("icons/wizards/newconnect.gif"));
		this.selectedResources = selectedResources;
		this.checkoutType = MultipleCheckoutMethodSelectionPage.FIND_PROJECTS;
		this.checkoutRecursively = true;
		this.ignoreExternals = false;
		
		this.setDescription(SVNTeamUIPlugin.instance().getResource("MultipleCheckoutMethodSelectionPage.Description"));
	}
	
	public boolean isFindProjectsSelected() {
		return this.checkoutType == MultipleCheckoutMethodSelectionPage.FIND_PROJECTS;
	}
	
	public boolean isCheckoutAsFolderSelected() {
		return this.checkoutType == MultipleCheckoutMethodSelectionPage.CHECKOUT_AS_FOLDER;
	}
	
	public boolean isCheckoutRecursivelySelected() {
		return this.checkoutRecursively;
	}
	
	public boolean isIgnoreExternalsSelected() {
		return this.ignoreExternals;
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
		findProjectsButton.setText(SVNTeamUIPlugin.instance().getResource("MultipleCheckoutMethodSelectionPage.Find"));
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
		checkoutAsFolder.setText(SVNTeamUIPlugin.instance().getResource("MultipleCheckoutMethodSelectionPage.Folders"));
		
		Button simpleCheckoutButton = new Button(composite, SWT.RADIO);
		data = new GridData(GridData.FILL_HORIZONTAL);
		simpleCheckoutButton.setLayoutData(data);
		simpleCheckoutButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				MultipleCheckoutMethodSelectionPage.this.checkoutType = MultipleCheckoutMethodSelectionPage.CHECKOUT_AS_PROJECTS;
				MultipleCheckoutMethodSelectionPage.this.validateContent();
			}
		});
		simpleCheckoutButton.setText(SVNTeamUIPlugin.instance().getResource("MultipleCheckoutMethodSelectionPage.Projects"));
		
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
		
		tableViewer.setContentProvider(new IStructuredContentProvider() {
			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			public Object[] getElements(Object inputElement) {
				return MultipleCheckoutMethodSelectionPage.this.selectedResources;
			}
		});
		
		tableViewer.setInput(this.selectedResources);
		
		Label separator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		separator.setVisible(false);
		
		Button checkoutRecursivelyCheckbox = new Button (composite, SWT.CHECK);
		data = new GridData(GridData.FILL_HORIZONTAL);
		checkoutRecursivelyCheckbox.setLayoutData(data);
		checkoutRecursivelyCheckbox.setSelection(true);
		checkoutRecursivelyCheckbox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				MultipleCheckoutMethodSelectionPage.this.checkoutRecursively = ((Button)e.widget).getSelection();
			}
		});
		checkoutRecursivelyCheckbox.setText(SVNTeamUIPlugin.instance().getResource("MultipleCheckoutMethodSelectionPage.Recursively"));
		
		Button ingnoreExternalsCheckbox = new Button (composite, SWT.CHECK);
		data = new GridData(GridData.FILL_HORIZONTAL);
		ingnoreExternalsCheckbox.setLayoutData(data);
		ingnoreExternalsCheckbox.setSelection(false);
		ingnoreExternalsCheckbox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				MultipleCheckoutMethodSelectionPage.this.ignoreExternals = ((Button)e.widget).getSelection();
			}
		});
		ingnoreExternalsCheckbox.setText(SVNTeamUIPlugin.instance().getResource("MultipleCheckoutMethodSelectionPage.Externals"));
		
//		Setting context help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, "org.eclipse.team.svn.help.multiSelectionContext");
		
		return composite;
    }

}

