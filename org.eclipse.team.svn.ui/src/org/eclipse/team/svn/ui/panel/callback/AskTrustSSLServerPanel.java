/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Gabor Liptak - Speedup Pattern's usage
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.callback;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.utility.PatternProvider;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;

/**
 * This panel allows us to ask user about trust to SSL server
 * 
 * @author Alexander Gurov
 */
public class AskTrustSSLServerPanel extends AbstractDialogPanel {
	protected String message;

	public AskTrustSSLServerPanel(String location, String message, boolean allowPermanently) {
        super(allowPermanently ? new String[] {SVNTeamUIPlugin.instance().getResource("AskTrustSSLServerPanel.Trust"), SVNTeamUIPlugin.instance().getResource("AskTrustSSLServerPanel.TrustAlways"), IDialogConstants.NO_LABEL} : new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL});
        this.dialogTitle = SVNTeamUIPlugin.instance().getResource("AskTrustSSLServerPanel.Title");
        this.dialogDescription = SVNTeamUIPlugin.instance().getResource("AskTrustSSLServerPanel.Description");
        this.defaultMessage = SVNTeamUIPlugin.instance().getResource("AskTrustSSLServerPanel.Message");
        this.defaultMessage = MessageFormat.format(this.defaultMessage, new String[] {location});
        this.message = message;
	}

    public Point getPrefferedSize() {
        return new Point(530, 250);
    }
    
	public void createControls(Composite parent) {
		String []baseLines = this.message.split("\n");
		final String [][]tableData = new String[baseLines.length][];
		for (int i = 0; i < baseLines.length; i++) {
			int idx = baseLines[i].indexOf(':');
			tableData[i] = new String[2];
			if (idx != -1) {
				int idx2 = baseLines[i].indexOf("https:");
				if (idx2 == -1) {
					tableData[i][0] = baseLines[i].substring(0, idx);
					tableData[i][1] = baseLines[i].substring(idx + 1).trim();
				}
				else {
					tableData[i][0] = baseLines[i].substring(0, idx2).trim();
					tableData[i][1] = baseLines[i].substring(idx2).trim();
					if (tableData[i][1].endsWith(":")) {
						tableData[i][1] = tableData[i][1].substring(0, tableData[i][1].length() - 1);
					}
				}
			}
			else {
				tableData[i][0] = baseLines[i];
				tableData[i][1] = "";
			}
		}
		
		GridData data = null;
		
		SashForm innerSashForm = new SashForm(parent, SWT.VERTICAL);
		data = new GridData(GridData.FILL_BOTH);
		innerSashForm.setLayoutData(data);
		
		final Table table = new Table(innerSashForm, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		data = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(data);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		TableLayout tLayout = new TableLayout();
		tLayout.addColumnData(new ColumnWeightData(20, true));
		tLayout.addColumnData(new ColumnWeightData(80, true));
		table.setLayout(tLayout);
		
		final Text text = new Text(innerSashForm, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		innerSashForm.setWeights(new int[] {25, 75});
		
		TableColumn col = new TableColumn(table, SWT.LEFT);
		col.setResizable(true);
		col.setText(SVNTeamUIPlugin.instance().getResource("AskTrustSSLServerPanel.Field"));
		col = new TableColumn(table, SWT.LEFT);
		col.setResizable(true);
		col.setText(SVNTeamUIPlugin.instance().getResource("AskTrustSSLServerPanel.Value"));

		TableViewer view = new TableViewer(table);
		view.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return tableData;
			}
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		view.setLabelProvider(new ITableLabelProvider() {
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}
			public String getColumnText(Object element, int columnIndex) {
				String []row = (String [])element;
				return row[columnIndex];
			}
			public void addListener(ILabelProviderListener listener) {
			}
			public void dispose() {
			}
			public boolean isLabelProperty(Object element, String property) {
				return true;
			}
			public void removeListener(ILabelProviderListener listener) {
			}
		});
		view.setInput(tableData);
		SelectionListener listener = new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				int idx = table.getSelectionIndex();
				if (idx > -1 && idx < tableData.length) {
					text.setText(PatternProvider.replaceAll(tableData[idx][1].trim(), ", ", "\n"));
				}
			}
		};
		table.addSelectionListener(listener);
		
		data = new GridData(GridData.FILL_BOTH);
		text.setLayoutData(data);
		text.setEditable(false);
		
		table.setSelection(0);
		listener.widgetSelected(null);
	}
	
	protected void saveChanges() {

	}

	protected void cancelChanges() {

	}

}
