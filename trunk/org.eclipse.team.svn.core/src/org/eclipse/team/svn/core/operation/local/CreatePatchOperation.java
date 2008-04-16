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

package org.eclipse.team.svn.core.operation.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.Team;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Create patch operation implementation
 * 
 * @author Alexander Gurov
 */
public class CreatePatchOperation extends AbstractActionOperation {
	public static final int SELECTION = 0;
	public static final int PROJECT = 1;
	public static final int WORKSPACE = 2;
	
	protected IResource []resources;
	protected IResource []selection;
	protected String fileName;
	protected boolean recurse;
	protected boolean ignoreDeleted;
	protected boolean processBinary;
	protected boolean processUnversioned;
	protected int rootPoint;
	
	protected String lineFeed = System.getProperty("line.separator");
	protected String contentSeparator = this.lineFeed + "===================================================================" + this.lineFeed;
	protected String indexEntry = "Index: ";
	protected String removeSign = "--- ";
	protected String addSign = "+++ ";
	protected String revisionMark = "\t(revision 0)" + this.lineFeed;
	protected String noLF = "\\ No newline at end of file" + this.lineFeed;
	protected String rangeStart = "@@ -0,0 +1";
	protected String rangeEnd = " @@" + this.lineFeed;
	
	public CreatePatchOperation(IResource []resources, String fileName, boolean recurse, boolean ignoreDeleted, boolean processBinary, boolean processUnversioned) {
		this(resources, fileName, recurse, ignoreDeleted, processBinary, processUnversioned, CreatePatchOperation.PROJECT);
	}
	
	public CreatePatchOperation(IResource []resources, String fileName, boolean recurse, boolean ignoreDeleted, boolean processBinary, boolean processUnversioned, int rootPoint) {
		super("Operation.CreatePatchLocal");
		this.resources = resources;
		this.fileName = fileName;
		this.recurse = recurse;
		this.ignoreDeleted = ignoreDeleted;
		this.processBinary = processBinary;
		this.processUnversioned = processUnversioned;
		this.rootPoint = rootPoint;
	}

	protected void runImpl(final IProgressMonitor monitor) throws Exception {
		Map workingCopies = SVNUtility.splitWorkingCopies(this.resources);
		final FileOutputStream stream = new FileOutputStream(this.fileName);
		try {
//			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn diff " + (this.recurse ? "" : " -N") + (this.ignoreDeleted ? " --no-diff-deleted" : "") + "\n");
			if (workingCopies.size() > 1 || this.rootPoint == CreatePatchOperation.WORKSPACE) {
				this.rootPoint = CreatePatchOperation.WORKSPACE;
				stream.write("### Eclipse Workspace Patch 1.0".getBytes());
				stream.write(this.lineFeed.getBytes());
			}
			else if (this.rootPoint == CreatePatchOperation.SELECTION) {
				this.selection = FileUtility.shrinkChildNodes(this.resources);
			}
			for (Iterator it = workingCopies.entrySet().iterator(); it.hasNext() && !monitor.isCanceled(); ) {
				Map.Entry entry = (Map.Entry)it.next();
				IProject project = (IProject)entry.getKey();
				if (this.rootPoint == CreatePatchOperation.WORKSPACE) {
					stream.write("#P ".getBytes());
					stream.write(project.getName().getBytes());
					stream.write(this.lineFeed.getBytes());
				}
				IResource []resources = (IResource [])((List)entry.getValue()).toArray(new IResource[0]);
				for (int i = 0; i < resources.length && !monitor.isCanceled(); i++) {
					if (resources[i] instanceof IFile) {
						this.addFileDiff(stream, (IFile)resources[i], monitor);
					}
					else if (this.recurse) {
						FileUtility.visitNodes(resources[i], new IResourceVisitor() {
							public boolean visit(IResource resource) throws CoreException {
								if (resource instanceof IFile) {
									try {
										CreatePatchOperation.this.addFileDiff(stream, (IFile)resource, monitor);
									}
									catch (RuntimeException ex) {
										throw ex;
									}
									catch (CoreException ex) {
										throw ex;
									}
									catch (Exception ex) {
										throw new RuntimeException(ex);
									}
								}
								return true;
							}
						}, IResource.DEPTH_INFINITE);
					}
				}
			}
		}
		finally {
			try {stream.close();} catch (Exception ex) {}
		}
	}
	
