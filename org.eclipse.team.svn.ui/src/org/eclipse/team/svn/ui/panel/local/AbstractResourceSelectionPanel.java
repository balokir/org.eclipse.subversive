/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.local;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.core.ITeamStatus;
import org.eclipse.team.core.synchronize.ISyncInfoSetChangeEvent;
import org.eclipse.team.core.synchronize.ISyncInfoSetChangeListener;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.internal.core.subscribers.WorkingSetFilteredSyncInfoCollector;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.synchronize.SubscriberParticipantPage;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.internal.ui.synchronize.SynchronizePageConfiguration;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.ResourceSelectionComposite;
import org.eclipse.team.svn.ui.event.IResourceSelectionChangeListener;
import org.eclipse.team.svn.ui.event.ResourceSelectionChangedEvent;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.panel.BasePaneParticipant;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.svn.ui.verifier.AbstractVerifier;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePage;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ParticipantPagePane;

/**
 * Abstract resource selection panel implementation
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractResourceSelectionPanel extends AbstractDialogPanel {
	protected IResource []resources;
	protected CheckboxTableViewer tableViewer;
	protected ResourceSelectionComposite selectionComposite;
//	protected int subPathStart;	// common root length, unfortunately doesn't work with more than one repository location
	protected IResource[] userSelectedResources;

	//--- participant pane fields	
	protected boolean isParticipantPane;	
	protected BasePaneParticipant participant;
	protected ISynchronizePageConfiguration syncPageConfiguration;
	protected List<IResource> resourcesRemovedFromPane = new ArrayList<IResource>();	
	protected ISyncInfoSetChangeListener paneSyncInfoSetListener;
	
    public AbstractResourceSelectionPanel(IResource []resources, IResource[] userSelectedResources, String []buttonNames) {
        super(buttonNames);
		this.resources = resources;
		this.userSelectedResources = userSelectedResources;		
		
		this.isParticipantPane = SVNTeamPreferences.getBehaviourBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.BEHAVIOUR_SHOW_SELECTED_RESOURCES_IN_SYNC_PANE_NAME);
    }

	public IResource []getSelectedResources() {
		if (this.isParticipantPane) {
			SyncInfoSet syncInfoSet = this.getPaneSyncInfoSetToProcess();
			return syncInfoSet.getResources();		    		
		} else {
			return this.selectionComposite.getSelectedResources();			
		}				
	}

	public IResource []getNotSelectedResources() {
    	if (this.isParticipantPane) {    		
    		/*
    		 * As we can delete resources using 'Remove from View' action,
    		 * we need to process not selected resources.
    		 */    		
    		return this.resourcesRemovedFromPane.toArray(new IResource[0]);
    	} else {
    		return this.selectionComposite.getNotSelectedResources();
    	}    	
	}

    public Point getPrefferedSizeImpl() {
        return new Point(600, SWT.DEFAULT);
    }
    
    public void createControlsImpl(Composite parent) {
    	if (this.isParticipantPane) {
    		this.createPaneControls(parent);
    	} else {
        	this.selectionComposite = new ResourceSelectionComposite(parent, SWT.NONE, this.resources, false, this.userSelectedResources);
    		GridData data = new GridData(GridData.FILL_BOTH);
    		data.heightHint = 210;
    		this.selectionComposite.setLayoutData(data);
    		this.selectionComposite.addResourcesSelectionChangedListener(new IResourceSelectionChangeListener() {
    			public void resourcesSelectionChanged(ResourceSelectionChangedEvent event) {
    				AbstractResourceSelectionPanel.this.validateContent();
    			}
    		});
    		this.attachTo(this.selectionComposite, new AbstractVerifier() {
    			protected String getErrorMessage(Control input) {
    				IResource []selection = AbstractResourceSelectionPanel.this.getSelectedResources();
    				if (selection == null || selection.length == 0) {
    					return SVNTeamUIPlugin.instance().getResource("ResourceSelectionComposite.Verifier.Error");
    				}
    				return null;
    			}
    			protected String getWarningMessage(Control input) {
    				return null;
    			}
    		});
    		this.addContextMenu();	
    	}
    }
    
    protected void createPaneControls(Composite parent) {
    	this.participant = this.createPaneParticipant();
		
		Control paneControl = this.createChangesPage(parent);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 210;
        paneControl.setLayoutData(data);                        
        
        //sync view listener
        SyncInfoSet paneSyncInfoSet = this.getPaneSyncInfoSet();
        this.paneSyncInfoSetListener = new PaneSyncInfoSetListener();
        paneSyncInfoSet.addSyncSetChangedListener(this.paneSyncInfoSetListener);	        
                                          
        //add validator to pane
        this.attachTo(paneControl, new PaneVerifier());                        
    }
    
    protected Control createChangesPage(Composite composite) {
        this.syncPageConfiguration= this.participant.createPageConfiguration();
        ParticipantPagePane pagePane = new ParticipantPagePane(UIMonitorUtility.getShell(), true /* modal */, this.syncPageConfiguration, participant);        
        
        Control control = pagePane.createPartControl(composite);
        return control;
    }
    
    public void postInit() {
    	super.postInit();
    	if (this.isParticipantPane) {
    		this.expandPaneTree();
    	}
    }
    
    protected void expandPaneTree() {
        if (this.syncPageConfiguration != null) {
            final Viewer viewer= this.syncPageConfiguration.getPage().getViewer();
            if (viewer instanceof TreeViewer) {
            	try {
    	        	viewer.getControl().setRedraw(false);
    	            ((TreeViewer)viewer).expandAll();
            	} finally {
            		viewer.getControl().setRedraw(true);
            	}
            }
        }
    }
    
    public SyncInfoSet getPaneSyncInfoSetToProcess() {    
        final SyncInfoSet infos= new SyncInfoSet();
        if (this.syncPageConfiguration == null) {
            return this.participant.getSyncInfoSet();
        }
        
        final IDiffElement root = (ISynchronizeModelElement) this.syncPageConfiguration.getProperty(SynchronizePageConfiguration.P_MODEL);
        final IDiffElement[] elements= Utils.getDiffNodes(new IDiffElement [] { root });
        
        for (int i = 0; i < elements.length; i++) {
            if (elements[i] instanceof SyncInfoModelElement) {
                SyncInfo syncInfo = ((SyncInfoModelElement)elements[i]).getSyncInfo();                 	
                infos.add(syncInfo);
            }
        }  
        return infos;
    }       
    
	protected SyncInfoSet getPaneSyncInfoSet() {
		SyncInfoSet syncInfoSet = null;
		ISynchronizePage page = this.syncPageConfiguration.getPage();
		if (page instanceof SubscriberParticipantPage) {
        	WorkingSetFilteredSyncInfoCollector collector = ((SubscriberParticipantPage)page).getCollector();
        	syncInfoSet = collector.getWorkingSetSyncInfoSet();
		}
		return syncInfoSet;
	}
    
    public void dispose() {
    	super.dispose();
    	if (this.isParticipantPane) {
    		SyncInfoSet paneSyncInfoSet =  this.getPaneSyncInfoSet();
    		paneSyncInfoSet.removeSyncSetChangedListener(this.paneSyncInfoSetListener);
    	}  	
    }
	
    /*
	 * Pane validator
	 */
	private class PaneVerifier extends AbstractVerifier {
		
		protected String getErrorMessage(Control input) {
			/*
			 * As current validation may be caused by deletion of resources from sync view,
			 * then selected resources still contain deleted resources. 
			 * So we need to exclude them explicitly
			 */
			IResource []selectedResources = AbstractResourceSelectionPanel.this.getSelectedResources();
			List<IResource> resourcesToProcess = new ArrayList<IResource>();
			resourcesToProcess.addAll(Arrays.asList(selectedResources));
			if (!AbstractResourceSelectionPanel.this.resourcesRemovedFromPane.isEmpty()) {
				resourcesToProcess.removeAll(AbstractResourceSelectionPanel.this.resourcesRemovedFromPane);
			}
			
			if (resourcesToProcess.isEmpty()) {
				return SVNTeamUIPlugin.instance().getResource("ResourceSelectionComposite.Verifier.Error");
			}			
			return null;
		}
		
		protected String getWarningMessage(Control input) {
			return null;
		}	
	}
    
	/*
	 * Listens to changes in sync view for pane
	 */
	private class PaneSyncInfoSetListener implements ISyncInfoSetChangeListener {
			
		public void syncInfoChanged(ISyncInfoSetChangeEvent event, IProgressMonitor monitor) {					
			IResource[] removed = event.getRemovedResources();			
			if (removed.length > 0) {	 							 							 						
				AbstractResourceSelectionPanel.this.resourcesRemovedFromPane.addAll(Arrays.asList(removed));	 						
				UIMonitorUtility.getDisplay().syncExec(new Runnable() {
					public void run() {
						AbstractResourceSelectionPanel.this.validateContent();									
					}	 							
				});	 						
			}	 			
		}

		public void syncInfoSetErrors(SyncInfoSet set, ITeamStatus[] errors, IProgressMonitor monitor) {			 				
		}

		public void syncInfoSetReset(SyncInfoSet set, IProgressMonitor monitor) {					 					
		}	        	
	}
	
	protected void saveChangesImpl() {
    }

    protected void cancelChangesImpl() {
    }
    
    protected void addContextMenu() {
    }
    
    protected abstract BasePaneParticipant createPaneParticipant();
}
