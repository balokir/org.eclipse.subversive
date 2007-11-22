/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.operation;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.team.svn.core.client.SVNConnectorException;
import org.eclipse.team.svn.core.operation.AbstractNonLockingOperation;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.remote.GetFileContentOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;

/**
 * Obtain project names from .project files operation 
 *
 * @author Sergiy Logvin
 */
public class ObtainProjectNameOperation extends AbstractNonLockingOperation {
	
	protected IRepositoryResourceProvider resourceProvider;
	protected IRepositoryResource []resources;
	protected HashMap names2Resources;

	public ObtainProjectNameOperation(IRepositoryResource[] resources) {
		super("Operation.ObtainProjectName");
		this.resources = resources;
		this.names2Resources = new HashMap();
	}
	
	public ObtainProjectNameOperation(IRepositoryResourceProvider resourceProvider) {
		this((IRepositoryResource[])null);
		this.resourceProvider = resourceProvider;
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		final boolean doObtainFromDotProject = SVNTeamPreferences.getCheckoutBoolean(store, SVNTeamPreferences.CHECKOUT_USE_DOT_PROJECT_NAME);
		final Set lowerCaseNames = new HashSet();
		final boolean caseInsensitiveOS = FileUtility.isCaseInsensitiveOS();
		if (this.resourceProvider != null) {
			this.resources = this.resourceProvider.getRepositoryResources();
		}
		for (int i = 0; i < this.resources.length && !monitor.isCanceled(); i++) {
			ProgressMonitorUtility.setTaskInfo(monitor, this, MessageFormat.format(this.getOperationResource("Scanning"), new String[] {this.resources[i].getName()}));
			
			final int j = i;
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					String name = null;
					String newName = null;
					try {
						name = doObtainFromDotProject ? ObtainProjectNameOperation.this.obtainProjectName(ObtainProjectNameOperation.this.resources[j], monitor) : null;
					}
					catch (SVNConnectorException ex) {
						// do nothing
					}
					if (name == null) {
						IRepositoryResource resource = ObtainProjectNameOperation.this.resources[j];
						IRepositoryLocation location = resource.getRepositoryLocation();
						if (location.isStructureEnabled() && location.getTrunkLocation().equals(resource.getName())) {
							if (resource.getParent() != null) {
								name = resource.getParent().getName();
							}
							else {
								name = new Path(location.getUrl()).lastSegment();
							}
							name = FileUtility.formatResourceName(name);
						}
					}
					name = name == null ? ObtainProjectNameOperation.this.resources[j].getName() : name;
					if (!ObtainProjectNameOperation.this.names2Resources.containsKey(name) && (caseInsensitiveOS ? !lowerCaseNames.contains(name.toLowerCase()) : true)) {
						ObtainProjectNameOperation.this.names2Resources.put(name, ObtainProjectNameOperation.this.resources[j]);
						if (caseInsensitiveOS) {
							lowerCaseNames.add(name.toLowerCase());
						}
					}
					else {
						for (int k = 1; ; k++) {
							newName = name + " (" + k + ")";
							if (!ObtainProjectNameOperation.this.names2Resources.containsKey(newName) && (caseInsensitiveOS ? !lowerCaseNames.contains(newName.toLowerCase()) : true)) {
								ObtainProjectNameOperation.this.names2Resources.put(newName, ObtainProjectNameOperation.this.resources[j]);
								if (caseInsensitiveOS) {
									lowerCaseNames.add(newName.toLowerCase());
								}
								break;
							}
						}
					}
					
				}
			}, monitor, this.resources.length);
		
		}
	}
	
	protected String obtainProjectName(IRepositoryResource resource, IProgressMonitor monitor) throws Exception {
		String projFileUrl = resource.getUrl() + "/.project";
		IRepositoryResource projFile = resource.getRepositoryLocation().asRepositoryFile(projFileUrl, false);
		if (projFile.exists()) {
			GetFileContentOperation op = new GetFileContentOperation(projFile);
			ProgressMonitorUtility.doTaskExternal(op, new NullProgressMonitor());
			InputStream is = op.getContent();
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				String currentString;
				int first;
				int last;
				while ((currentString = reader.readLine()) != null) {
					//FIXME incorrect search: could be on different lines
					if ((first = currentString.indexOf("<name>")) >= 0 &&
						(last = currentString.indexOf("</name>")) >= 0) {
						return currentString.substring(first + "<name>".length(), last);
					}
				}
			}
			finally {
				if (reader != null) {
					try {reader.close();} catch (Exception ex) {}
				}
				try {is.close();} catch (Exception ex) {}
			}
		}

		return null;
	}

	public HashMap getNames2Resources() {
		return this.names2Resources;
	}
	
}
