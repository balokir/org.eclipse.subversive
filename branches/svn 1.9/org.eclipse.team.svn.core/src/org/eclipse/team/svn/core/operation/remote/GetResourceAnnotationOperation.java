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

package org.eclipse.team.svn.core.operation.remote;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.BaseMessages;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNAnnotationCallback;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNConnector.Options;
import org.eclipse.team.svn.core.connector.SVNAnnotationData;
import org.eclipse.team.svn.core.connector.SVNConnectorException;
import org.eclipse.team.svn.core.connector.SVNErrorCodes;
import org.eclipse.team.svn.core.connector.SVNRevisionRange;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Resource annotation operation implementation 
 * 
 * @author Alexander Gurov
 */
public class GetResourceAnnotationOperation extends AbstractRepositoryOperation {
	protected SVNAnnotationData []annotatedLines;
	protected byte []content;
	protected long options;
	protected SVNRevisionRange revisions;
	protected boolean isRetryIfMergeInfoNotSupported;
	
	public GetResourceAnnotationOperation(IRepositoryResource resource, SVNRevisionRange revisions) {
		this(resource, revisions, ISVNConnector.Options.IGNORE_MIME_TYPE);
	}
	
	public GetResourceAnnotationOperation(IRepositoryResource resource, SVNRevisionRange revisions, long options) {
		super("Operation_GetAnnotation", SVNMessages.class, new IRepositoryResource[] {resource}); //$NON-NLS-1$
		this.revisions = revisions;
		this.options = options & ISVNConnector.CommandMasks.ANNOTATE;
	}
	
	public boolean getIncludeMerged() {
		return (this.options & ISVNConnector.Options.INCLUDE_MERGED_REVISIONS) != 0;
	}
	
	public void setIncludeMerged(boolean includeMerged) {
		this.options &= ~ISVNConnector.Options.INCLUDE_MERGED_REVISIONS;
		this.options |= includeMerged ? ISVNConnector.Options.INCLUDE_MERGED_REVISIONS : ISVNConnector.Options.NONE;
	}
	
	public void setRetryIfMergeInfoNotSupported(boolean isRetryIfMergeInfoNotSupported) {
		this.isRetryIfMergeInfoNotSupported = isRetryIfMergeInfoNotSupported;
	}
	
	public IRepositoryResource getRepositoryResource() {
		return this.operableData()[0];
	}

	public SVNAnnotationData []getAnnotatedLines() {
		return this.annotatedLines;
	}
	
	public byte []getContent() {
		return this.content;
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		final ArrayList<SVNAnnotationData> lines = new ArrayList<SVNAnnotationData>();
		IRepositoryResource resource = this.operableData()[0];
		IRepositoryLocation location = resource.getRepositoryLocation();
		ISVNConnector proxy = location.acquireSVNProxy();
		try {
//			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn blame " + url + "@" + resource.getPegRevision() + " -r 0:" + resource.getSelectedRevision() + " --username \"" + location.getUsername() + "\"\n");
			
			ISVNAnnotationCallback callback = new ISVNAnnotationCallback() {
				public void annotate(String line, SVNAnnotationData data) {
					lines.add(data);
					try {
						stream.write((line + "\n").getBytes()); //$NON-NLS-1$
					} catch (IOException e) { throw new RuntimeException(e); }
				}
			};
			
			try {
				proxy.annotate(
						SVNUtility.getEntryReference(resource),
						new SVNRevisionRange(this.revisions.from, this.revisions.to),
						this.options, ISVNConnector.DiffOptions.NONE, callback, new SVNProgressMonitor(this, monitor, null));
			} catch (SVNConnectorException ex) {
				/*
				 * If SVN server doesn't support merged revisions, then we re-call without this option
				 */
				if (this.isRetryIfMergeInfoNotSupported && 
					ex.getErrorId() == SVNErrorCodes.unsupportedFeature && 
					(this.options & Options.INCLUDE_MERGED_REVISIONS) != 0) {
					this.options &= ~Options.INCLUDE_MERGED_REVISIONS;
					proxy.annotate(
							SVNUtility.getEntryReference(resource),
							new SVNRevisionRange(this.revisions.from, this.revisions.to),
							this.options, ISVNConnector.DiffOptions.NONE, callback, new SVNProgressMonitor(this, monitor, null));
				} else {
					throw ex;
				}
			}
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
		this.annotatedLines = lines.toArray(new SVNAnnotationData[lines.size()]);
		this.content = stream.toByteArray();
	}
	
	protected String getShortErrorMessage(Throwable t) {
		if (t instanceof SVNConnectorException && ((SVNConnectorException)t).getErrorId() == SVNErrorCodes.clientIsBinaryFile) {
			return this.getOperationResource("Error_IsBinary"); //$NON-NLS-1$
		}
		return BaseMessages.format(super.getShortErrorMessage(t), new Object[] {this.operableData()[0].getName()});
	}
	

}
