/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.local.property;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.BaseMessages;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IResourcePropertyProvider;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Concatenate new property value with already existing value (by retrieving it from SVN)
 *   
 * @author Igor Burilo
 */
public class ConcatenateProperyDataOperation extends AbstractActionOperation implements IResourcePropertyProvider {

	protected IResource resource;
	protected String propertyName;
	protected boolean isStringValue;	
	
	protected String stringValuesSeparator;
	protected String newStringValue;
	protected byte[] newByteValue;	
	protected IResourcePropertyProvider propertyProvider;
	
	protected SVNProperty property;
	
	public ConcatenateProperyDataOperation(IResource resource, String propertyName, IResourcePropertyProvider propertyProvider) {
		this(resource, propertyName, true);
		this.propertyProvider = propertyProvider;
	}
	
	public ConcatenateProperyDataOperation(IResource resource, String propertyName, String newStringValue) {
		this(resource, propertyName, true);		
		this.newStringValue = newStringValue;
	}
	
	public ConcatenateProperyDataOperation(IResource resource, String propertyName, byte[] newByteValue) {
		this(resource, propertyName, false);		
		this.newByteValue = newByteValue;
	}
	
	private ConcatenateProperyDataOperation(IResource resource, String propertyName, boolean isStringValue) {
		super("Operation_ConcatenatePropertyData", SVNMessages.class); //$NON-NLS-1$
		this.resource = resource;
		this.propertyName = propertyName;
		this.isStringValue = isStringValue;		
		this.stringValuesSeparator = "\r\n"; //$NON-NLS-1$
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		final String wcPath = FileUtility.getWorkingCopyPath(this.resource);
		IRepositoryLocation location = SVNRemoteStorage.instance().getRepositoryLocation(this.resource);
		final ISVNConnector proxy = location.acquireSVNProxy();
		SVNProperty existingProperty;
		try {
			existingProperty = proxy.getProperty(new SVNEntryRevisionReference(wcPath), this.propertyName, null, new SVNProgressMonitor(this, monitor, null));
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
		if (existingProperty != null && existingProperty.value != null) {
			if (this.isStringValue) {
				String value = existingProperty.value;												
				value += this.stringValuesSeparator + this.getNewStringValue();
				this.property = new SVNProperty(this.propertyName, value);
			} else {
				byte[] existingData = existingProperty.value.getBytes();
				byte[] newData = new byte[existingData.length + this.getNewByteValue().length];
				System.arraycopy(existingData, 0, newData, 0, existingData.length);
				System.arraycopy(this.getNewByteValue(), 0, newData, existingData.length, this.getNewByteValue().length);
				this.property = new SVNProperty(this.propertyName, new String(newData));	
			}
		} else {
			if (this.isStringValue) {
				this.property = new SVNProperty(this.propertyName, this.getNewStringValue());
			} else {
				this.property = new SVNProperty(this.propertyName, new String(this.getNewByteValue()));	
			}
		}
	}

	protected String getNewStringValue() {
		if (!this.isStringValue) {
			return null;
		} 
		return this.propertyProvider != null ? this.propertyProvider.getProperties()[0].value : this.newStringValue;
	}
	
	protected byte[] getNewByteValue() {
		return this.newByteValue;
	}
	
	public void setStringValuesSeparator(String stringValuesSeparator) {
		this.stringValuesSeparator = stringValuesSeparator;
	}
	
	public IResource getLocal() {
		return this.resource;
	}

	public SVNProperty[] getProperties() {
		return new SVNProperty[] {this.property};
	}

	public IRepositoryResource getRemote() {
		return SVNRemoteStorage.instance().asRepositoryResource(this.resource);
	}

	public boolean isEditAllowed() {
		return false;
	}

	public void refresh() {
		
	}
	
	protected String getShortErrorMessage(Throwable t) {
		return BaseMessages.format(super.getShortErrorMessage(t), new Object[] {this.propertyName, this.resource.getName()});
	}
}
