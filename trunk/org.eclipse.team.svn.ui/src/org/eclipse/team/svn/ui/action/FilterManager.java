/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.action;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.events.IResourceStatesListener;
import org.eclipse.team.svn.core.resource.events.ResourceStatesChangedEvent;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;

/**
 * Manager for the resource state filters
 * 
 * @author Sergiy Logvin
 */
public class FilterManager implements IPropertyChangeListener, IResourceStatesListener {
	private static FilterManager instance = null;
	protected Map filters2condition;
	protected Map recursiveFilters2condition;
	
	protected MapChecker flatChecker;
	protected MapChecker recursiveChecker;
	
	protected boolean dirty;
	
	public static synchronized FilterManager instance() {
		if (FilterManager.instance == null) {
			FilterManager.instance = new FilterManager();
		}
		return FilterManager.instance;
	}
	
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_PRECISE_ENABLEMENTS_NAME)) ||
			event.getProperty().equals(SVNTeamPreferences.fullCoreName(SVNTeamPreferences.CORE_SVNCONNECTOR_NAME))) {
			this.clear();
		}
	}
	
	public void resourcesStateChanged(ResourceStatesChangedEvent event) {
		this.clear();
	}
	
	public void clear() {
		this.dirty = true;
	}
	
	public boolean checkForResourcesPresenceRecursive(IResource []selectedResources, IStateFilter stateFilter) {
		return this.checkForResourcesPresence(selectedResources, stateFilter, true);
	}
	
	public boolean checkForResourcesPresence(IResource []selectedResources, IStateFilter stateFilter, boolean recursive) {
		boolean computeDeep = SVNTeamPreferences.getDecorationBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.DECORATION_PRECISE_ENABLEMENTS_NAME);
		if (this.dirty) {
			this.dirty = false;
			if (!this.connectedToSVN(selectedResources)) {
				this.flatChecker.clearFilters();
				this.flatChecker.checkDisallowed();
				this.recursiveChecker.clearFilters();
				this.recursiveChecker.checkDisallowed();
				return false;
			}
			if (this.filters2condition.size() > 0) {
				this.flatChecker.clearFilters();
				FileUtility.checkForResourcesPresence(selectedResources, this.flatChecker, IResource.DEPTH_ZERO);
				this.flatChecker.checkDisallowed();
			}
			if (computeDeep) {
				if (this.recursiveFilters2condition.size() > 0) {
					this.recursiveChecker.clearFilters();
					FileUtility.checkForResourcesPresence(selectedResources, this.recursiveChecker, IResource.DEPTH_INFINITE);
					this.recursiveChecker.checkDisallowed();
				}
			}
			else {
				this.recursiveChecker.setAllTo(Boolean.TRUE);
			}
		}
		Map filtersMap = recursive ? this.recursiveFilters2condition : this.filters2condition;
		Boolean retVal = (Boolean)filtersMap.get(stateFilter);
		if (retVal == null) {
			if (!computeDeep && recursive) {
				filtersMap.put(stateFilter, retVal = Boolean.TRUE);
			}
			else {
				boolean containsResources = FileUtility.checkForResourcesPresence(selectedResources, stateFilter, recursive ? IResource.DEPTH_INFINITE : IResource.DEPTH_ZERO);
				filtersMap.put(stateFilter, retVal = Boolean.valueOf(containsResources));
			}
		}
		return retVal.booleanValue();
	}
	
	protected boolean connectedToSVN(IResource []selectedResources) {
		for (int i = 0; i < selectedResources.length; i++) {
			if (!FileUtility.isConnected(selectedResources[i])) {
				return false;
			}
		}
		return true;
	}
	
	private FilterManager() {
		this.filters2condition = new HashMap();
		this.recursiveFilters2condition = new HashMap();
		
		this.flatChecker = new MapChecker(this.filters2condition);
		this.recursiveChecker = new MapChecker(this.recursiveFilters2condition);
		SVNTeamUIPlugin.instance().getPreferenceStore().addPropertyChangeListener(this);
		SVNRemoteStorage.instance().addResourceStatesListener(ResourceStatesChangedEvent.class, this);
	}

	protected static class MapChecker extends IStateFilter.AbstractStateFilter {
		protected Map filterMap;
		
		public MapChecker(Map filterMap) {
			this.filterMap = filterMap;
		}
		
		public void clearFilters() {
			for (Iterator it = this.filterMap.keySet().iterator(); it.hasNext(); ) {
				this.filterMap.put(it.next(), null);
			}
		}
		
		public void setAllTo(Boolean value) {
			for (Iterator it = this.filterMap.keySet().iterator(); it.hasNext(); ) {
				this.filterMap.put(it.next(), value);
			}
		}
		
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			boolean retVal = true;
			for (Iterator it = this.filterMap.entrySet().iterator(); it.hasNext(); ) {
				Map.Entry entry = (Map.Entry)it.next();
				if (entry.getValue() == null) {
					IStateFilter filter = (IStateFilter)entry.getKey();
					boolean value = local == null ? filter.accept(resource, state, mask) : filter.accept(local);
					retVal &= value;
					if (value) {
						this.filterMap.put(filter, Boolean.TRUE);
					}
				}
			}
			return retVal;
		}
		
		public void checkDisallowed() {
			for (Iterator it = this.filterMap.entrySet().iterator(); it.hasNext(); ) {
				Map.Entry entry = (Map.Entry)it.next();
				if (entry.getValue() == null) {
					this.filterMap.put(entry.getKey(), Boolean.FALSE);
				}
			}
		}

		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return true;
		}

	}
	
}
