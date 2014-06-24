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

import java.util.StringTokenizer;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.connector.SVNProperty.BuiltIn;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNNullProgressMonitor;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.resource.ISVNStorage;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * "Add to svn::ignore" operation implementation
 * 
 * @author Alexander Gurov
 */
public class AddToSVNIgnoreOperation extends AbstractWorkingCopyOperation {
	protected int ignoreType;
	protected String pattern;

	public AddToSVNIgnoreOperation(IResource []resources, int ignoreType, String pattern) {
		super("Operation_AddToSVNIgnore", SVNMessages.class, resources); //$NON-NLS-1$
		
		this.ignoreType = ignoreType;
		this.pattern = pattern;
	}

	public AddToSVNIgnoreOperation(IResourceProvider provider, int ignoreType, String pattern) {
		super("Operation_AddToSVNIgnore", SVNMessages.class, provider); //$NON-NLS-1$
		
		this.ignoreType = ignoreType;
		this.pattern = pattern;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource []resources = this.operableData();
		
		final IRemoteStorage storage = SVNRemoteStorage.instance();
		
		for (int i = 0; i < resources.length && !monitor.isCanceled(); i++) {
			final IResource current = resources[i];
			
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					AddToSVNIgnoreOperation.this.handleResource(storage, current);
				}
			}, monitor, resources.length);
		}
	}

	protected void handleResource(IRemoteStorage storage, IResource current) throws Exception {
		IResource parent = current.getParent();
		IRepositoryLocation location = storage.getRepositoryLocation(parent);
		ISVNConnector proxy = location.acquireSVNProxy();
		try {
			AddToSVNIgnoreOperation.changeIgnoreProperty(proxy, this.ignoreType, this.pattern, FileUtility.getWorkingCopyPath(parent), current.getName());
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
	}
	
	public static void changeIgnoreProperty(ISVNConnector proxy, int ignoreType, String pattern, String path, String name) throws Exception {
		SVNProperty data = proxy.getProperty(new SVNEntryRevisionReference(path), BuiltIn.IGNORE, new SVNNullProgressMonitor());
		String ignoreValue = data == null ? "" : data.value; //$NON-NLS-1$
		String mask = null;
		switch (ignoreType) {
			case ISVNStorage.IGNORE_NAME: {
				mask = name;
				break;
			}
			case ISVNStorage.IGNORE_EXTENSION: {
			    String extension = new Path(path + "/" +name).getFileExtension(); //$NON-NLS-1$
			    if (extension != null) {
					mask = "*." + extension; //$NON-NLS-1$
			    }
				break;
			}
			case ISVNStorage.IGNORE_PATTERN: {
				mask = pattern;
				break;
			}
		}
		ignoreValue = AddToSVNIgnoreOperation.addMask(ignoreValue, mask);
		proxy.setProperty(new String[] {path}, new SVNProperty(BuiltIn.IGNORE, ignoreValue), Depth.EMPTY, ISVNConnector.Options.NONE, null, new SVNNullProgressMonitor());
	}
	
	protected static String addMask(String ignore, String mask) {
	    if (mask == null || mask.length() == 0) {
	        return ignore;
	    }
		StringTokenizer tok = new StringTokenizer(ignore, "\n", false); //$NON-NLS-1$
		boolean found = false;
		while (tok.hasMoreTokens()) {
			if (tok.nextToken().equals(mask)) {
				found = true;
				break;
			}
		}
		
		return found ? ignore : (ignore + (ignore.length() > 0 ? "\n" : "") + mask); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
}
