/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Gabor Liptak - Speedup Pattern's usage
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.local;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.PatternProvider;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * The operation saves project meta (.project and .classpath) in order to prevent project refresh problem when meta is deleted
 * 
 * @author Alexander Gurov
 */
public class SaveProjectMetaOperation extends AbstractWorkingCopyOperation implements IResourceProvider {
	// exclude .settings folder due large information amount contained in it
	protected static final String []META_FILES = new String[] {".project", ".classpath"}; //$NON-NLS-1$ //$NON-NLS-2$
	protected HashMap<String, File> savedMetas;
	protected String startsWith;

	public SaveProjectMetaOperation(IResource []resources) {
		this(resources, null);
	}
	
	public SaveProjectMetaOperation(IResource []resources, String startsWith) {
		super("Operation_SaveMeta", SVNMessages.class, resources); //$NON-NLS-1$
		this.savedMetas = new HashMap<String, File>();
		this.startsWith = startsWith;
	}

	public SaveProjectMetaOperation(IResourceProvider provider) {
		this(provider, null);
	}
	
	public SaveProjectMetaOperation(IResourceProvider provider, String startsWith) {
		super("Operation_SaveMeta", SVNMessages.class, provider); //$NON-NLS-1$
		this.savedMetas = new HashMap<String, File>();
		this.startsWith = startsWith;
	}
	
	public IResource []getResources() {
		return this.operableData();
	}
	
	public Map<String, File> getSavedMetas() {
		return this.savedMetas;
	}
	
	public int getOperationWeight() {
		return 0;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource []changeSet = this.operableData();
		
		for (int i = 0; i < changeSet.length && !monitor.isCanceled(); i++) {
			final IResource change = changeSet[i];
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					IProject project = change.getProject();
					IResource []members = project.members();
					for (int i = 0; i < members.length && !monitor.isCanceled(); i++) {
						if (SaveProjectMetaOperation.this.shouldBeSaved(members[i])) {
							try {
								SaveProjectMetaOperation.this.saveMeta(members[i], monitor);
							}
							catch (Exception ex) {
								// disallow error reporting
							}
						}
					}
				}
			}, monitor, changeSet.length);
		}
	}
	
	protected boolean shouldBeSaved(IResource resource) {
		String name = resource.getName();
		if (name.equals(SVNUtility.getSVNFolderName())) {
			return false;
		}
		if (this.startsWith != null) {
			return name.startsWith(this.startsWith);
		}
		for (int i = 0; i < SaveProjectMetaOperation.META_FILES.length; i++) {
			if (name.equalsIgnoreCase(SaveProjectMetaOperation.META_FILES[i])) {
				return true;
			}
		}
		return false;
	}

	protected void saveMeta(IResource resource, IProgressMonitor monitor) throws Exception {
		if (resource != null) {
			String sourceLocation = PatternProvider.replaceAll(FileUtility.getWorkingCopyPath(resource), "\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
			File source = new File(sourceLocation);
			if (source.exists()) {
				File target = File.createTempFile("save_" + resource.getName(), ".tmp", SVNTeamPlugin.instance().getStateLocation().toFile()); //$NON-NLS-1$ //$NON-NLS-2$
				target.deleteOnExit();
				if (source.isDirectory()) {
					target.delete();
					FileUtility.copyAll(target, source, FileUtility.COPY_NO_OPTIONS, new FileFilter() {
						public boolean accept(File pathname) {
							return !pathname.getName().equals(SVNUtility.getSVNFolderName());
						}
					}, monitor);
				}
				else {
					FileUtility.copyFile(target, source, monitor);
				}
				this.savedMetas.put(sourceLocation, target);
			}
		}
	}
	
}
