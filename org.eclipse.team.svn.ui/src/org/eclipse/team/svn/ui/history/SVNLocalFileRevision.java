/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.history;

import java.net.URI;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.history.provider.FileRevision;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;

/**
 * Local file revision representation.
 * Wrapper for both IFileState and IFile.
 * 
 * @author Alexei Goncharov
 */
public class SVNLocalFileRevision extends FileRevision {

	protected DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.getDefault());
	protected IFile file;
	protected IFileState fileState;
	
	public SVNLocalFileRevision (IFile file) {
		this.file = file;
		this.fileState = null;
	}
	
	public SVNLocalFileRevision (IFileState fileState) {
		this.fileState = fileState;
		this.file = null;
	}

	public String getAuthor() {
		return "";
	}

	public String getComment() {
		if (this.file != null) {
			return SVNTeamUIPlugin.instance().getResource("SVNLocalFileRevision.CurrentVersion");
		}
		return "";
	}
	
	public boolean isCurrentState() {
		return (file != null);
	}
	
	public URI getURI() {
		if (file != null) {
			return file.getLocationURI();
		}
		return ResourcesPlugin.getWorkspace().getRoot().getFile(fileState.getFullPath()).getLocationURI();
	}
	
	public int hashCode() {
		if (file != null) {
			return file.hashCode();
		}
		return fileState.hashCode();
	}
	
	public boolean exists() {
		if (file != null) {
			return file.exists();
		}
		return fileState.exists();
	}
	
	public long getTimestamp() {
		if (file != null) {
			return file.getLocalTimeStamp();
		}
		return fileState.getModificationTime();
	}

	public String getName() {
		if (this.file != null) {
			return file.getName();
		}
		return fileState.getName();
	}

	public IStorage getStorage(IProgressMonitor monitor){
		if (file != null) {
			return file;
		}
		return fileState;
	}
	
	public String getContentIdentifier() {
		if (this.file != null) {
			return "";
		}
		return "[" + dateTimeFormat.format(new Date(this.getTimestamp())) + "]"; 
	}

	public boolean isPropertyMissing() {
		return true;
	}

	public IFileRevision withAllProperties(IProgressMonitor monitor) {
		return this;
	}

}
