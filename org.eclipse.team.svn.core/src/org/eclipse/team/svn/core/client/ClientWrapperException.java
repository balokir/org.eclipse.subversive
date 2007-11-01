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

package org.eclipse.team.svn.core.client;

/**
 * Basic SVN client wrapper exception
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL client library
 * is not EPL compatible and we won't to pin plug-in with concrete client implementation. So, the only way to do this is
 * providing our own client interface which will be covered by concrete client implementation.
 * 
 * @author Alexander Gurov
 */
public class ClientWrapperException extends Exception {
	private static final long serialVersionUID = 6066882107735517763L;

	protected boolean runtime;

	protected int errorId;

	public ClientWrapperException() {
		super();
		this.runtime = false;
	}

	public ClientWrapperException(String message) {
		super(message);
		this.runtime = false;
	}

	public ClientWrapperException(Throwable cause, boolean runtime) {
		super(cause);
		this.runtime = runtime;
	}

	public ClientWrapperException(String message, Throwable cause, boolean runtime) {
		super(message, cause);
		this.runtime = runtime;
	}

	public ClientWrapperException(String message, int errorId, Throwable cause, boolean runtime) {
		super(message, cause);
		this.runtime = runtime;
		this.errorId = errorId;
	}

	public boolean isRuntime() {
		return this.runtime;
	}

	public int getErrorId() {
		return this.errorId;
	}

}
