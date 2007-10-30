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

package org.eclipse.team.svn.ui.debugmail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.SVNTeamProvider;
import org.eclipse.team.svn.core.operation.AbstractNonLockingOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.file.SVNFileStorage;
import org.eclipse.team.svn.core.operation.local.AbstractWorkingCopyOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Convert old plug-in and project settings to newest one
 * 
 * @author Alexander Gurov
 */
public class MigrateToEclipse extends AbstractMainMenuAction {
	public static final String OLD_PROVIDER_ID = "org.polarion.team.svn.core.svnnature";
	public static final String OLD_STATE_LOCATION = "org.polarion.team.svn.core";
	public static final String OLD_PREFERENCES_LOCATION = "org.polarion.team.svn.ui";
	public static final QualifiedName OLD_RESOURCE_PROPERTY = new QualifiedName("org.polarion.team.svn", "resource");
	public static final QualifiedName OLD_LOCATION_PROPERTY = new QualifiedName("org.polarion.team.svn", "location");

	protected static final byte []OLD_ENTRY1 = new byte[] {'o', 'r', 'g', '.', 'p', 'o', 'l', 'a', 'r', 'i', 'o', 'n'};
	protected static final byte []NEW_ENTRY1 = new byte[] {'o', 'r', 'g', '.', 'e', 'c', 'l', 'i', 'p', 's', 'e'};
	protected static final byte []OLD_ENTRY2 = new byte[] {'L', 'o', 'r', 'g', '/', 'p', 'o', 'l', 'a', 'r', 'i', 'o', 'n'};
	protected static final byte []NEW_ENTRY2 = new byte[] {'L', 'o', 'r', 'g', '/', 'e', 'c', 'l', 'i', 'p', 's', 'e'};
	
	public void run(IAction action) {
		CompositeOperation op = new CompositeOperation("Operation.MigrateToEclipse");
		op.add(new ConvertRepositoryFiles());
		op.add(new ConvertSettings());
		RemapProjects remapOp = new RemapProjects();
		op.add(remapOp);
		op.add(new RefreshResourcesOperation(remapOp, IResource.DEPTH_ONE, RefreshResourcesOperation.REFRESH_CACHE));
		UIMonitorUtility.doTaskNowDefault(op, false);
	}

	public void selectionChanged(IAction action, ISelection selection) {
//		action.setEnabled(Arrays.asList(RepositoryProvider.getAllProviderTypeIds()).indexOf(MigrateToEclipse.OLD_PROVIDER_ID) != -1);
	}

	protected static int replaceBinaryEntries(byte []data, int writeLen, byte []oldEntry, byte []newEntry, boolean decrementPrevByte) {
		for (int i = MigrateToEclipse.findNextBinaryEntry(data, 0, writeLen, oldEntry); i < writeLen; i = MigrateToEclipse.findNextBinaryEntry(data, i, writeLen, oldEntry)) {
			// decrement chars count
			if (decrementPrevByte) {
				data[i - 1]--;
			}
			for (int offs = 0; offs < newEntry.length && i < writeLen; i++, offs++) {
				data[i] = newEntry[offs];
			}
			System.arraycopy(data, i + 1, data, i, --writeLen - i);
		}
		return writeLen;
	}
	
	protected static int findNextBinaryEntry(byte []data, int start, int len, byte []oldEntry) {
main:
		for (; start < len; start++) {
			if (data[start] == oldEntry[0]) {
				for (int i = 1; i < oldEntry.length && start + i < len; i++) {
					if (data[start + i] != oldEntry[i]) {
						continue main;
					}
				}
				return start;
			}
		}
		return len;
	}
	
	protected static class RemapProjects extends AbstractWorkingCopyOperation implements IResourceProvider {
		protected List processed;
		
		public RemapProjects() {
			super("Operation.RemapProjects", (IResource [])null);
		}

		public IResource []getResources() {
			return (IProject [])this.processed.toArray(new IProject [this.processed.size()]);
		}
		
		protected void runImpl(IProgressMonitor monitor) throws Exception {
			this.processed = new ArrayList();
			
			IProject []projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			for (int i = 0; i < projects.length; i++) {
				final IProject current = projects[i];
				this.protectStep(new IUnprotectedOperation() {
					public void run(IProgressMonitor monitor) throws Exception {
						RemapProjects.this.processProject(current);
					}
				}, monitor, projects.length);
			}
		}
		
