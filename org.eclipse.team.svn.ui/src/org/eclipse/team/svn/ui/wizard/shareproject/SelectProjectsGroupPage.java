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

package org.eclipse.team.svn.ui.wizard.shareproject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.team.svn.core.connector.SVNChangeStatus;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.utility.ColumnedViewerComparator;
import org.eclipse.team.svn.ui.wizard.AbstractVerifiedWizardPage;
import org.eclipse.ui.PlatformUI;

/**
 * Allows to select group of projects which will be shared at once
 * 
 * @author Alexander Gurov
 */
public class SelectProjectsGroupPage extends AbstractVerifiedWizardPage implements IResourceProvider {
	protected IProject []allProjects;
	protected Map projectGroups;
	protected int analysisDepth;
	protected int maxURLLength;
	
	protected String selectedGroup;
	
	protected Combo groupsCombo;
	protected Combo analysisDepthCombo;
	protected TableViewer viewer;

	public SelectProjectsGroupPage(IProject []projects) {
		super(
			SelectProjectsGroupPage.class.getName(), 
			SVNTeamUIPlugin.instance().getResource("SelectProjectsGroupPage.Title"), 
			SVNTeamUIPlugin.instance().getImageDescriptor("icons/wizards/newconnect.gif"));
		
		this.setDescription(SVNTeamUIPlugin.instance().getResource("SelectProjectsGroupPage.Description"));
		this.allProjects = projects;
		this.analysisDepth = 2;
		this.maxURLLength = 0;
		
		this.performAnalysis();
		this.selectedGroup = this.projectGroups.get(null) != null ? null : (String)this.projectGroups.keySet().iterator().next();
	}
	
	public boolean isGroupSelectionRequired() {
		return this.allProjects.length > 1;
	}
	
	public IResource []getResources() {
		List group = this.getProjectsGroup(this.selectedGroup);
		return (IProject [])group.toArray(new IProject[group.size()]);
	}

	protected void performAnalysis() {
		this.projectGroups = new LinkedHashMap();
		for (int i = 0; i < this.allProjects.length; i++) {
			SVNChangeStatus info = SVNUtility.getSVNInfoForNotConnected(this.allProjects[i]);
			if (info == null) {
				this.getProjectsGroup(null).add(this.allProjects[i]);
			}
			else {
				IPath url = new Path(SVNUtility.decodeURL(info.url));
				if (this.maxURLLength < url.segmentCount()) {
					this.maxURLLength = url.segmentCount();
				}
				if (url.segmentCount() > this.analysisDepth) {
					url = url.uptoSegment(this.analysisDepth);
				}
				this.getProjectsGroup(url.toString()).add(this.allProjects[i]);
			}
		}
		if (this.maxURLLength < this.analysisDepth) {
			this.analysisDepth = this.maxURLLength;
		}
	}
	
	protected List getProjectsGroup(Object key) {
		List retVal = (List)this.projectGroups.get(key);
		if (retVal == null) {
			this.projectGroups.put(key, retVal = new ArrayList());
		}
		return retVal;
	}
	
	protected void setGroupsComboItems() {
		ArrayList groups = new ArrayList(this.projectGroups.keySet());
		groups.remove(null);
		this.groupsCombo.setItems((String [])groups.toArray(new String[groups.size()]));
		this.groupsCombo.select(0);
		if (groups.size() > 0) {
			this.selectedGroup = this.groupsCombo.getItem(0);
		}
	}

	protected Composite createControlImpl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		this.initializeDialogUnits(parent);
		
