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

package org.eclipse.team.svn.ui.history;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.events.IResourceStatesListener;
import org.eclipse.team.svn.core.resource.events.ResourceStatesChangedEvent;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.repository.model.RepositoryLocation;
import org.eclipse.team.svn.ui.repository.model.RepositoryResource;
import org.eclipse.team.ui.history.HistoryPage;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * Generic HistoryView page
 * 
 * @author Alexander Gurov
 */
public class SVNHistoryPage extends HistoryPage implements IViewInfoProvider, IResourceStatesListener {
	public static final String VIEW_ID = "org.eclipse.team.ui.GenericHistoryView";
	
	protected HistoryViewImpl viewImpl;
	
	public SVNHistoryPage(Object input) {
		SVNRemoteStorage.instance().addResourceStatesListener(ResourceStatesChangedEvent.class, this);
	}
	
	public void resourcesStateChanged(ResourceStatesChangedEvent event) {
		if (this.getResource() == null) {
			return;
		}
		if (event.contains(this.getResource()) || event.contains(this.getResource().getProject())) {
			if (!this.getResource().exists() || !FileUtility.isConnected(this.getResource())) {
				this.disconnectView();
			}
			else {
				ILocalResource local = SVNRemoteStorage.instance().asLocalResource(this.getResource());
				if (local == null || IStateFilter.SF_UNVERSIONED.accept(local)) {
					this.disconnectView();
				}
			}
		}
	}

	public void dispose() {
		SVNRemoteStorage.instance().removeResourceStatesListener(ResourceStatesChangedEvent.class, this);
		if (this.viewImpl != null) {
			this.viewImpl.dispose();
		}
		super.dispose();
	}

	public int getOptions() {
		if (this.viewImpl != null) {
			return this.viewImpl.getOptions();
		}
		return 0;
	}
	
	public void setOptions(int mask, int values) {
		if (this.viewImpl != null) {
			this.viewImpl.setOptions(mask, values);
		}
	}
	
	public void setCompareWith(ILocalResource compareWith) {
		if (this.viewImpl != null) {
			this.viewImpl.setCompareWith(compareWith);
		}
	}
	
	public IResource getResource() {
		if (this.viewImpl != null) {
			return this.viewImpl.getResource();
		}
		return null;
	}
	
	public IRepositoryResource getRepositoryResource() {
		if (this.viewImpl != null) {
			return this.viewImpl.getRepositoryResource();
		}
		return null;
	}

	public boolean inputSet() {
		if (this.viewImpl == null) {
			return SVNHistoryPage.isValidData(this.getInput());
		}
		if (this.getInput() instanceof IResource) {
			this.viewImpl.showHistory((IResource)this.getInput(), true);
			return true;
		}
		else if (this.getInput() instanceof IRepositoryResource) {
			this.viewImpl.showHistory((IRepositoryResource)this.getInput(), true);
			return true;
		}
		else if (this.getInput() instanceof RepositoryResource) {
			this.viewImpl.showHistory(((RepositoryResource)this.getInput()).getRepositoryResource(), true);
			return true;
		}
		else if (this.getInput() instanceof RepositoryLocation) {
			this.viewImpl.showHistory(((RepositoryLocation)this.getInput()).getRepositoryResource(), true);
			return true;
		}
		return false;
	}
	
	public void createControl(Composite parent) {
		this.viewImpl = new HistoryViewImpl(null, null, this);
	    IActionBars actionBars = this.getActionBars();
	    IToolBarManager tbm = actionBars.getToolBarManager();
        tbm.add(new Separator("MainGroup"));
        tbm.add(new Separator("SecondGroup"));
        
		this.viewImpl.createPartControl(parent);
		
		this.inputSet();
	}

	public Control getControl() {
		if (this.viewImpl != null) {
			return this.viewImpl.getControl();
		}
		return null;
	}

	public void setFocus() {

	}

	public String getDescription() {
		if (this.viewImpl != null) {
			return this.viewImpl.getResourceLabel();
		}
		return null;
	}

	public String getName() {
		if (this.viewImpl != null) {
			if (this.viewImpl.getResource() != null) {
				return this.viewImpl.getResource().getFullPath().toString().substring(1);
			}
			if (this.viewImpl.getRepositoryResource() != null) {
				return this.viewImpl.getRepositoryResource().getUrl();
			}
		}
		return null;
	}

	public boolean isValidInput(Object object) {
		return SVNHistoryPage.isValidData(object);
	}

	public static boolean isValidData(Object object) {
		if (object instanceof IResource && FileUtility.isConnected((IResource)object)){
			ILocalResource local = SVNRemoteStorage.instance().asLocalResource((IResource)object);
			boolean flag = IStateFilter.SF_NOTONREPOSITORY.accept(local);
			if (flag) {
				return false;
			}
			else {
				return true;
			}
		}
		return 
			object instanceof IRepositoryResource || 
			object instanceof RepositoryResource || object instanceof RepositoryLocation;
	}

	public void refresh() {
		if (this.viewImpl != null) {
			this.viewImpl.refresh();
		}
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

	public IActionBars getActionBars() {
		return this.getHistoryPageSite().getWorkbenchPageSite().getActionBars();
	}

	public IWorkbenchPartSite getPartSite() {
		IWorkbenchPart part = this.getHistoryPageSite().getPart();
		if (part == null) {
			return null;
		}
		IWorkbenchPartSite site = part.getSite();
		while (site == null) {
			try {
				// await while site is initialized, see IWorkbenchPart.getSite() documentation
				Thread.sleep(100);
			}
			catch (InterruptedException ex) {
				break;
			}
			site = part.getSite();
		}
		return site;
	}

	public void setDescription(String description) {
//		IWorkbenchPart part = this.getHistoryPageSite().getPart();
//		if (part instanceof GenericHistoryView) {
//			((GenericHistoryView)part).updateContentDescription(description);
//		}
	}

	public void selectRevision(long revision) {
		if (this.viewImpl != null) {
			this.viewImpl.selectRevision(revision);
		}
	}
	
    protected void disconnectView() {
    	if (this.viewImpl != null) {
    		this.getSite().getShell().getDisplay().syncExec(new Runnable() {
    			public void run() {
    				SVNHistoryPage.this.viewImpl.showHistory((IResource)null, false);
    			}
    		});
    	}
    }
    
}
