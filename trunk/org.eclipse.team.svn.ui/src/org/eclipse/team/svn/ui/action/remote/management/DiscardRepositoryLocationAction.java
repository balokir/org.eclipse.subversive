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

package org.eclipse.team.svn.ui.action.remote.management;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.SVNTeamProvider;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.remote.management.DiscardRepositoryLocationsOperation;
import org.eclipse.team.svn.core.operation.remote.management.SaveRepositoryLocationsOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.dialog.DiscardConfirmationDialog;
import org.eclipse.team.svn.ui.operation.RefreshRepositoryLocationsOperation;
import org.eclipse.team.svn.ui.panel.remote.DiscardLocationFailurePanel;

/**
 * Discard location action
 * 
 * @author Alexander Gurov
 */
public class DiscardRepositoryLocationAction extends AbstractRepositoryTeamAction {

	public DiscardRepositoryLocationAction() {
		super();
	}
	
	public void run(IAction action) {
		IRepositoryLocation []locations = this.getSelectedRepositoryLocations();
		List selection = Arrays.asList(locations);
		List operateLocations = new ArrayList();
		operateLocations.addAll(Arrays.asList(locations));
		ArrayList connectedProjects = new ArrayList();
		HashSet connectedLocations = new HashSet();
		IProject []projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			RepositoryProvider tmp = RepositoryProvider.getProvider(projects[i]);
			if (tmp != null && SVNTeamPlugin.NATURE_ID.equals(tmp.getID())) {
				SVNTeamProvider provider = (SVNTeamProvider)tmp;
				if (selection.contains(provider.getRepositoryLocation())) {
					connectedProjects.add(projects[i]);
					connectedLocations.add(provider.getRepositoryLocation());
					operateLocations.remove(provider.getRepositoryLocation());
				}
			}
		}
		
		if (operateLocations.size() > 0) {
			locations = (IRepositoryLocation [])operateLocations.toArray(new IRepositoryLocation[operateLocations.size()]);
			DiscardConfirmationDialog dialog = new DiscardConfirmationDialog(this.getShell(), locations.length == 1, DiscardConfirmationDialog.MSG_LOCATION);
			if (dialog.open() == 0) {
				DiscardRepositoryLocationsOperation mainOp = new DiscardRepositoryLocationsOperation(locations);
				
				CompositeOperation op = new CompositeOperation(mainOp.getId());
				
				op.add(mainOp);
				op.add(new SaveRepositoryLocationsOperation());
				op.add(new RefreshRepositoryLocationsOperation(false));
				
				this.runNow(op, false);
			}
		}
		if (connectedProjects.size() > 0) {
			ArrayList locationsList = new ArrayList();
			for (Iterator iter = connectedLocations.iterator(); iter.hasNext();) {
				IRepositoryLocation location = (IRepositoryLocation)iter.next();
				locationsList.add(location.getLabel());
			}
			DiscardLocationFailurePanel panel = new DiscardLocationFailurePanel((String [])locationsList.toArray(new String[locationsList.size()]),
					(IProject [])connectedProjects.toArray(new IProject[connectedProjects.size()]));
			new DefaultDialog(this.getShell(), panel).open();
		}
	}

	protected boolean isEnabled() throws TeamException {
		return this.getSelectedRepositoryLocations().length > 0;
	}

}
