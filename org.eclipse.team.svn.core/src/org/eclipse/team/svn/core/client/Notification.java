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
 * Notification information container
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL client library
 * is not EPL compatible and we won't to pin plug-in with concrete client implementation. So, the only way to do this is
 * providing our own client interface which will be covered by concrete client implementation.
 * 
 * @author Alexander Gurov
 */
public class Notification {
	/**
	 * The entry path
	 */
	public final String path;

	/**
	 * The action performed with the entry (see {@link NotifyAction}).
	 */
	public final int action;

	/**
	 * The entry kind (see {@link NodeKind}).
	 */
	public final int kind;

	/**
	 * The entry MIME-type
	 */
	public final String mimeType;

	/**
	 * The entry lock. Could be <code>null</code>
	 */
	public final Lock lock;

	/**
	 * The error message for the entry
	 */
	public final String errMsg;

	/**
	 * The entry content state (see {@link NotifyStatus}).
	 */
	public final int contentState;

	/**
	 * The entry properties state (see {@link NotifyStatus}).
	 */
	public final int propState;

	/**
	 * The entry revision
	 */
	public final long revision;

	/**
	 * the state of the lock of the item (see {@link LockStatus}).
	 */
	public final int lockState;

	public Notification(String path, int action, int kind, String mimeType, Lock lock, String errMsg, int contentState, int propState, int lockState, long revision) {
		this.path = path;
		this.action = action;
		this.kind = kind;
		this.mimeType = mimeType;
		this.lock = lock;
		this.errMsg = errMsg;
		this.contentState = contentState;
		this.propState = propState;
		this.revision = revision;
		this.lockState = lockState;
	}

}
