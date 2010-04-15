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
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IResourcePropertyProvider;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Generate external property value by provided info
 * 
 * @author Igor Burilo
 */
public class GenerateExternalsPropertyOperation extends AbstractActionOperation implements IResourcePropertyProvider {

	protected IResource resource;
	protected String url;
	protected SVNRevision revision;
	protected String localPath;
	protected boolean isPriorToSVN15Format;
	
	protected SVNProperty property;
	
	public GenerateExternalsPropertyOperation(IResource resource, String url, SVNRevision revision, String localPath, boolean isPriorToSVN15Format) {		
		super("Operation_GenerateExternalsProperty", SVNMessages.class); //$NON-NLS-1$
		this.resource = resource;
		this.url = url;
		this.revision = revision;
		this.localPath= localPath;
		this.isPriorToSVN15Format = isPriorToSVN15Format;
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		this.preprocessInputParameters();
		
		StringBuffer value = new StringBuffer();
		if (this.isPriorToSVN15Format) {
			//local -r 10 url								
			value.append(this.localPath);
			if (this.getStrRevision() != null) {
				value.append(" -r ").append(this.getStrRevision()); //$NON-NLS-1$
			}
			value.append(" ").append(this.url); //$NON-NLS-1$
		} else {
			//-r 10 url local
			if (this.getStrRevision() != null) {
				value.append("-r ").append(this.getStrRevision()).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
			}
			value.append(this.url);
			value.append(" ").append(this.localPath); //$NON-NLS-1$
		}			
		this.property = new SVNProperty(SVNProperty.BuiltIn.EXTERNALS, value.toString());
	}
	
	protected void preprocessInputParameters() {		
		this.url = SVNUtility.encodeURL(this.url);
		
		if (this.localPath.contains(" ")) { //$NON-NLS-1$
			this.localPath = "\"" + this.localPath + "\""; //$NON-NLS-1$ //$NON-NLS-2$
		}		
	}
	
	protected String getStrRevision() {
//		if (this.revision.getKind() == SVNRevision.Kind.DATE) {
//			//Example: 2006-02-17 15:30 +0230				
//			SimpleDateFormat formatter = new SimpleDateFormat("{yyyy-dd-MM}");
//			return formatter.format(((SVNRevision.Date) this.revision).getDate());				
//		} else 
		if (this.revision.getKind() == SVNRevision.Kind.NUMBER) {
			long number = ((SVNRevision.Number) this.revision).getNumber();
			if (number != -1) {
				return String.valueOf(number);
			}				
		}
		return null;
	}

	public SVNProperty[] getProperties() {
		return new SVNProperty[]{this.property};
	}		
	
	public IResource getLocal() {
		return this.resource;
	}

	public IRepositoryResource getRemote() {
		return SVNRemoteStorage.instance().asRepositoryResource(this.resource);
	}

	public boolean isEditAllowed() {
		return false;
	}

	public void refresh() {						
	}		
}
