/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.operation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Copy file content into clipboard
 * 
 * @author Alexander Gurov
 */
public class FileToClipboardOperation extends AbstractActionOperation {
	protected String fileName;
	protected boolean deleteFile;
	
	public FileToClipboardOperation(String fileName) {
		this(fileName, true);
	}
	
	public FileToClipboardOperation(String fileName, boolean deleteFile) {
		super("Operation_FileToClipboard", SVNUIMessages.class); //$NON-NLS-1$
		this.fileName = fileName;
		this.deleteFile = deleteFile;
	}
	
	public int getOperationWeight() {
		return 0;
	}

	protected void runImpl(final IProgressMonitor monitor) throws Exception {
		final ByteArrayOutputStream data = new ByteArrayOutputStream();
		File tempFile = new File(this.fileName);
		FileInputStream stream = new FileInputStream(tempFile);
		try {
			byte []buf = new byte[2048];
			int len = 0;
			while ((len = stream.read(buf)) > 0) {
				data.write(buf, 0, len);
			}
		}
		finally {
			try {stream.close();} catch (Exception ex) {}
		}
		if (this.deleteFile) {
			tempFile.delete();
		}
		if (data.size() > 0) {
			final Display display = UIMonitorUtility.getDisplay();
			display.syncExec(new Runnable() {
				public void run() {
					TextTransfer plainTextTransfer = TextTransfer.getInstance();
					Clipboard clipboard = new Clipboard(display);
					clipboard.setContents(new String[] {data.toString()}, new Transfer[] {plainTextTransfer});
					clipboard.dispose();
				}
			});
		}
	}

}