		Label label = new Label(composite, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		data.heightHint = this.convertHeightInCharsToPixels(3);
		data.widthHint = 420;
		label.setLayoutData(data);
		label.setText(SVNTeamUIPlugin.instance().getResource("SelectProjectsGroupPage.Hint"));
		
		Button shareProjectsButton = new Button(composite, SWT.RADIO);
		data = new GridData();
		data.horizontalSpan = 2;
		shareProjectsButton.setLayoutData(data);
		shareProjectsButton.setText(SVNTeamUIPlugin.instance().getResource("SelectProjectsGroupPage.ShareNew"));
		boolean newEnabled = this.projectGroups.get(null) != null;
		shareProjectsButton.setEnabled(newEnabled);
		shareProjectsButton.setSelection(newEnabled);
		shareProjectsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (((Button)e.widget).getSelection()) {
					SelectProjectsGroupPage.this.groupsCombo.setEnabled(false);
					SelectProjectsGroupPage.this.analysisDepthCombo.setEnabled(false);
					SelectProjectsGroupPage.this.selectedGroup = null;
				}
				SelectProjectsGroupPage.this.viewer.setInput(SelectProjectsGroupPage.this.projectGroups);
			}
		});
		
		Button reconnectProjectsButton = new Button(composite, SWT.RADIO);
		data = new GridData();
		data.horizontalSpan = 2;
		reconnectProjectsButton.setLayoutData(data);
		reconnectProjectsButton.setEnabled(this.projectGroups.size() > 1 || this.projectGroups.get(null) == null);
		reconnectProjectsButton.setText(SVNTeamUIPlugin.instance().getResource("SelectProjectsGroupPage.Reconnect"));
		reconnectProjectsButton.setSelection(!newEnabled);
		reconnectProjectsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (((Button)e.widget).getSelection()) {
					SelectProjectsGroupPage.this.groupsCombo.setEnabled(true);
					SelectProjectsGroupPage.this.analysisDepthCombo.setEnabled(true);
					SelectProjectsGroupPage.this.selectedGroup = SelectProjectsGroupPage.this.groupsCombo.getItem(SelectProjectsGroupPage.this.groupsCombo.getSelectionIndex());
				}
				SelectProjectsGroupPage.this.viewer.setInput(SelectProjectsGroupPage.this.projectGroups);
			}
		});

		this.analysisDepthCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		data = new GridData();
		this.analysisDepthCombo.setLayoutData(data);
		this.analysisDepthCombo.setVisibleItemCount(10);
		this.analysisDepthCombo.setEnabled(!newEnabled);
		this.analysisDepthCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SelectProjectsGroupPage.this.analysisDepth = ((Combo)e.widget).getSelectionIndex() + 1;
				SelectProjectsGroupPage.this.performAnalysis();
				SelectProjectsGroupPage.this.setGroupsComboItems();
				SelectProjectsGroupPage.this.viewer.setInput(SelectProjectsGroupPage.this.projectGroups);
			}
		});
		String []allItems = new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
		String []items = allItems;
		if (this.maxURLLength < 10) {
			items = new String[this.maxURLLength];
			System.arraycopy(allItems, 0, items, 0, this.maxURLLength);
		}
		this.analysisDepthCombo.setItems(items);
		this.analysisDepthCombo.select(1);
		
		this.groupsCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.groupsCombo.setLayoutData(data);
		this.setGroupsComboItems();
		this.groupsCombo.setEnabled(!newEnabled);
		this.groupsCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Combo c = (Combo)e.widget;
				SelectProjectsGroupPage.this.selectedGroup = c.getItem(c.getSelectionIndex());
				SelectProjectsGroupPage.this.viewer.setInput(SelectProjectsGroupPage.this.projectGroups);
			}
		});
		
		label = new Label(composite, SWT.NONE);
		data = new GridData();
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		label.setText(SVNTeamUIPlugin.instance().getResource("SelectProjectsGroupPage.ProjectsList"));
		
		this.viewer = new TableViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		Table table = this.viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		data = new GridData(GridData.FILL_BOTH);
		data.widthHint = 420;
		data.horizontalSpan = 2;
		table.setLayoutData(data);
		
		this.viewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				List group = SelectProjectsGroupPage.this.getProjectsGroup(SelectProjectsGroupPage.this.selectedGroup);
				return group.toArray();
			}
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		this.viewer.setLabelProvider(new ITableLabelProvider() {
			public void removeListener(ILabelProviderListener listener) {
			}
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}
			public void dispose() {
			}
			public void addListener(ILabelProviderListener listener) {
			}
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}
			public String getColumnText(Object element, int columnIndex) {
				return ((IProject)element).getName();
			}
		});

		ColumnedViewerComparator comparator = new ColumnedViewerComparator(this.viewer) {
			public int compareImpl(Viewer viewer, Object row1, Object row2) {
				return ColumnedViewerComparator.compare(row1.toString(), row2.toString());
			}
		};
		
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setResizable(false);
		col.setText(SVNTeamUIPlugin.instance().getResource("SelectProjectsGroupPage.ProjectName"));
		col.addSelectionListener(comparator);
		TableLayout tLayout = new TableLayout();
		tLayout.addColumnData(new ColumnWeightData(100));
		table.setLayout(tLayout);
		
		this.viewer.getTable().setSortDirection(SWT.UP);
		this.viewer.getTable().setSortColumn(this.viewer.getTable().getColumn(0));
		
		this.selectedGroup = this.projectGroups.get(null) != null ? null : (String)this.projectGroups.keySet().iterator().next();
		this.viewer.setInput(this.projectGroups);
		
//		Setting context help
        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, "org.eclipse.team.svn.help.projectGroupContext");
		
		return composite;
	}


}
