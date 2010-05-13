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

package org.eclipse.team.svn.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RevertOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;

/**
 * Handles file replacement as file modification, i.e. if file which is under
 * version control is deleted and then it was added file with the same name then
 * treat this file as modified but not as replaced
 * 
 * @author Igor Burilo
 */
public class FileReplaceListener implements IResourceChangeListener {

	public void resourceChanged(IResourceChangeEvent event) {
		try {
			final List<IFile> added = new ArrayList<IFile>();			
			event.getDelta().accept(new IResourceDeltaVisitor() {
				public boolean visit(IResourceDelta delta) throws CoreException {					
					if (delta.getResource().getType() == IResource.FILE && delta.getKind() == IResourceDelta.ADDED) {
						added.add((IFile)delta.getResource());
					}					
					return true;
				}			
			});			
			if (!added.isEmpty()) {
				this.processResources(added);
			}
		} catch (CoreException e) {
			LoggedOperation.reportError(this.getClass().getName(), e);
		}
	}

	protected void processResources(final List<IFile> addedFiles) {
		//collect files which were deleted and then added
		final List<ILocalResource> localResources = new ArrayList<ILocalResource>();
		final List<IResource> resources = new ArrayList<IResource>();
		for (IResource resource : addedFiles) {
			ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);								
			if (IStateFilter.SF_DELETED.accept(local)) {
				//check if parent directory is not marked for deletion
				IResource parent = local.getResource().getParent();
				if (parent != null) {
					ILocalResource localParent = SVNRemoteStorage.instance().asLocalResource(parent);
					if (!IStateFilter.SF_DELETED.accept(localParent)) {
						localResources.add(local);	
						resources.add(local.getResource());
					}									
				}				
			}						
		}
		if (!localResources.isEmpty()) {		
			final AbstractActionOperation mainOp = new AbstractActionOperation("Operation_FileReplaceListener", SVNMessages.class) { //$NON-NLS-1$
				protected void runImpl(IProgressMonitor monitor) throws Exception {																				
					for (final ILocalResource local : localResources) {														
						if (monitor.isCanceled()) {
							return;
						}				
						
						boolean hasError = true;
						File originalFile = new File(FileUtility.getWorkingCopyPath(local.getResource()));
						File tmpFile = new File(originalFile + ".svntmp"); //$NON-NLS-1$
						//used for restore
						File fileWithOriginalContent = originalFile;
						try {							
							if (tmpFile.exists()) {
								tmpFile.delete();
							}											
							if (originalFile.renameTo(tmpFile)) {
								fileWithOriginalContent = tmpFile;
								RevertOperation revertOp = new RevertOperation(new IResource[]{local.getResource()}, false);
								ProgressMonitorUtility.doTask(revertOp, monitor, 100, 60);													
								if (revertOp.getExecutionState() == IActionOperation.OK) {			
									if (!originalFile.delete()) {
										throw new Exception("Failed to delete file: " + originalFile); //$NON-NLS-1$
									}
									if (!tmpFile.renameTo(originalFile)) {
										throw new Exception("Failed to rename file: " + originalFile); //$NON-NLS-1$
									}
									fileWithOriginalContent = originalFile;
									hasError = false;
								} else {
									this.reportStatus(revertOp.getStatus());
								}								
							} else {
								throw new Exception("Failed to rename file: " + originalFile.getAbsolutePath()); //$NON-NLS-1$
							}
						} catch (Throwable t) {
							this.reportError(t);
						} finally {
							/*
							 * Restore
							 */
							if (hasError) {
								if (fileWithOriginalContent.equals(tmpFile)) {
									if (originalFile.exists()) {
										originalFile.delete();
									}
									if (tmpFile.renameTo(originalFile)) {
										tmpFile.delete();
									}
								} else if (fileWithOriginalContent.equals(originalFile)) {
									if (tmpFile.exists()) {
										tmpFile.delete();	
									}									
								}								
							}
						}
					}									
				}	
				
				public ISchedulingRule getSchedulingRule() {				
			    	HashSet<IResource> ruleSet = new HashSet<IResource>();	
			    	ruleSet.addAll(resources);
			    	
			    	for (IResource resource : resources) {
						ruleSet.add(resource instanceof IProject ? resource : resource.getParent());
			    	}
			    	return new MultiRule(ruleSet.toArray(new IResource[ruleSet.size()]));
				}
			};			
			
			CompositeOperation cmpOp = new CompositeOperation(mainOp.getId(), mainOp.getMessagesClass());
			cmpOp.add(mainOp);
			cmpOp.add(new RefreshResourcesOperation(resources.toArray(new IResource[0])));			
			ProgressMonitorUtility.doTaskScheduledDefault(cmpOp);
		}				
	}
}
