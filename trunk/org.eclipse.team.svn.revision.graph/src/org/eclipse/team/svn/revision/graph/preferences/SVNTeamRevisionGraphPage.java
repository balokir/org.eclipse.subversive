/*******************************************************************************
 * Copyright (c) 2005-2010 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo (Polarion Software) - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.revision.graph.preferences;

import java.io.File;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphMessages;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphPlugin;
import org.eclipse.team.svn.revision.graph.cache.RepositoryCacheInfo;
import org.eclipse.team.svn.revision.graph.cache.RepositoryCachesManager;
import org.eclipse.team.svn.ui.composite.PathSelectionComposite;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * Revision graph preferences page implementation
 * 
 * @author Igor Burilo
 */
public class SVNTeamRevisionGraphPage extends AbstractSVNRevisionGraphPreferencesPage implements IPropertyChangeListener {

	protected RepositoryCachesManager cachesManager;
	
	protected ListViewer cachesViewer;
	protected PathSelectionComposite pathSelectionComposite;
	protected Button removeButton;
	protected Button exportButton;
	protected Button importButton; 
	
	public SVNTeamRevisionGraphPage() {	
		this.cachesManager = SVNRevisionGraphPlugin.instance().getRepositoryCachesManager();
		this.cachesManager.addListener(this);		
	}	
	
	public void init(IWorkbench workbench) {
		this.setDescription(SVNRevisionGraphMessages.SVNTeamRevisionGraphPage_Description);
	}
	
