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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
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
import org.eclipse.team.svn.core.operation.UnreportableException;
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
// TODO (requires API change) SVNTeamPreferences.getDecorationBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.DECORATION_ENABLE_PERSISTENT_SSH_NAME);
//		SVNTeamPlugin.instance().getOptionProvider().isFileReplaceListenerEnabled()
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
				IRepositoryLocation location = SVNRemoteStorage.instance().getRepositoryLocation(file);
				ISVNConnector proxy = location.acquireSVNProxy();
				OutputStreamComparator oStream = null;
				try {
					oStream = new OutputStreamComparator(originalFile);
					proxy.streamFileContent(new SVNEntryRevisionReference(originalFile.getAbsolutePath(), null, SVNRevision.BASE), 8192, oStream, new SVNProgressMonitor(this, monitor, null));
					this.processedResources.add(file);
					originalFile.delete();
					proxy.revert(new String[] {originalFile.getAbsolutePath()}, SVNDepth.EMPTY, null, ISVNConnector.Options.NONE, new SVNProgressMonitor(this, monitor, null));
				}
				catch (UnreportableException ex) {
					// do nothing
				}
				finally {
					if (oStream != null) {
						try {oStream.close();} catch (IOException ex) {}
					}
					location.releaseSVNProxy(proxy);
				}
			}									
		}	
	}
	
	private class OutputStreamComparator
		extends OutputStream {
		private FileInputStream stream;
		private byte []buffer;

		public OutputStreamComparator(File src1) throws FileNotFoundException {
			this.stream = new FileInputStream(src1);
		}
		
		public void write(int b) throws IOException {
			if (b != this.stream.read()) {
				throw new UnreportableException();
			}
		}
		
		public void write(byte b[], int off, int len) throws IOException {
			byte []b1 = b;
			if (b.length != len) {
				b1 = Arrays.copyOfRange(b, off, off + len);
			}
			if (this.buffer == null || this.buffer.length != b1.length) {
				this.buffer = new byte[b1.length];
			}
			this.stream.read(this.buffer);
			if (!Arrays.equals(b1, this.buffer)) {
				throw new UnreportableException();
			}
		}
		
		public void close() throws IOException {
			this.stream.close();
		}
		
	}
}
