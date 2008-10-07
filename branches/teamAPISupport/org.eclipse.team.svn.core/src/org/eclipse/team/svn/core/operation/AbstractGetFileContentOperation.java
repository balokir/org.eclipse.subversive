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

package org.eclipse.team.svn.core.operation;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;


/**
 * Abstract implementation of get file content operation
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractGetFileContentOperation extends AbstractActionOperation {
	protected File tmpFile;

	public AbstractGetFileContentOperation(String getOperationType) {
		super("Operation.GetFileContent." + getOperationType);
	}
	
	public String getTemporaryPath() {
		return this.tmpFile == null ? null : this.tmpFile.getAbsolutePath();
	}

	public InputStream getContent() {
		final InputStream []retVal = new InputStream[] {new ByteArrayInputStream(new byte[0])};
		if (this.tmpFile != null && this.tmpFile.exists()) {
			ProgressMonitorUtility.doTaskExternal(new AbstractActionOperation("Operation.GetFileContent.CreateStream") {
				protected void runImpl(IProgressMonitor monitor) throws Exception {
					retVal[0] = new FileInputStream(AbstractGetFileContentOperation.this.tmpFile);
				}
			}, new NullProgressMonitor());
		}
		return retVal[0];
	}
	
	public void setContent(final byte []data) {
		ProgressMonitorUtility.doTaskExternal(new AbstractActionOperation("Operation.GetFileContent.SetContent") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				if (AbstractGetFileContentOperation.this.tmpFile == null) {
					AbstractGetFileContentOperation.this.tmpFile = AbstractGetFileContentOperation.this.createTempFile();
				}
				FileOutputStream stream = new FileOutputStream(AbstractGetFileContentOperation.this.tmpFile);
				try {
					stream.write(data);
				}
				finally {
					try {stream.close();} catch (Exception ex) {}
				}
			}
		}, new NullProgressMonitor());
	}
	
	protected File createTempFile() throws IOException {
		String extension = this.getExtension();
		File retVal = File.createTempFile("getfilecontent", ".tmp" + (extension != null && extension.length() > 0 ? "." + extension : ""), SVNTeamPlugin.instance().getStateLocation().toFile());
		retVal.deleteOnExit();
		return retVal;
	}
	
	protected String getExtension() {
		return "";
	}
	
}