	protected Control createContentsImpl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);		
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = 2;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));			
		
		this.createCachesViewer(composite);		
		this.createButtons(composite);						
		this.createDirectorySelection(composite);	

		//Setting context help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.eclipse.team.svn.help.revisionGraphPreferencesContext"); //$NON-NLS-1$
		
		return composite;
	}
	
	protected void createCachesViewer(Composite parent) {
		this.cachesViewer = new ListViewer(parent);
		List list = this.cachesViewer.getList();
		list.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		this.cachesViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				RepositoryCacheInfo cacheInfo = (RepositoryCacheInfo) element;
				return cacheInfo.getRepositoryName();
			}
		});
		
		this.cachesViewer.addSelectionChangedListener(new ISelectionChangedListener() {			
			public void selectionChanged(SelectionChangedEvent event) {
				SVNTeamRevisionGraphPage.this.selectionChanged();
			}
		});
		
		this.cachesViewer.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer, Object e1, Object e2) {
				String str1 = ((RepositoryCacheInfo) e1).getRepositoryName();
				String str2 = ((RepositoryCacheInfo) e2).getRepositoryName();
				return str1.compareToIgnoreCase(str2);
			}
		});	
		
		this.cachesViewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					SVNTeamRevisionGraphPage.this.removeCaches();
				}
			}
		});
	}
	
	protected void createButtons(Composite parent) {
		Composite buttons = new Composite(parent, SWT.NONE);
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		buttons.setLayout(layout);
		
		//remove
		this.removeButton = new Button(buttons, SWT.PUSH);
		this.removeButton.setText(SVNRevisionGraphMessages.SVNTeamRevisionGraphPage_Remove);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.widthHint = DefaultDialog.computeButtonWidth(this.removeButton);
		this.removeButton.setLayoutData(data);		
		this.removeButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				SVNTeamRevisionGraphPage.this.removeCaches();
			}
		});
		
		//export
		this.exportButton = new Button(buttons, SWT.PUSH);
		this.exportButton.setText(SVNRevisionGraphMessages.SVNTeamRevisionGraphPage_Export);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.widthHint = DefaultDialog.computeButtonWidth(this.exportButton);
		this.exportButton.setLayoutData(data);		
		this.exportButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				SVNTeamRevisionGraphPage.this.exportCaches();
			}
		});
		
		//import
		this.importButton = new Button(buttons, SWT.PUSH);
		this.importButton.setText(SVNRevisionGraphMessages.SVNTeamRevisionGraphPage_Import);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.widthHint = DefaultDialog.computeButtonWidth(this.importButton);
		this.importButton.setLayoutData(data);		
		this.importButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				SVNTeamRevisionGraphPage.this.importCaches();
			}
		});
	}

	protected void createDirectorySelection(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		group.setLayout(layout);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		group.setLayoutData(data);
		group.setText(SVNRevisionGraphMessages.SVNTeamRevisionGraphPage_Group_CacheDirectory);
		
		this.pathSelectionComposite = new PathSelectionComposite(
			SVNRevisionGraphMessages.SVNTeamRevisionGraphPage_Path_Label,
			SVNRevisionGraphMessages.SVNTeamRevisionGraphPage_Path_Field,
			SVNRevisionGraphMessages.SVNTeamRevisionGraphPage_Path_BrowseDialogTitle,
			SVNRevisionGraphMessages.SVNTeamRevisionGraphPage_Path_BrowseDialogDescription,
			true, group, this);
		data = new GridData(GridData.FILL_HORIZONTAL);		
		this.pathSelectionComposite.setLayoutData(data);
	}
	
	protected void initializeControls() {
		//remove old data		
		List list = this.cachesViewer.getList();
		if (list.getItemCount() > 0) {
			list.removeAll();	
		}
						
		//populate list
		RepositoryCacheInfo[] caches = this.cachesManager.getCaches();
		if (caches.length > 0) {
			this.cachesViewer.add(caches);	
		}						
		
		this.selectionChanged();		
	}	
	
	protected void loadDefaultValues(IPreferenceStore store) {
		String path = SVNRevisionGraphPreferences.getDefaultCacheString(store, SVNRevisionGraphPreferences.CACHE_DIRECTORY_NAME);
		this.pathSelectionComposite.setSelectedPath(path);
	}
	
	protected void loadValues(IPreferenceStore store) {		
		String path = SVNRevisionGraphPreferences.getCacheString(store, SVNRevisionGraphPreferences.CACHE_DIRECTORY_NAME);
		this.pathSelectionComposite.setSelectedPath(path);
	}

	protected void saveValues(IPreferenceStore store) {
		SVNRevisionGraphPreferences.setCacheString(store, SVNRevisionGraphPreferences.CACHE_DIRECTORY_NAME, this.pathSelectionComposite.getSelectedPath());
	}
	
	protected void selectionChanged() {
		IStructuredSelection selection = (IStructuredSelection) this.cachesViewer.getSelection();
		int selectionSize = selection.size();
		this.removeButton.setEnabled(selectionSize > 0);
		this.exportButton.setEnabled(selectionSize > 0);
	}

	protected RepositoryCacheInfo[] getSelectedCaches() {
		IStructuredSelection selection = (IStructuredSelection) this.cachesViewer.getSelection();
		Object[] objects = selection.toArray();
		RepositoryCacheInfo[] caches = new RepositoryCacheInfo[objects.length];
		for (int i = 0; i < objects.length; i ++) {
			caches[i] = (RepositoryCacheInfo) objects[i];
		}
		return caches;
	}
	
	protected void removeCaches() {		
		MessageDialog dlg = new MessageDialog(this.getShell(), 
				SVNRevisionGraphMessages.SVNTeamRevisionGraphPage_RemoveConfirm_Title,
				null, 
				SVNRevisionGraphMessages.SVNTeamRevisionGraphPage_RemoveConfirm_Description,
				MessageDialog.QUESTION, 
				new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL}, 
				0);  				
		if (dlg.open() == 0) {
			RepositoryCacheInfo[] caches = this.getSelectedCaches();			
			this.cachesManager.remove(caches);					
		}				
	}
	
	protected void exportCaches() {		
		DirectoryDialog fileDialog = new DirectoryDialog(this.getShell());
		fileDialog.setText(SVNRevisionGraphMessages.SVNTeamRevisionGraphPage_ExportTitle);
		fileDialog.setMessage(SVNRevisionGraphMessages.SVNTeamRevisionGraphPage_ExportDescription);
		String path = fileDialog.open();
		if (path != null) {
			File destinationFolder = new File(path);
			RepositoryCacheInfo[] caches = this.getSelectedCaches();			
			this.cachesManager.export(destinationFolder, caches);	
		}
	}
	
	protected void importCaches() {	
		DirectoryDialog fileDialog = new DirectoryDialog(this.getShell());
		fileDialog.setText(SVNRevisionGraphMessages.SVNTeamRevisionGraphPage_ImportTitle);
		fileDialog.setMessage(SVNRevisionGraphMessages.SVNTeamRevisionGraphPage_ImportDescription);
		String path = fileDialog.open();
		if (path != null) {
			File cacheData = new File(path);		
			this.cachesManager.importCache(cacheData);	
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	@Override
	public void dispose() {
		this.cachesManager.removeListener(this);
		
		super.dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {			
			public void run() {
				//react to any changes in caches
				SVNTeamRevisionGraphPage.this.initializeControls();
			}
		});
	}

}
