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

import java.io.ByteArrayInputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.eclipse.compare.IEditableContent;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.internal.core.history.LocalFileRevision;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.operation.UILoggedOperation;

/**
 * Local file revision representation.
 * Wrapper for both IFileState and IFile.
 * 
 * @author Alexei Goncharov
 */
public class SVNLocalFileRevision extends LocalFileRevision implements IEditableContent {

	protected DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.getDefault());
	
	public SVNLocalFileRevision (IFile file) {
		super(file);
	}
	
	public SVNLocalFileRevision (IFileState fileState) {
		super(fileState);
	}

	public String getComment() {
		if (this.getFile() != null) {
			return SVNTeamUIPlugin.instance().getResource("SVNLocalFileRevision.CurrentVersion");
		}
		return "";
	}
	
	public String getContentIdentifier() {
		if (this.getFile() != null) {
			return "";
		}
		return "[" + dateTimeFormat.format(new Date(this.getTimestamp())) + "]"; 
	}
	
	public boolean isEditable() {
		return this.isCurrentState();
	}

	public ITypedElement replace(ITypedElement dest, ITypedElement src) {
		return dest;
	}
	
	public void setContent(byte[] newContent) {
		if (this.isEditable()) {
			try {
				this.getFile().setContents(new ByteArrayInputStream(newContent), true, true, new NullProgressMonitor());
			}
			catch (CoreException ex) {
				UILoggedOperation.reportError(SVNTeamUIPlugin.instance().getResource("Operation.GetFileContent.SetContent"), ex);
			}
		}
	}

}
