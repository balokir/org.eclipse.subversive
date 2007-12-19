/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
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
import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.ISVNAnnotationCallback;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNRevision;
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
	protected String [][]annotatedLines;
	protected byte []content;

	public GetResourceAnnotationOperation(IRepositoryResource resource) {
		super("Operation.GetAnnotation", new IRepositoryResource[] {resource});
	}
	
	public IRepositoryResource getRepositoryResource() {
		return this.operableData()[0];
	}

	public String [][]getAnnotatedLines() {
		return this.annotatedLines;
	}
	
	public byte []getContent() {
		return this.content;
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		final ArrayList lines = new ArrayList();
		IRepositoryResource resource = this.operableData()[0];
		IRepositoryLocation location = resource.getRepositoryLocation();
		ISVNConnector proxy = location.acquireSVNProxy();
		try {
//			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn blame " + url + "@" + resource.getPegRevision() + " -r 0:" + resource.getSelectedRevision() + " --username \"" + location.getUsername() + "\"\n");
			proxy.annotate(
				SVNUtility.getEntryReference(resource),
				SVNRevision.fromNumber(0),
				resource.getSelectedRevision(),
				ISVNConnector.Options.NONE, new ISVNAnnotationCallback() {
					protected int lineNumber = 0;
					protected String noAuthor = SVNTeamPlugin.instance().getResource("SVNInfo.NoAuthor");
					
					public void annotate(String line, long revision, long date, String author, long merged_revision, long merged_date, String merged_author, String merged_path) {
						String []row = new String[] {
							String.valueOf(revision),
							author == null ? this.noAuthor : author,
							String.valueOf(++this.lineNumber),
						};
						lines.add(row);
						try {
							stream.write((line + "\n").getBytes());
						} 
						catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				}, 
				new SVNProgressMonitor(this, monitor, null)
			);
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
		this.annotatedLines = (String [][])lines.toArray(new String[lines.size()][]);
		this.content = stream.toByteArray();
	}
	
	protected String getShortErrorMessage(Throwable t) {
		return MessageFormat.format(super.getShortErrorMessage(t), new String[] {this.operableData()[0].getUrl()});
	}

}
