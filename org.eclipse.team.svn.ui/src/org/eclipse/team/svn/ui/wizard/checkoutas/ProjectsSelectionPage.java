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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.verifier.AbstractVerifier;
import org.eclipse.team.svn.ui.wizard.AbstractVerifiedWizardPage;

/**
 * Project selection page
 *
 * @author Sergiy Logvin
 */
public class ProjectsSelectionPage extends AbstractVerifiedWizardPage {
	
	protected List selectedProjects;
	protected IRepositoryResource []projects;
	protected CheckboxTableViewer listViewer;
	protected boolean respectHierarchy;
	protected boolean checkoutAsFolders;
	protected ProjectLocationSelectionPage locationPage;
	protected Button respectHierarchyButton;
	protected Button checkoutAsFolderButton;
	protected Button checkoutAsProjectButton;
	
	public ProjectsSelectionPage() {
		super(ProjectsSelectionPage.class.getName(), 
				"", 
				SVNTeamUIPlugin.instance().getImageDescriptor("icons/wizards/newconnect.gif"));
	}
	
	public boolean isCheckoutAsFoldersSelected() {
		return this.checkoutAsFolders;
	}
	
	public List getSelectedProjects() {
		return this.selectedProjects;
	}

	public boolean isRespectHierarchy() {
		return !this.checkoutAsFolders && this.respectHierarchy;
	}
	
	public void postInit(ProjectLocationSelectionPage locationPage, IRepositoryResource[] projects, ITableLabelProvider labelProvider, IStructuredContentProvider contentProvider) {
		this.locationPage = locationPage;
		this.projects = projects;
		this.listViewer.setLabelProvider(labelProvider);
		this.listViewer.setContentProvider(contentProvider);
		if (projects.length > 1) {
			this.setTitle(SVNUIMessages.ProjectsSelectionPage_Title_Multi);
			this.setDescription(SVNUIMessages.ProjectsSelectionPage_Description_Multi);
		}
		else {
			this.setTitle(SVNUIMessages.ProjectsSelectionPage_Title_Single);
			this.setDescription(SVNUIMessages.ProjectsSelectionPage_Description_Single);
		}
		
		this.checkoutAsFolderButton.setText(this.projects.length > 0 ? SVNUIMessages.ProjectsSelectionPage_CheckoutAsFolder_Multi : SVNUIMessages.ProjectsSelectionPage_CheckoutAsFolder_Single);
		this.checkoutAsProjectButton.setText(this.projects.length > 0 ? SVNUIMessages.ProjectsSelectionPage_CheckoutAsProject_Multi : SVNUIMessages.ProjectsSelectionPage_CheckoutAsProject_Single);
		
		this.listViewer.setInput(projects);
		this.listViewer.setAllChecked(true);
		this.refreshSelectedResult();
		this.validateContent();
	}
	
