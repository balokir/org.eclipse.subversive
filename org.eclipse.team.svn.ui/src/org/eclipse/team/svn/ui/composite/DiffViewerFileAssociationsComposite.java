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

package org.eclipse.team.svn.ui.composite;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.operation.local.DiffViewerSettings;
import org.eclipse.team.svn.core.operation.local.DiffViewerSettings.IDiffViewerChangeListener;
import org.eclipse.team.svn.core.operation.local.DiffViewerSettings.ResourceSpecificParameterKindEnum;
import org.eclipse.team.svn.core.operation.local.DiffViewerSettings.ResourceSpecificParameters;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.common.DiffViewerVariablesPanel;
import org.eclipse.team.svn.ui.panel.common.EditFileAssociationsPanel;
import org.eclipse.team.svn.ui.utility.ColumnedViewerComparator;
import org.eclipse.team.svn.ui.verifier.IValidationManager;

/**
 * 
 * File associations for diff viewer
 * It associates either file extension or file mime type with external diff program
 * 
 * @author Igor Burilo
 */
public class DiffViewerFileAssociationsComposite extends Composite {

	protected static final int COLUMN_CHECKBOX = 0;
	protected static final int COLUMN_EXTENSION = 1;
	protected static final int COLUMN_DIFF_PATH = 2;
	protected static final int COLUMN_MERGE_PATH = 3;
	
	protected IValidationManager validationManager;	
	protected DiffViewerSettings diffSettings;
	
	protected CheckboxTableViewer tableViewer;
	protected Text diffParametersText;
	protected Text mergeParametersText;
	protected Button addButton;
	protected Button editButton;
	protected Button removeButton;
	
	public DiffViewerFileAssociationsComposite(Composite parent, IValidationManager validationManager) {
		super(parent, SWT.NONE);
		this.validationManager = validationManager;
		
		this.createControls();
	}

	public void initializeControls(DiffViewerSettings diffSettings) {
		this.diffSettings = diffSettings;
		
		this.diffParametersText.setText(""); //$NON-NLS-1$
		this.mergeParametersText.setText(""); //$NON-NLS-1$
		this.tableViewer.setInput(diffSettings);		
		
		//set checked
		ResourceSpecificParameters[] params = diffSettings.getResourceSpecificParameters();
		for (ResourceSpecificParameters param : params) {
			this.tableViewer.setChecked(param, param.isEnabled);
		}
	}
	
