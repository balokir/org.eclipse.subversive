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

package org.eclipse.team.svn.ui.panel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;

/**
 * List selection panel implementation
 * 
 * @author Alexander Gurov
 */
public class ListSelectionPanel extends AbstractDialogPanel {
	protected Object inputElement;
	protected IBaseLabelProvider labelProvider;
	protected IStructuredContentProvider contentProvider;
	protected List initialSelections;
	protected List initialGrayed;
	protected Object []resultSelections;
	protected boolean multipleColumns;
	
	protected CheckboxTableViewer listViewer;
	
	public ListSelectionPanel(Object input, IStructuredContentProvider contentProvider, IBaseLabelProvider labelProvider, String description, String defaultMessage) {
		this(input, contentProvider, labelProvider, description, defaultMessage, SVNTeamUIPlugin.instance().getResource("ListSelectionPanel.Title"), false);
	}
	
	public ListSelectionPanel(Object input, IStructuredContentProvider contentProvider, IBaseLabelProvider labelProvider, String description, String defaultMessage, String title, boolean multipleColumns) {
		super();
		this.initialSelections = Collections.EMPTY_LIST;
		this.initialGrayed = Collections.EMPTY_LIST;
		this.dialogTitle = title;
		this.inputElement = input;
		this.contentProvider = contentProvider;
		this.labelProvider = labelProvider;
		this.dialogDescription = description;
		this.defaultMessage = defaultMessage;
		this.multipleColumns = multipleColumns;
	}
	
	public void createControls(Composite parent) {
		this.listViewer = this.createViewer(parent);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 120;
		this.listViewer.getTable().setLayoutData(data);
		this.listViewer.setLabelProvider(this.labelProvider);
		this.listViewer.setContentProvider(this.contentProvider);
		this.listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ListSelectionPanel.this.validateContent();
			}
		});
		
		Composite tComposite = new Composite(parent, SWT.RIGHT);
		GridLayout gLayout = new GridLayout();
		gLayout.numColumns = 2;
		gLayout.marginWidth = 0;
		tComposite.setLayout(gLayout);
		data = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		tComposite.setData(data);
	
		Button selectButton = new Button(tComposite, SWT.PUSH);
		selectButton.setText(SVNTeamUIPlugin.instance().getResource("Button.SelectAll"));
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(selectButton);
		selectButton.setLayoutData(data);
		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
			    ListSelectionPanel.this.listViewer.setAllChecked(true);
				ListSelectionPanel.this.validateContent();
			}
		};
		selectButton.addSelectionListener(listener);
	
		Button deselectButton = new Button(tComposite, SWT.PUSH);
		deselectButton.setText(SVNTeamUIPlugin.instance().getResource("Button.ClearSelection"));
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(deselectButton);
		deselectButton.setLayoutData(data);
		listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ListSelectionPanel.this.listViewer.setAllChecked(false);
				ListSelectionPanel.this.validateContent();
			}
		};
		deselectButton.addSelectionListener(listener);
	}
	
	public void postInit() {
		this.listViewer.setInput(this.inputElement);
		for (Iterator it = this.initialSelections.iterator(); it.hasNext(); ) {
			this.listViewer.setChecked(it.next(), true);
		}
		for (Iterator it = this.initialGrayed.iterator(); it.hasNext(); ) {
			this.listViewer.setGrayed(it.next(), true);
		}
		super.postInit();
	}
	
	public Object []getResultSelections() {
		return this.resultSelections;
	}
	
	public Object []getInitialSelections() {
		return this.initialSelections.toArray();
	}
	
	public void setInitialSelections(Object []selectedElements) {
		this.initialSelections = Arrays.asList(selectedElements);
	}
	
	public void setInitialGrayed(Object[] grayedElements) {
		this.initialGrayed = Arrays.asList(grayedElements);
	}
	
	protected CheckboxTableViewer createViewer(Composite parent) {
		if (!this.multipleColumns) {
			return CheckboxTableViewer.newCheckList(parent, SWT.BORDER);
		}
		else {
			Table table = new Table(parent, SWT.CHECK | SWT.BORDER);
			table.setLinesVisible(true);
			GridData data = new GridData(GridData.FILL_BOTH);
			table.setLayoutData(data);
		
			TableLayout layout = new TableLayout();
			table.setLayout(layout);
			
			// resource name
			TableColumn col = new TableColumn(table, SWT.NONE);
			col.setResizable(true);
			col.setText(SVNTeamUIPlugin.instance().getResource("ListSelectionPanel.Resource"));
			layout.addColumnData(new ColumnWeightData(60, true));
		
			// local presentation
			col = new TableColumn(table, SWT.NONE);
			col.setResizable(true);
			col.setText(SVNTeamUIPlugin.instance().getResource("ListSelectionPanel.LocalPresentation"));
			layout.addColumnData(new ColumnWeightData(40, true));
			
			return new CheckboxTableViewer(table); 
		}
	}
	
	protected void saveChanges() {
		Object[] children = this.contentProvider.getElements(this.inputElement);
		if (children != null) {
			ArrayList list = new ArrayList();
			for (int i = 0; i < children.length; i++) {
				if (this.listViewer.getChecked(children[i])) {
					list.add(children[i]);
				}
			}
			this.resultSelections = list.toArray();
		}
	}

	protected void cancelChanges() {

	}

}
