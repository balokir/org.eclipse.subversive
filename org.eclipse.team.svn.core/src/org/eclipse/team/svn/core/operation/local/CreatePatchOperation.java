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

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Create patch operation implementation
 * 
 * @author Alexander Gurov
 */
public class CreatePatchOperation extends AbstractActionOperation {
	protected IResource resource;
	protected String fileName;
	protected boolean recurse;
	protected boolean ignoreDeleted;
	protected boolean processBinary;
	protected boolean processUnversioned;
	protected boolean useRelativePath;
	
	public CreatePatchOperation(IResource resource, String fileName, boolean recurse, boolean ignoreDeleted, boolean processBinary, boolean processUnversioned, boolean useRelativePath) {
		super("Operation.CreatePatchLocal");
		this.resource = resource;
		this.fileName = fileName;
		this.recurse = recurse;
		this.ignoreDeleted = ignoreDeleted;
		this.processBinary = processBinary;
		this.processUnversioned = processUnversioned;
		this.useRelativePath = useRelativePath;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IRepositoryLocation location = SVNRemoteStorage.instance().getRepositoryLocation(this.resource);
		String wcPath = FileUtility.getWorkingCopyPath(this.resource);
		ISVNConnector proxy = location.acquireSVNProxy();
		try {
			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn diff " + (this.recurse ? "" : " -N") + (this.ignoreDeleted ? " --no-diff-deleted" : "") + "\n");
			long options = ISVNConnector.Options.IGNORE_ANCESTRY;
			options |= this.ignoreDeleted ? ISVNConnector.Options.SKIP_DELETED : ISVNConnector.Options.NONE;
			options |= this.processBinary ? ISVNConnector.Options.FORCE : ISVNConnector.Options.NONE;
			options |= this.processUnversioned ? ISVNConnector.Options.INCLUDE_UNVERSIONED : ISVNConnector.Options.NONE;
			proxy.diff(
				new SVNEntryRevisionReference(wcPath, null, SVNRevision.BASE), new SVNEntryRevisionReference(wcPath, null, SVNRevision.WORKING), this.useRelativePath ? wcPath : null, this.fileName, 
				Depth.infinityOrFiles(this.recurse), options, null, new SVNProgressMonitor(this, monitor, null));
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
	}
	
	protected String lineFeed = System.getProperty("line.separator");
	protected String contentSeparator = lineFeed + "===================================================================" + lineFeed;
	protected String indexEntry = "Index: ";
	protected String removeSign = "--- ";
	protected String addSign = "+++ ";
	protected String revisionMark = "\t(revision 0)" + lineFeed;
	protected String noLF = "\\ No newline at end of file" + lineFeed;
	protected String rangeStart = "@@ -0,0 +1";
	protected String rangeEnd = " @@" + lineFeed;
	
	protected String getNewFileDiff(IFile file) throws Exception {
		byte []data = new byte[(int)FileUtility.getResourcePath(file).toFile().length()];
		InputStream stream = file.getContents(true);
		try {
			stream.read(data);
			String fileName = FileUtility.getWorkingCopyPath(file).substring(FileUtility.getWorkingCopyPath(file.getProject()).length() + 1);
			return this.getNewFileDiff(fileName, new String(data));
		}
		finally {
			try {stream.close();} catch (Exception ex) {}
		}
	}
	
	protected String getNewFileDiff(String fileName, String content) {
		if (content.length() == 0) {
			return this.getEmptyNewFileDiff(fileName);
		}
		String []lines = content.split("\n\r|\n|\r");
		String retVal = this.getFilledNewFileDiff(fileName, lines);
		if (lines.length == 1 || lines[lines.length - 1].length() == 0) {
			retVal += this.noLF;
		}
		return retVal;
	}

	protected String getNewFileWOLastLFDiff(String fileName, String []lines) {
		return 
			this.getEmptyNewFileDiff(fileName) + 
			this.removeSign + fileName + this.revisionMark +
			this.addSign + fileName + this.revisionMark + 
			this.rangeStart + (lines.length == 1 ? "" : ("," + lines.length)) + this.rangeEnd;
	}

	protected String getFilledNewFileDiff(String fileName, String []lines) {
		String retVal = 
			this.getEmptyNewFileDiff(fileName) + 
			this.removeSign + fileName + this.revisionMark +
			this.addSign + fileName + this.revisionMark + 
			this.rangeStart + (lines.length == 1 ? "" : ("," + lines.length)) + this.rangeEnd;
		for (int i = 0; i < lines.length; i++) {
			retVal += "+" + lines[i] + this.lineFeed;
		}
		return retVal;
	}

	protected String getEmptyNewFileDiff(String fileName) {
		return this.indexEntry + fileName + this.contentSeparator;
	}
	
}