	protected void createControls() {
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		layout.numColumns = 2;
		GridData data = new GridData(GridData.FILL_BOTH);
		this.setLayout(layout);
		this.setLayoutData(data);
		
		Composite tableComposite = new Composite(this, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		layout.numColumns = 1;
		data = new GridData(GridData.FILL_BOTH);
		tableComposite.setLayout(layout);
		tableComposite.setLayoutData(data);
		
		this.createFileAssociationsTable(tableComposite);
		this.createParametersPreview(tableComposite);
		
		this.createButtonsControls(this);
		this.enableButtons();
	}
	
	protected void createParametersPreview(Composite parent) {
		//diff
		Group diffGroup = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		diffGroup.setLayout(layout);
		diffGroup.setLayoutData(data);
		diffGroup.setText(SVNUIMessages.DiffViewerExternalProgramComposite_DiffProgramArguments_Label);
				
		this.diffParametersText = new Text(diffGroup, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);		
		data.heightHint = DefaultDialog.convertHeightInCharsToPixels(this.diffParametersText, 5);
		this.diffParametersText.setLayoutData(data);
		this.diffParametersText.setBackground(this.diffParametersText.getBackground());
		this.diffParametersText.setEditable(false);
		
		//merge
		Group mergeGroup = new Group(parent, SWT.NONE);
		layout = new GridLayout();
		data = new GridData(GridData.FILL_HORIZONTAL);
		mergeGroup.setLayout(layout);
		mergeGroup.setLayoutData(data);
		mergeGroup.setText(SVNUIMessages.DiffViewerExternalProgramComposite_MergeProgramArguments_Label);
				
		this.mergeParametersText = new Text(mergeGroup, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);		
		data.heightHint = DefaultDialog.convertHeightInCharsToPixels(this.mergeParametersText, 5);
		this.mergeParametersText.setLayoutData(data);
		this.mergeParametersText.setBackground(this.mergeParametersText.getBackground());
		this.mergeParametersText.setEditable(false);				
	}

	protected void createFileAssociationsTable(Composite parent) {		
		Table table = new Table(parent, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnPixelData(20, false));
		layout.addColumnData(new ColumnWeightData(30, true));
		layout.addColumnData(new ColumnWeightData(40, true));
		layout.addColumnData(new ColumnWeightData(40, true));
		GridData data = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(data);
		table.setLayout(layout);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);				
		
		this.tableViewer = new CheckboxTableViewer(table);
		this.tableViewer.setUseHashlookup(true);
		//this.tableViewer.setColumnProperties(columnNames);
				
		this.tableViewer.setContentProvider(new FileAssociationsContentProvider());				
		this.tableViewer.setLabelProvider(new FileAssociationsLabelProvider());
		
		ColumnedViewerComparator comparator = new FileAssociationsComparator(this.tableViewer);				
		
		TableColumn column = new TableColumn(table, SWT.NONE);
		column.setResizable(false);		
		
		column = new TableColumn(table, SWT.NONE);
		column.setText(SVNUIMessages.DiffViewerFileAssociationsComposite_ExtensionMimeType_Column);
		column.addSelectionListener(comparator);
		
		column = new TableColumn(table, SWT.NONE);
		column.setText(SVNUIMessages.DiffViewerFileAssociationsComposite_DiffProgramPath_Column);
		column.addSelectionListener(comparator);
		
		column = new TableColumn(table, SWT.NONE);
		column.setText(SVNUIMessages.DiffViewerFileAssociationsComposite_MergeProgramPath_Column);
		column.addSelectionListener(comparator);
		
		this.tableViewer.setComparator(comparator);
		comparator.setColumnNumber(DiffViewerFileAssociationsComposite.COLUMN_EXTENSION);
		this.tableViewer.getTable().setSortColumn(this.tableViewer.getTable().getColumn(DiffViewerFileAssociationsComposite.COLUMN_EXTENSION));
		this.tableViewer.getTable().setSortDirection(SWT.UP);
		
		this.tableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {			
				ResourceSpecificParameters param = (ResourceSpecificParameters) event.getElement();
				param.isEnabled = event.getChecked();
			}			
		});
		
		this.tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {				
				ResourceSpecificParameters param = getSelectedResourceSpecificParameter();
				DiffViewerFileAssociationsComposite.this.editFileAssociations(param);
			}			
		});
		
		//selection listener
		this.tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				//init parameters control
				ResourceSpecificParameters param = DiffViewerFileAssociationsComposite.this.getSelectedResourceSpecificParameter();
				if (param != null) {
					String diffParamsStr = param.params.diffParamatersString;
					diffParamsStr = diffParamsStr != null ? diffParamsStr : ""; //$NON-NLS-1$
					DiffViewerFileAssociationsComposite.this.diffParametersText.setText(diffParamsStr);					
					
					String mergeParamsStr = param.params.mergeParamatersString;
					mergeParamsStr = mergeParamsStr != null ? mergeParamsStr : "";  //$NON-NLS-1$
					DiffViewerFileAssociationsComposite.this.mergeParametersText.setText(mergeParamsStr);					
				}
				
				DiffViewerFileAssociationsComposite.this.enableButtons();								
			}			
		});					
	}
	
	protected void enableButtons() {
		boolean hasSelection = this.getSelectedResourceSpecificParameter() != null;
		this.editButton.setEnabled(hasSelection);
		this.removeButton.setEnabled(hasSelection);
	}
	
	protected void createButtonsControls(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = layout.marginHeight = 0;
		
		GridData data = new GridData();
		data.verticalAlignment = SWT.TOP;
		composite.setLayout(layout);
		composite.setLayoutData(data);
		
		this.addButton = new Button(composite, SWT.PUSH);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = DefaultDialog.computeButtonWidth(this.addButton);
		this.addButton.setLayoutData(data);
		this.addButton.setText(SVNUIMessages.DiffViewerFileAssociationsComposite_Add_Button);
		
		this.editButton = new Button(composite, SWT.PUSH);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = DefaultDialog.computeButtonWidth(this.editButton);
		this.editButton.setLayoutData(data);
		this.editButton.setText(SVNUIMessages.DiffViewerFileAssociationsComposite_Edit_Button);
		
		this.removeButton = new Button(composite, SWT.PUSH);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = DefaultDialog.computeButtonWidth(this.removeButton);
		this.removeButton.setLayoutData(data);
		this.removeButton.setText(SVNUIMessages.DiffViewerFileAssociationsComposite_Remove_Button);
		
		Button variablesButton = new Button(composite, SWT.PUSH);		
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = DefaultDialog.computeButtonWidth(variablesButton);
		variablesButton.setLayoutData(data);
		variablesButton.setText(SVNUIMessages.DiffViewerExternalProgramComposite_Variables_Button);
		
		//handlers
		
		this.addButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {			
				EditFileAssociationsPanel editPanel = new EditFileAssociationsPanel(null, DiffViewerFileAssociationsComposite.this.diffSettings);
				DefaultDialog dialog = new DefaultDialog(DiffViewerFileAssociationsComposite.this.getShell(), editPanel);
				if (dialog.open() == 0) {
					ResourceSpecificParameters resourceParams = editPanel.getResourceSpecificParameters();					
					DiffViewerFileAssociationsComposite.this.diffSettings.addResourceSpecificParameters(resourceParams);
				}
			}			
		});
		
		this.editButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {		
				ResourceSpecificParameters resourceParams = DiffViewerFileAssociationsComposite.this.getSelectedResourceSpecificParameter();
				DiffViewerFileAssociationsComposite.this.editFileAssociations(resourceParams);									
			}			
		});
		
		this.removeButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {			
				ResourceSpecificParameters resourceParams = DiffViewerFileAssociationsComposite.this.getSelectedResourceSpecificParameter();
				if (resourceParams != null) {					
					DiffViewerFileAssociationsComposite.this.diffSettings.removeResourceSpecificParameters(resourceParams);
				}					
			}			
		});
		
		variablesButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				DiffViewerVariablesPanel panel = new DiffViewerVariablesPanel();
				DefaultDialog dlg = new DefaultDialog(DiffViewerFileAssociationsComposite.this.getShell(), panel);
				dlg.open();
			}				
		});
	}
	
	protected void editFileAssociations(ResourceSpecificParameters resourceParams) {
		if (resourceParams != null) {
			EditFileAssociationsPanel editPanel = new EditFileAssociationsPanel(resourceParams, DiffViewerFileAssociationsComposite.this.diffSettings);
			DefaultDialog dialog = new DefaultDialog(DiffViewerFileAssociationsComposite.this.getShell(), editPanel);
			if (dialog.open() == 0) {
				resourceParams = editPanel.getResourceSpecificParameters();					
				DiffViewerFileAssociationsComposite.this.diffSettings.updateResourceSpecificParameters(resourceParams);
			}
		}		
	}
	
	protected ResourceSpecificParameters getSelectedResourceSpecificParameter() {
		ResourceSpecificParameters resourceParams = null;
	
		ISelection sel =  this.tableViewer.getSelection();
		if (!sel.isEmpty() && sel instanceof IStructuredSelection) {					
			IStructuredSelection selection = (IStructuredSelection) sel;
			resourceParams = (ResourceSpecificParameters) selection.getFirstElement();
		}
		return resourceParams;			
	}
	
	/*
	 * Label provider for file associations table
	 */
	protected class FileAssociationsLabelProvider extends LabelProvider implements ITableLabelProvider {

		public String getColumnText(Object element, int columnIndex) {
			String res = ""; //$NON-NLS-1$
			ResourceSpecificParameters param = (ResourceSpecificParameters) element;
			switch (columnIndex) {
				case DiffViewerFileAssociationsComposite.COLUMN_CHECKBOX:
					res = ""; //$NON-NLS-1$
					break;
				case DiffViewerFileAssociationsComposite.COLUMN_EXTENSION:
					res = param.kind.formatKindValue();
					break;
				case DiffViewerFileAssociationsComposite.COLUMN_DIFF_PATH:
					res = param.params.diffProgramPath;
					break;
				case DiffViewerFileAssociationsComposite.COLUMN_MERGE_PATH:
					res = param.params.mergeProgramPath;
					break;	
			}
			return res;
		}
		
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		
	}
	
	/**
	 * File Associations table comparator 
	 * 
	 */
	protected class FileAssociationsComparator extends  ColumnedViewerComparator {
		
		public FileAssociationsComparator(Viewer basedOn) {
			super(basedOn);
		}

		public int compareImpl(Viewer viewer, Object row1, Object row2) {
			ResourceSpecificParameters r1 = (ResourceSpecificParameters) row1;
			ResourceSpecificParameters r2 = (ResourceSpecificParameters) row2;
			
			if (this.column == DiffViewerFileAssociationsComposite.COLUMN_EXTENSION) {
				ResourceSpecificParameterKindEnum kindEnum1 = r1.kind.kindEnum;
				ResourceSpecificParameterKindEnum kindEnum2 = r2.kind.kindEnum;
				
				if (kindEnum1.equals(kindEnum2)) {
					return ColumnedViewerComparator.compare(r1.kind.kindValue, r2.kind.kindValue);
				} else {
					return kindEnum2.compareTo(kindEnum1);
				}				 
			}
			if (this.column == DiffViewerFileAssociationsComposite.COLUMN_DIFF_PATH) {
				String path1 = r1.params.diffProgramPath;
				String path2 = r2.params.diffProgramPath;
				return ColumnedViewerComparator.compare(path1, path2);
			}
			if (this.column == DiffViewerFileAssociationsComposite.COLUMN_MERGE_PATH) {
				String path1 = r1.params.mergeProgramPath;
				String path2 = r2.params.mergeProgramPath;
				return ColumnedViewerComparator.compare(path1, path2);
			}
			
			return 0;
		}			
	};
	
	/*
	 * Content provider for file associations table
	 */
	protected class FileAssociationsContentProvider implements IStructuredContentProvider, IDiffViewerChangeListener {
		
		public Object[] getElements(Object inputElement) {
			DiffViewerSettings diffSettings = (DiffViewerSettings) inputElement;
			return diffSettings.getResourceSpecificParameters();
		}
		
		public void addResourceSpecificParameters(ResourceSpecificParameters params) {
			DiffViewerFileAssociationsComposite.this.tableViewer.add(params);
			DiffViewerFileAssociationsComposite.this.tableViewer.setChecked(params, params.isEnabled);
		}

		public void changeResourceSpecificParameters(ResourceSpecificParameters params) {
			DiffViewerFileAssociationsComposite.this.tableViewer.update(params, null);
			//update parametersText
			DiffViewerFileAssociationsComposite.this.diffParametersText.setText(params.params.diffParamatersString);
			DiffViewerFileAssociationsComposite.this.mergeParametersText.setText(params.params.mergeParamatersString);
		}

		public void removeResourceSpecificParameters(ResourceSpecificParameters params) {
			DiffViewerFileAssociationsComposite.this.tableViewer.remove(params);
			//clear parametersText
			DiffViewerFileAssociationsComposite.this.diffParametersText.setText(""); //$NON-NLS-1$
			DiffViewerFileAssociationsComposite.this.mergeParametersText.setText(""); //$NON-NLS-1$
		}
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput != null) {
				((DiffViewerSettings) newInput).addChangeListener(this);
			}
			if (oldInput != null) {
				((DiffViewerSettings) oldInput).removeChangeListener(this);
			}
		}			
		
		public void dispose() {
			DiffViewerFileAssociationsComposite.this.diffSettings.removeChangeListener(this);
		}
	}

}
