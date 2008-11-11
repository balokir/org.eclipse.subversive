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

package org.eclipse.team.svn.ui.panel.participant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
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
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.svn.ui.verifier.AbstractVerifier;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePage;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ParticipantPagePane;

public class PaneParticipantHelper {

	protected boolean isParticipantPane;	
	protected BasePaneParticipant participant;
	protected ISynchronizePageConfiguration syncPageConfiguration;
	protected ParticipantPagePane pagePane;
	protected List<IResource> resourcesRemovedFromPane = new ArrayList<IResource>();	
	protected ISyncInfoSetChangeListener paneSyncInfoSetListener;				
	
	public PaneParticipantHelper() {
		this.isParticipantPane = PaneParticipantHelper.isParticipantPaneOption();
	}
	
	public void init(BasePaneParticipant participant) {		
		this.participant = participant;						
		this.syncPageConfiguration = this.participant.createPageConfiguration();							
	}	
	
	public void initListeners() {
		 //sync view listener
        SyncInfoSet paneSyncInfoSet = this.getPaneSyncInfoSet();
        this.paneSyncInfoSetListener = new PaneSyncInfoSetListener();
        paneSyncInfoSet.addSyncSetChangedListener(this.paneSyncInfoSetListener);	
	}
	
	public boolean isParticipantPane() {
		return this.isParticipantPane;		
	}
	
	public static boolean isParticipantPaneOption() {
		return SVNTeamPreferences.getBehaviourBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.BEHAVIOUR_SHOW_SELECTED_RESOURCES_IN_SYNC_PANE_NAME);		
	}
	
	public ISynchronizePageConfiguration getSyncPageConfiguration() {
		return this.syncPageConfiguration;
	}
	
	public BasePaneParticipant getParticipant() {
		return this.participant;
	}
	
	public IResource[] getSelectedResources() {
		SyncInfoSet syncInfoSet = this.getPaneSyncInfoSetToProcess();
		return syncInfoSet.getResources();
	}
	
	public IResource []getNotSelectedResources() {	        		
	    /*
		 * As we can delete resources using 'Remove from View' action,
		 * we need to process not selected resources.
		 */    		
		return this.resourcesRemovedFromPane.toArray(new IResource[0]);
	}
	
	public Control createChangesPage(Composite composite) {
        this.pagePane = new ParticipantPagePane(UIMonitorUtility.getShell(), true /* modal */, this.syncPageConfiguration, this.participant);
        Control control = this.pagePane.createPartControl(composite);
        return control;
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
	
    protected SyncInfoSet getPaneSyncInfoSetToProcess() {    
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
	
    public void dispose() {
    	if (this.syncPageConfiguration != null) {
    		SyncInfoSet paneSyncInfoSet =  this.getPaneSyncInfoSet();
    		paneSyncInfoSet.removeSyncSetChangedListener(this.paneSyncInfoSetListener);    		
    	}
		
		// Disposing of the page pane will dispose of the page and the configuration
		if (this.pagePane != null) {
			this.pagePane.dispose();
		}
		
		if (this.participant != null) {
			this.participant.dispose();
		}
    }
    
    public void expandPaneTree() {       
        Viewer viewer = this.syncPageConfiguration.getPage().getViewer();
        if (viewer instanceof TreeViewer) {
        	try {
	        	viewer.getControl().setRedraw(false);
	            ((TreeViewer)viewer).expandAll();
        	} finally {
        		viewer.getControl().setRedraw(true);
        	}
        }        
    }
    
	/*
	 * Pane validator
	 */
	public class PaneVerifier extends AbstractVerifier {					
		
		protected String getErrorMessage(Control input) {
			IResource[] resources = PaneParticipantHelper.this.getSelectedResources();
			if (resources.length == 0) {
				return SVNTeamUIPlugin.instance().getResource("ParticipantPagePane.Verifier.Error");
			}
			return null;
		}
		
		protected String getWarningMessage(Control input) {
			return null;
		}	
	}
	
	/*
	 * Listens to changes in sync view for pane.
	 * As we need to track not selected resources(e.g. removed from view), current listener
	 * tracks removed resources
	 */
	private class PaneSyncInfoSetListener implements ISyncInfoSetChangeListener {
			
		public void syncInfoChanged(ISyncInfoSetChangeEvent event, IProgressMonitor monitor) {					
			IResource[] removed = event.getRemovedResources();
			if (removed.length > 0) {	 							 							 						
				PaneParticipantHelper.this.resourcesRemovedFromPane.addAll(Arrays.asList(removed));								
			}	 					 						 					
		}

		public void syncInfoSetErrors(SyncInfoSet set, ITeamStatus[] errors, IProgressMonitor monitor) {			 				
		}

		public void syncInfoSetReset(SyncInfoSet set, IProgressMonitor monitor) {					 					
		}	        	
	}
}
