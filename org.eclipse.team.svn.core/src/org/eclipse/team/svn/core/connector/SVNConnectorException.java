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
	protected ErrorMessage []messageStack;

	public SVNConnectorException(String message) {
		super(message);
		this.errorId = SVNErrorCodes.NO_ERROR_CODE;
	}

	public SVNConnectorException(Throwable cause) {
		super(cause);
		this.errorId = SVNErrorCodes.NO_ERROR_CODE;
	}

	public SVNConnectorException(String message, Throwable cause) {
		this(message, SVNErrorCodes.NO_ERROR_CODE, cause);
	}

	public SVNConnectorException(String message, int errorId, Throwable cause) {
		this(message, errorId, cause, null);
	}

	/**
	 * @since 1.9
	 */
	public SVNConnectorException(String message, int errorId, Throwable cause, ErrorMessage []messageStack) {
		super(message, cause);
		this.errorId = errorId;
		this.messageStack = messageStack;
	}

	/**
	 * @since 1.9
	 */
    public ErrorMessage []getMessageStack() {
        return this.messageStack;
    }

	public int getErrorId() {
		return this.errorId;
	}

	public static final class ErrorMessage
	{
        /** 
         * @return The APR error code associated with the message 
         */
        public final int code;
        /** 
         * @return The error message text
         */
        public final String message;
        /** 
         * A flag indicating whether this is a generic message for the APR error code, or a more specific message 
         * generated by the native libraries. 
         */
        public final boolean generic;
        
        public ErrorMessage(int code, String message, boolean generic)
        {
            this.code = code;
            this.message = message;
            this.generic = generic;
        }
	}
}
