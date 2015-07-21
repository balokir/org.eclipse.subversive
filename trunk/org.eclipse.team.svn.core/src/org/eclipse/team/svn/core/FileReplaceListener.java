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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNDepth;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.local.AbstractWorkingCopyOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;

/**
 * Handles the file deletion "Undo": there should be no change in the end.
 * If the files are different everything will be left as is. 
 * 
 * @author Igor Burilo
 */
public class FileReplaceListener implements IResourceChangeListener {

	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.POST_CHANGE || event.getType() == IResourceChangeEvent.PRE_BUILD) {
			try {
				final List<IFile> added = new ArrayList<IFile>();			
				event.getDelta().accept(new IResourceDeltaVisitor() {
					public boolean visit(IResourceDelta delta) throws CoreException {
						if (delta.getResource().getType() == IResource.FILE) {
							int kind = delta.getKind();
							if (kind == IResourceDelta.ADDED || kind == IResourceDelta.CHANGED) {
								added.add((IFile)delta.getResource());
							}					
						}					
						return true;
					}			
				});			
				if (!added.isEmpty()) {
					this.processResources(added.toArray(new IResource[0]));
				}
			} catch (CoreException e) {
				LoggedOperation.reportError(this.getClass().getName(), e);
			}
		}
	}

	protected void processResources(IResource []resources) {
		FileReplaceListenerOperation mainOp = new FileReplaceListenerOperation(resources);			
		
		CompositeOperation cmpOp = new CompositeOperation(mainOp.getId(), mainOp.getMessagesClass());
		cmpOp.add(mainOp);
		cmpOp.add(new RefreshResourcesOperation(mainOp, IResource.DEPTH_ZERO, RefreshResourcesOperation.REFRESH_CHANGES));			
		ProgressMonitorUtility.doTaskScheduledDefault(cmpOp);
	}
	
	private class FileReplaceListenerOperation 
		extends AbstractWorkingCopyOperation 
		implements IResourceProvider {
		
		private ArrayList<IResource> processedResources;
		
		public FileReplaceListenerOperation(IResource []resources) {
			super("Operation_FileReplaceListener", SVNMessages.class, resources);//$NON-NLS-1$
			this.processedResources = new ArrayList<IResource>();
		}
		
		public IResource[] getResources() {
			return this.processedResources.toArray(new IResource[this.processedResources.size()]);
		}
		
		protected void runImpl(IProgressMonitor monitor) throws Exception {																				
			IResource []resources = this.operableData();
			SVNRemoteStorage.instance().refreshLocalResources(resources, IResource.DEPTH_ZERO);
			for (IResource file : resources) {														
				if (monitor.isCanceled()) {
					return;
				}

				ILocalResource local = SVNRemoteStorage.instance().asLocalResource(file);
				if (!IStateFilter.SF_PREREPLACEDREPLACED.accept(local)) {
					continue;
				}
				IResource parent = file.getParent();
				ILocalResource localParent = SVNRemoteStorage.instance().asLocalResource(parent);
				if (IStateFilter.SF_DELETED.accept(localParent)) {
					continue;
				}
				
				File originalFile = new File(FileUtility.getWorkingCopyPath(file));
				File tmpFile = new File(originalFile + ".svntmp"); //$NON-NLS-1$
				try {
					IRepositoryLocation location = SVNRemoteStorage.instance().getRepositoryLocation(file);
					ISVNConnector proxy = location.acquireSVNProxy();
					FileOutputStream oStream = null;
					try {
						oStream = new FileOutputStream(tmpFile);
						proxy.streamFileContent(new SVNEntryRevisionReference(originalFile.getAbsolutePath(), null, SVNRevision.BASE), 8192, oStream, new SVNProgressMonitor(this, monitor, null));
						if (this.equals(originalFile, tmpFile)) {
							this.processedResources.add(file);
							originalFile.delete();
							proxy.revert(originalFile.getAbsolutePath(), SVNDepth.EMPTY, null, new SVNProgressMonitor(this, monitor, null));
						}
					}
					finally {
						if (oStream != null) {
							try {oStream.close();} catch (IOException ex) {}
						}
						location.releaseSVNProxy(proxy);
					}
				}
				finally {
					if (!originalFile.exists()) {
						tmpFile.renameTo(originalFile);
					}
					else {
						tmpFile.delete();
					}
				}
			}									
		}	
		
		protected boolean equals(File src1, File src2) throws IOException {
			long len = src1.length();
			if (len == src2.length()) {
				FileInputStream stream1 = null;
				FileInputStream stream2 = null;
				try {
					stream1 = new FileInputStream(src1);
					stream2 = new FileInputStream(src2);
					int bufSize = len < 8192 ? (int)len : 8192;
					byte []buffer1 = new byte[bufSize];
					byte []buffer2 = new byte[bufSize];
					int rem = (int)(len % bufSize);
					for (int off = 0; off < len; off += bufSize) {
						stream1.read(buffer1);							
						stream2.read(buffer2);
						if (!Arrays.equals(buffer1, buffer2)) {
							return false;
						}
					}
					if (rem != 0) {
						buffer1 = new byte[rem];
						buffer2 = new byte[rem];
						stream1.read(buffer1);
						stream2.read(buffer2);
						if (!Arrays.equals(buffer1, buffer2)) {
							return false;
						}
					}
					return true;
				}
				finally {
					if (stream1 != null) {
						try {stream1.close();} catch (IOException ex) {}
					}
					if (stream2 != null) {
						try {stream2.close();} catch (IOException ex) {}
					}
				}
			}
			return false;
		}
	}
}
