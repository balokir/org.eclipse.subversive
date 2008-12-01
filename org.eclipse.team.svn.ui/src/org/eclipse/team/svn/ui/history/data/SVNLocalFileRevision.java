/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *    Thomas Champagne - Bug 217561 : additional date formats for label decorations
 *******************************************************************************/

package org.eclipse.team.svn.ui.history.data;

import java.io.ByteArrayInputStream;

import org.eclipse.compare.IEditableContent;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.internal.core.history.LocalFileRevision;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.operation.UILoggedOperation;
import org.eclipse.team.svn.ui.utility.DateFormatter;

/**
 * Local file revision representation.
 * Wrapper for both IFileState and IFile.
 * 
 * @author Alexei Goncharov
 */
public class SVNLocalFileRevision extends LocalFileRevision implements IEditableContent {
	public SVNLocalFileRevision (IFile file) {
		super(file);
	}
	
	public SVNLocalFileRevision (IFileState fileState) {
		super(fileState);
	}

	public String getComment() {
		if (this.getFile() != null) {
			return SVNUIMessages.SVNLocalFileRevision_CurrentVersion;
		}
		return "";
	}
	
	public String getContentIdentifier() {
		if (this.getFile() != null) {
			return "";
		}
		return "[" + DateFormatter.formatDate(this.getTimestamp()) + "]"; 
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
				UILoggedOperation.reportError(SVNMessages.Operation_GetFileContent_SetContent, ex);
			}
		}
	}

}