		protected void processProject(IProject current) throws Exception {
			if (RepositoryProvider.isShared(current)) {
				RepositoryProvider p = RepositoryProvider.getProvider(current);
				// check for old provider Id
				if (p != null && p.getID().equals(MigrateToEclipse.OLD_PROVIDER_ID)) {
					String resourceProperty = current.getPersistentProperty(MigrateToEclipse.OLD_RESOURCE_PROPERTY);
					String locationProperty = current.getPersistentProperty(MigrateToEclipse.OLD_LOCATION_PROPERTY);
					
					RepositoryProvider.unmap(current);
					
					current.setPersistentProperty(SVNTeamProvider.RESOURCE_PROPERTY, resourceProperty);
					current.setPersistentProperty(SVNTeamProvider.LOCATION_PROPERTY, locationProperty);
					
					RepositoryProvider.map(current, SVNTeamPlugin.NATURE_ID);
					
					this.processed.add(current);
				}
			}
		}

	}
	
	protected static class ConvertSettings extends AbstractNonLockingOperation {
		public ConvertSettings() {
			super("Operation.ConvertSettings");
		}
	
		protected void runImpl(IProgressMonitor monitor) throws Exception {
			InstanceScope ctx = new InstanceScope();
			IEclipsePreferences prefs = ctx.getNode(MigrateToEclipse.OLD_PREFERENCES_LOCATION);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			Platform.getPreferencesService().exportPreferences(prefs, output, null);
			
			byte []data = output.toByteArray();
			int dataLen = MigrateToEclipse.replaceBinaryEntries(data, data.length, MigrateToEclipse.OLD_ENTRY1, MigrateToEclipse.NEW_ENTRY1, false);
			
			ByteArrayInputStream input = new ByteArrayInputStream(data, 0, dataLen);
			Platform.getPreferencesService().importPreferences(input);
		}
		
	}
	
	protected static class ConvertRepositoryFiles extends AbstractNonLockingOperation {
		public ConvertRepositoryFiles() {
			super("Operation.ConvertRepositoryFiles");
		}

		protected void runImpl(IProgressMonitor monitor) throws Exception {
			IPath newStateLocation = SVNTeamPlugin.instance().getStateLocation();
			
			IPath oldStateLocation = newStateLocation.removeLastSegments(1).append(MigrateToEclipse.OLD_STATE_LOCATION);
			
			this.transformStoreFile(oldStateLocation.append(SVNRemoteStorage.STATE_INFO_FILE_NAME).toFile(), newStateLocation.append(SVNRemoteStorage.STATE_INFO_FILE_NAME).toFile());
			this.transformStoreFile(oldStateLocation.append(SVNFileStorage.STATE_INFO_FILE_NAME).toFile(), newStateLocation.append(SVNFileStorage.STATE_INFO_FILE_NAME).toFile());
			
			// upload repositories after converting repositories files
			SVNRemoteStorage.instance().initialize(newStateLocation);
			SVNFileStorage.instance().initialize(newStateLocation);
			
			// save complete set of repositories
			SVNRemoteStorage.instance().saveConfiguration();
			SVNFileStorage.instance().saveConfiguration();
		}
		
		protected void transformStoreFile(File oldFile, File newFile) throws Exception {
			if (!oldFile.exists()) {
				return;
			}
			byte []data = new byte[(int)oldFile.length()];
			FileInputStream stream = new FileInputStream(oldFile);
			try {
				stream.read(data);
			}
			finally {
				try {stream.close();} catch (Exception ex) {}
			}
			
			int writeLen = MigrateToEclipse.replaceBinaryEntries(data, data.length, MigrateToEclipse.OLD_ENTRY1, MigrateToEclipse.NEW_ENTRY1, true);
			writeLen = MigrateToEclipse.replaceBinaryEntries(data, writeLen, MigrateToEclipse.OLD_ENTRY2, MigrateToEclipse.NEW_ENTRY2, true);
			
			FileOutputStream out = new FileOutputStream(newFile);
			try {
				out.write(data, 0, writeLen);
			}
			finally {
				try {out.close();} catch (Exception ex) {}
			}
		}
		
	}
}
