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

package org.eclipse.team.svn.ui.repository.model;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;

/**
 * All repositories node representation 
 * 
 * @author Alexander Gurov
 */
public class RepositoriesRoot extends RepositoryFictiveNode implements IParentTreeNode, IDataTreeNode {
	protected RepositoryLocation []children;
	protected boolean softRefresh;

	public Object getData() {
		return null;
	}
	
	public void refresh() {
		this.children = null;
	}
	
	public void softRefresh() {
		this.softRefresh = true;
	}
	
	public String getLabel(Object o) {
		return null;
	}

	public boolean hasChildren() {
		return true;
	}
	
	public Object []getChildren(Object o) {
		if (this.children == null || this.softRefresh) {
			HashMap<IRepositoryLocation, RepositoryLocation> oldLocations = new HashMap<IRepositoryLocation, RepositoryLocation>();
			if (this.children != null) {
				for (int i = 0; i < this.children.length; i++) {
					oldLocations.put(this.children[i].getRepositoryLocation(), this.children[i]);
				}
			}
			
			IRepositoryLocation []locations = SVNRemoteStorage.instance().getRepositoryLocations();
			Arrays.sort(locations, new Comparator<IRepositoryLocation>() {
				public int compare(IRepositoryLocation first, IRepositoryLocation second) {
					IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
					if (SVNTeamPreferences.getBehaviourBoolean(store, SVNTeamPreferences.BEHAVIOUR_CASE_INSENSITIVE_TABLE_SORTING_NAME)) {
						return first.getLabel().compareToIgnoreCase(second.getLabel());
					}
					return first.getLabel().compareTo(second.getLabel());
				}
			});
			this.children = new RepositoryLocation[locations.length];
			for (int i = 0; i < locations.length; i++) {
				this.children[i] = oldLocations.get(locations[i]);
				if (this.children[i] == null) {
					this.children[i] = new RepositoryLocation(locations[i]);
				}
			}
		}
		return this.children;
	}

	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}

}
