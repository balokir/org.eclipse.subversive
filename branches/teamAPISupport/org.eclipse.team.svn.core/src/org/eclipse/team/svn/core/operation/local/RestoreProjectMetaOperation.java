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

package org.eclipse.team.svn.core.operation.local;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * The operation restores project meta (.project and .classpath) in order to prevent project refresh problem when meta is deleted
 * 
 * @author Alexander Gurov
 */
public class RestoreProjectMetaOperation extends AbstractActionOperation {
	protected SaveProjectMetaOperation saveOp;
	protected boolean force;
	
	public RestoreProjectMetaOperation(SaveProjectMetaOperation saveOp) {
		this(saveOp, false);
	}
	
	public RestoreProjectMetaOperation(SaveProjectMetaOperation saveOp, boolean force) {
		super("Operation.RestoreMeta");
		this.saveOp = saveOp;
		this.force = force;
	}

	public ISchedulingRule getSchedulingRule() {
		return this.saveOp.getSchedulingRule();
	}
	
	public int getOperationWeight() {
		return 0;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		Map<?, ?> container = this.saveOp.getSavedMetas();
		for (Iterator<?> it = container.entrySet().iterator(); it.hasNext() && !monitor.isCanceled(); ) {
			Map.Entry entry = (Map.Entry)it.next();
			final File targetFile = new File((String)entry.getKey());
			final File savedCopy = (File)entry.getValue();
			if (targetFile.getParentFile().exists()) {
				if (savedCopy.isDirectory()) {
					this.protectStep(new IUnprotectedOperation() {
						public void run(IProgressMonitor monitor) throws Exception {
							try {
								if (RestoreProjectMetaOperation.this.force) {
									FileUtility.copyAll(targetFile.getParentFile(), savedCopy.listFiles()[0], FileUtility.COPY_IGNORE_EXISTING_FOLDERS | FileUtility.COPY_OVERRIDE_EXISTING_FILES, null, monitor);
								}
								else {
									FileUtility.copyAll(targetFile.getParentFile(), savedCopy.listFiles()[0], FileUtility.COPY_IGNORE_EXISTING_FOLDERS, null, monitor);
								}
							}
							catch (Exception ex) {
								// Any Exception must be ignored in this context
							}
						}
					}, monitor, container.size());
				}
				else if (!targetFile.exists() || this.force) {
					this.protectStep(new IUnprotectedOperation() {
						public void run(IProgressMonitor monitor) throws Exception {
							try {
								FileUtility.copyFile(targetFile, savedCopy, monitor);
							}
							catch (Exception ex) {
								// Any Exception must be ignored in this context
							}
						}
					}, monitor, container.size());
				}
			}
			FileUtility.deleteRecursive(savedCopy);
		}
	}

}
