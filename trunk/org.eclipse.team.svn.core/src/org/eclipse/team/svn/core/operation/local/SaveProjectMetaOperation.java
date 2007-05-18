/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
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
	protected HashMap savedMetas;
	protected String startsWith;

	public SaveProjectMetaOperation(IResource []resources) {
		this(resources, ".");
	}
	
	public SaveProjectMetaOperation(IResource []resources, String startsWith) {
		super("Operation.SaveMeta", resources);
		this.savedMetas = new HashMap();
		this.startsWith = startsWith;
	}

	public SaveProjectMetaOperation(IResourceProvider provider) {
		this(provider, ".");
	}
	
	public SaveProjectMetaOperation(IResourceProvider provider, String startsWith) {
		super("Operation.SaveMeta", provider);
		this.savedMetas = new HashMap();
		this.startsWith = startsWith;
	}
	
	public IResource []getResources() {
		return this.operableData();
	}
	
	public Map getSavedMetas() {
		return this.savedMetas;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource []changeSet = this.operableData();
		
		for (int i = 0; i < changeSet.length; i++) {
			final IResource change = changeSet[i];
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					IProject project = change.getProject();
					IResource []members = project.members();
					for (int i = 0; i < members.length; i++) {
						if (SaveProjectMetaOperation.this.shouldBeSaved(members[i])) {
							SaveProjectMetaOperation.this.saveMeta(members[i], monitor);
						}
					}
				}
			}, monitor, changeSet.length);
		}
	}
	
	protected boolean shouldBeSaved(IResource resource) {
		String name = resource.getName();
		return name.startsWith(this.startsWith) && !name.equals(SVNUtility.getSVNFolderName());
	}

	protected void saveMeta(IResource resource, IProgressMonitor monitor) throws Exception {
		if (resource != null) {
			String sourceLocation = PatternProvider.replaceAll(resource.getLocation().toString(), "\\\\", "/");
			File source = new File(sourceLocation);
			if (source.exists()) {
				File target = null;
				try {
					target = File.createTempFile("save_" + resource.getName(), ".tmp", SVNTeamPlugin.instance().getStateLocation().toFile());
					target.deleteOnExit();
				}
				catch (IOException ex) {
					// disallow error reporting if user has no access rights
					return;
				}
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