	public Composite createControlImpl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 12;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		Composite coTypeComposite = new Composite(composite, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		coTypeComposite.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		coTypeComposite.setLayoutData(data);
		
		this.checkoutAsFolderButton = new Button(coTypeComposite, SWT.RADIO);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.checkoutAsFolderButton.setLayoutData(data);
		this.checkoutAsFolderButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ProjectsSelectionPage.this.checkoutAsFolders = true;
				ProjectsSelectionPage.this.respectHierarchyButton.setEnabled(false);
				ProjectsSelectionPage.this.validateContent();
			}
		});
		this.checkoutAsFolderButton.setSelection(false);
		
		this.checkoutAsProjectButton = new Button(coTypeComposite, SWT.RADIO);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.checkoutAsProjectButton.setLayoutData(data);
		this.checkoutAsProjectButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ProjectsSelectionPage.this.checkoutAsFolders = false;
				ProjectsSelectionPage.this.respectHierarchyButton.setEnabled(true);
				ProjectsSelectionPage.this.validateContent();
			}
		});
		this.checkoutAsProjectButton.setSelection(true);
		
		this.listViewer = this.createViewer(composite);
		data = new GridData(GridData.FILL_BOTH);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		this.listViewer.getTable().setLayoutData(data);
		
		this.listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ProjectsSelectionPage.this.refreshSelectedResult();
				ProjectsSelectionPage.this.validateContent();
			}
		});
		layout = null;
		
		this.attachTo(this.listViewer.getTable(), new AbstractVerifier() {
			protected String getWarningMessage(Control input) {
				return null;
			}
			protected String getErrorMessage(Control input) {
				Object []elements = ProjectsSelectionPage.this.listViewer.getCheckedElements();
				return elements == null || elements.length == 0 ? SVNUIMessages.ProjectsSelectionPage_CheckoutAsProject_Verifier_Error : null;
			}
		});
						
		Composite bottomPart = new Composite(composite, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0; 
		layout.marginWidth = 0; 
		layout.numColumns = 2;
		bottomPart.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		bottomPart.setLayoutData(data);

		Composite tComposite = new Composite(bottomPart, SWT.LEFT);
		GridLayout gLayout = new GridLayout();
		gLayout.numColumns = 2;
		gLayout.marginWidth = gLayout.marginHeight = 0;
		tComposite.setLayout(gLayout);
		data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		tComposite.setData(data);

		Button selectButton = new Button(tComposite, SWT.PUSH);
		selectButton.setText(SVNUIMessages.Button_SelectAll);
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(selectButton);
		selectButton.setLayoutData(data);
		selectButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ProjectsSelectionPage.this.listViewer.setAllChecked(true);
				ProjectsSelectionPage.this.refreshSelectedResult();
				ProjectsSelectionPage.this.validateContent();
			}
		});
	
		Button deselectButton = new Button(tComposite, SWT.PUSH);
		deselectButton.setText(SVNUIMessages.Button_ClearSelection);
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(deselectButton);
		deselectButton.setLayoutData(data);
		deselectButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ProjectsSelectionPage.this.listViewer.setAllChecked(false);
				ProjectsSelectionPage.this.refreshSelectedResult();
				ProjectsSelectionPage.this.validateContent();
			}
		});
		
		this.respectHierarchyButton = new Button(bottomPart, SWT.CHECK);
		this.respectHierarchyButton.setText(SVNUIMessages.ProjectsSelectionPage_RespectHierarchy);
		this.respectHierarchyButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.FILL_HORIZONTAL));
		this.respectHierarchyButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ProjectsSelectionPage.this.respectHierarchy = ((Button)e.widget).getSelection();
				if (ProjectsSelectionPage.this.locationPage != null) {
					ProjectsSelectionPage.this.locationPage.setUseDefaultLocation(!ProjectsSelectionPage.this.respectHierarchy);
					ProjectsSelectionPage.this.locationPage.validateContent();
				}
				ProjectsSelectionPage.this.validateContent();
			}
		});
	
		return composite;
	}

	protected CheckboxTableViewer createViewer(Composite parent) {
		Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.CHECK | SWT.BORDER);
		TableLayout layout = new TableLayout();
		GridData data = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(data);
		table.setLayout(layout);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		CheckboxTableViewer viewer = new CheckboxTableViewer(table);
		
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setResizable(false);
		layout.addColumnData(new ColumnPixelData(20, false));
		
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(SVNUIMessages.ProjectsSelectionPage_RepositoryURL);
		layout.addColumnData(new ColumnPixelData(270, true));
		
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(SVNUIMessages.ProjectsSelectionPage_ProjectName);
		layout.addColumnData(new ColumnPixelData(150, true));
		
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(SVNUIMessages.ProjectsSelectionPage_ProjectType);
		layout.addColumnData(new ColumnPixelData(100, true));

		return viewer;
	}

	public void refreshSelectedResult() {
		if (this.projects != null) {
			ArrayList list = new ArrayList();
			for (int i = 0; i < this.projects.length; i++) {
				if (this.listViewer.getChecked(this.projects[i])) {
					list.add(this.projects[i]);
				}
			}
			this.selectedProjects = list;
		}
	}
	
}
