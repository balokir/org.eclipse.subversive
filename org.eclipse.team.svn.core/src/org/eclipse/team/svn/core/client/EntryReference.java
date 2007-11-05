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

package org.eclipse.team.svn.core.client;

/**
 * Copy source information container
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL client library
 * is not EPL compatible and we won't to pin plug-in with concrete client implementation. So, the only way to do this is
 * providing our own client interface which will be covered by concrete client implementation.
 * 
 * @author Alexander Gurov
 */
public class EntryReference {
	/**
	 * The copy source path or URL.
	 */
	public final String path;

	/**
	 * The copy source revision. If null could be treated as {@link Revision#HEAD} or ignored depending on calling context.
	 */
	public final Revision revision;

	/**
	 * The copy source peg revision. If null could be treated as {@link Revision#HEAD} or ignored depending on calling context.
	 */
	public final Revision pegRevision;

	/**
	 * The {@link EntryReference} instance could be initialized only once because all fields are final
	 * 
	 * @param path
	 *            the source path
	 */
	public EntryReference(String path) {
		this(path, null, null);
	}

	/**
	 * The {@link EntryReference} instance could be initialized only once because all fields are final
	 * 
	 * @param path
	 *            the source path
	 * @param revision
	 *            the source revision.
	 * @param pegRevision
	 *            the source peg revision
	 */
	public EntryReference(String path, Revision revision, Revision pegRevision) {
		this.path = path;
		this.revision = revision;
		this.pegRevision = pegRevision;
	}

}
