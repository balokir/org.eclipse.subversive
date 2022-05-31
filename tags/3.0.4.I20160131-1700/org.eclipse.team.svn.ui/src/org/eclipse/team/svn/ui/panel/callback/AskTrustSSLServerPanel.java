/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
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

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
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
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.utility.ArrayStructuredContentProvider;

/**
 * This panel allows us to ask user about trust to SSL server
 * 
 * @author Alexander Gurov
 */
public class AskTrustSSLServerPanel extends AbstractDialogPanel {
	protected String message;

	public AskTrustSSLServerPanel(String location, String message, boolean allowPermanently) {
        super(allowPermanently ? new String[] {SVNUIMessages.AskTrustSSLServerPanel_Trust, SVNUIMessages.AskTrustSSLServerPanel_TrustAlways, IDialogConstants.NO_LABEL} : new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL});
        this.dialogTitle = SVNUIMessages.AskTrustSSLServerPanel_Title;
        this.dialogDescription = SVNUIMessages.AskTrustSSLServerPanel_Description;
        this.defaultMessage = SVNUIMessages.format(SVNUIMessages.AskTrustSSLServerPanel_Message, new String[] {location});
        this.message = message;
	}

    public Point getPrefferedSizeImpl() {
        return new Point(530, 250);
    }
    
	public void createControlsImpl(Composite parent) {
		String []baseLines = this.message.split("\n"); //$NON-NLS-1$
		ArrayList tData = new ArrayList();
		boolean infoMessagePart = false;
		String infoMessage = null;
		for (int i = 0; i < baseLines.length; i++) {
			int idx1 = baseLines[i].indexOf("https:"); //$NON-NLS-1$
			if (idx1 != -1) {
				String serverURL = baseLines[i].substring(idx1).trim();
				serverURL = serverURL.substring(0, serverURL.length() - 2);
				String []line = new String[2];
				line[0] = SVNUIMessages.AskTrustSSLServerPanel_Server;
				line[1] = serverURL;
				tData.add(line);
				infoMessagePart = true;
			}
			else if (infoMessagePart) {
				if (baseLines[i].endsWith(":")) {
					String []line = new String[2];
					line[0] = SVNUIMessages.AskTrustSSLServerPanel_Problems;
					line[1] = infoMessage;
					tData.add(line);
					infoMessagePart = false;
				}
				else {
					infoMessage = infoMessage == null ? baseLines[i] : (infoMessage + "\n" + baseLines[i]); //$NON-NLS-1$
				}
			}
			else {
				int idx = baseLines[i].indexOf(':');
				String []line = new String[2];
				line[0] = baseLines[i].substring(0, idx).replaceFirst("\\s*-\\s*", "").trim(); //$NON-NLS-1$
				line[1] = baseLines[i].substring(idx + 1).trim();
				tData.add(line);
			}
		}
		final String [][]tableData = (String [][])tData.toArray(new String[tData.size()][]);
		
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
		col.setText(SVNUIMessages.AskTrustSSLServerPanel_Field);
		col = new TableColumn(table, SWT.LEFT);
		col.setResizable(true);
		col.setText(SVNUIMessages.AskTrustSSLServerPanel_Value);

		TableViewer view = new TableViewer(table);
		view.setContentProvider(new ArrayStructuredContentProvider());
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
					text.setText(PatternProvider.replaceAll(tableData[idx][1].trim(), ", ", "\n")); //$NON-NLS-1$ //$NON-NLS-2$
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
	
	protected void saveChangesImpl() {
	}

	protected void cancelChangesImpl() {
	}

}