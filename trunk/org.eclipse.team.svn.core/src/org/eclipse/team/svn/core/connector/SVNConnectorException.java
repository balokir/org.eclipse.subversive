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

package org.eclipse.team.svn.core.connector;

/**
 * Basic SVN connector exception wrapper
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL connector library
 * is not EPL compatible and we won't to pin plug-in with concrete connector implementation. So, the only way to do this is
 * providing our own connector interface which will be covered by concrete connector implementation.
 * 
 * @author Alexander Gurov
 */
public class SVNConnectorException extends Exception {
	private static final long serialVersionUID = 6066882107735517763L;

	protected int errorId;

	public SVNConnectorException(String message) {
		super(message);
		this.errorId = SVNErrorCodes.NO_ERROR_CODE;
	}

	public SVNConnectorException(Throwable cause) {
		super(cause);
		this.errorId = SVNErrorCodes.NO_ERROR_CODE;
	}

	public SVNConnectorException(String message, Throwable cause) {
		super(message, cause);
		this.errorId = SVNErrorCodes.NO_ERROR_CODE;
	}

	public SVNConnectorException(String message, int errorId, Throwable cause) {
		super(message, cause);
		this.errorId = errorId;
	}

	public int getErrorId() {
		return this.errorId;
	}

}