	protected void addFileDiff(OutputStream stream, IFile resource, IProgressMonitor monitor) throws Exception {
		String wcPath = FileUtility.getWorkingCopyPath(resource);
		String projectPath = FileUtility.getWorkingCopyPath(resource.getProject());
		String fileName = wcPath.substring(projectPath.length() + 1);
		if (this.rootPoint == CreatePatchOperation.SELECTION) {
			IPath resourcePath = resource.getFullPath();
			for (int i = 0; i < this.selection.length; i++) {
				IPath selectionPath = this.selection[i].getFullPath();
				if (selectionPath.isPrefixOf(resourcePath)) {
					fileName = this.selection[i].getType() == IResource.FILE ? fileName = resource.getName() : (resourcePath.toString().substring(selectionPath.toString().length() + 1));
					break;
				}
			}
		}
		
		ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
		if (IStateFilter.SF_VERSIONED.accept(local)) {
			File tmp = File.createTempFile("patch", ".tmp", SVNTeamPlugin.instance().getStateLocation().toFile());
			tmp.deleteOnExit();
			
			IRepositoryLocation location = SVNRemoteStorage.instance().getRepositoryLocation(resource);
			ISVNConnector proxy = location.acquireSVNProxy();
			long options = ISVNConnector.Options.IGNORE_ANCESTRY;
			options |= this.ignoreDeleted ? ISVNConnector.Options.SKIP_DELETED : ISVNConnector.Options.NONE;
			options |= this.processBinary ? ISVNConnector.Options.FORCE : ISVNConnector.Options.NONE;
			
			try {
				proxy.diff(new SVNEntryRevisionReference(wcPath, null, SVNRevision.BASE), new SVNEntryRevisionReference(wcPath, null, SVNRevision.WORKING), projectPath, 
						tmp.getAbsolutePath(), Depth.EMPTY, options, null, new SVNProgressMonitor(this, monitor, null));
				
				int len = (int)tmp.length();
				if (len > 0) {
					byte []data = new byte[len];
					InputStream input = new FileInputStream(tmp);
					try {
						input.read(data);
					}
					finally {
						try {input.close();} catch (Exception ex) {}
					}
					String diff = new String(data);
					int idx = diff.indexOf(this.contentSeparator);
					if (idx != -1) {
						String diffTail = diff.substring(idx);
						idx = diffTail.indexOf(this.removeSign);
						int idx1 = diffTail.indexOf('\t', idx);
						if (idx != -1 && idx1 != -1) {
							diffTail = diffTail.substring(0, idx + this.removeSign.length()) + fileName + diffTail.substring(idx1);
						}
						idx = diffTail.indexOf(this.addSign);
						idx1 = diffTail.indexOf('\t', idx);
						if (idx != -1 && idx1 != -1) {
							diffTail = diffTail.substring(0, idx + this.addSign.length()) + fileName + diffTail.substring(idx1);
						}
						diff = this.indexEntry + fileName + diffTail;
					}
					stream.write(diff.getBytes());
				}
			}
			finally {
				location.releaseSVNProxy(proxy);
				tmp.delete();
			}
		}
		else if (this.processUnversioned && !IStateFilter.SF_IGNORED.accept(local)) {
			int type = FileUtility.getMIMEType(resource);
			if (this.processBinary || type != Team.BINARY) {
				stream.write(this.getNewFileDiff(wcPath, fileName).getBytes());
			}
		}
	}
	
	protected String getNewFileDiff(String path, String fileName) throws Exception {
		File file = new File(path);
		byte []data = new byte[(int)file.length()];
		InputStream stream = new FileInputStream(file);
		try {
			stream.read(data);
			return this.getNewContentDiff(fileName, new String(data));
		}
		finally {
			try {stream.close();} catch (Exception ex) {}
		}
	}
	
	protected String getNewContentDiff(String fileName, String content) {
		if (content.length() == 0) {
			return this.getEmptyNewContentDiff(fileName);
		}
		ArrayList<String> tLines = new ArrayList<String>();
		for (int i = 0, lineStart = 0, m = content.length(); i < m; ) {
			if (content.charAt(i) == '\n') {
				tLines.add(content.substring(lineStart, ++i));
				lineStart = i;
			}
			else if (content.charAt(i) == '\r') {
				++i;
				if (i < m && content.charAt(i) == '\n') {
					++i;
				}
				tLines.add(content.substring(lineStart, i));
				lineStart = i;
			}
			else {
				i++;
			}
		}
		String []lines = tLines.toArray(new String[tLines.size()]);
		String retVal = this.getFilledNewContentDiff(fileName, lines);
		if (!content.endsWith("\r\n") && !content.endsWith("\r") && !content.endsWith("\n")) {
			retVal += this.noLF;
		}
		return retVal;
	}

	protected String getFilledNewContentDiff(String fileName, String []lines) {
		String retVal = 
			this.getEmptyNewContentDiff(fileName) + 
			this.removeSign + fileName + this.revisionMark +
			this.addSign + fileName + this.revisionMark + 
			this.rangeStart + (lines.length == 1 ? "" : ("," + lines.length)) + this.rangeEnd;
		for (int i = 0; i < lines.length; i++) {
			retVal += "+" + lines[i];
		}
		return retVal;
	}

	protected String getEmptyNewContentDiff(String fileName) {
		return this.indexEntry + fileName + this.contentSeparator;
	}
	
}
