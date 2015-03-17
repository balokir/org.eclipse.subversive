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

package org.eclipse.team.svn.core.connector;


/**
 * SVN post-commit hook's message holder
 * 
 * @author Alexander Gurov
 */
public class SVNCommitStatus {
	public final String message;
	public final String repository;
	public final long revision;
	public final String author;
	public final long date;

	public SVNCommitStatus(String message, String repository, long revision, long date, String author) {
		this.message = message;
		this.repository = repository;
		this.revision = revision;
		this.date = date;
		this.author = author;
	}
}
