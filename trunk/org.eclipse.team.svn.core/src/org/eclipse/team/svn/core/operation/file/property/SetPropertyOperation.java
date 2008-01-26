/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.file.property;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.file.AbstractFileOperation;
import org.eclipse.team.svn.core.operation.file.IFileProvider;
import org.eclipse.team.svn.core.operation.file.SVNFileStorage;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Set resource property operation
 * 
 * @author Alexander Gurov
 */
public class SetPropertyOperation extends AbstractFileOperation {
	protected SVNProperty []propertyData;
	protected boolean isRecursive;
	
	public SetPropertyOperation(File []files, String name, byte []data, boolean isRecursive) {
		this(files, new SVNProperty[] {new SVNProperty(name, new String(data))}, isRecursive);
	}

	public SetPropertyOperation(File []files, SVNProperty []data, boolean isRecursive) {
		super("Operation.SetPropertiesFile", files);
		this.propertyData = data;
		this.isRecursive = isRecursive;
	}

	public SetPropertyOperation(IFileProvider provider, String name, byte []data, boolean isRecursive) {
		this(provider, new SVNProperty[] {new SVNProperty(name, new String(data))}, isRecursive);
	}

	public SetPropertyOperation(IFileProvider provider, SVNProperty []data, boolean isRecursive) {
		super("Operation.SetPropertiesFile", provider);
		this.propertyData = data;
		this.isRecursive = isRecursive;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		File []files = this.operableData();
		if (this.isRecursive) {
			files = FileUtility.shrinkChildNodes(files, false);
		}
		
		for (int i = 0; i < files.length && !monitor.isCanceled(); i++) {
			final File current = files[i];
			IRepositoryResource remote = SVNFileStorage.instance().asRepositoryResource(files[i], false);
			IRepositoryLocation location = remote.getRepositoryLocation();
			final ISVNConnector proxy = location.acquireSVNProxy();
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					for (int i = 0; i < SetPropertyOperation.this.propertyData.length && !monitor.isCanceled(); i++) {
					    final SVNProperty property = SetPropertyOperation.this.propertyData[i];
					    SetPropertyOperation.this.protectStep(new IUnprotectedOperation() {
	                        public void run(IProgressMonitor monitor) throws Exception {
	        					proxy.setProperty(current.getAbsolutePath(), property.name, property.value, Depth.infinityOrEmpty(SetPropertyOperation.this.isRecursive), ISVNConnector.Options.NONE, null, new SVNProgressMonitor(SetPropertyOperation.this, monitor, null));
	                        }
	                    }, monitor, SetPropertyOperation.this.propertyData.length);
					}
				}
			}, monitor, files.length);
			location.releaseSVNProxy(proxy);
		}
	}

	protected ISchedulingRule getSchedulingRule(File file) {
		return file.isDirectory() ? new LockingRule(file) : super.getSchedulingRule(file);
	}
	
}
