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
 * SVN connector wrapper cancel exception
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL connector library
 * is not EPL compatible and we won't to pin plug-in with concrete connector implementation. So, the only way to do this is
 * providing our own connector interface which will be covered by concrete connector implementation.
 * 
 * @author Alexander Gurov
 */
public class SVNConnectorCancelException extends SVNConnectorException {
	private static final long serialVersionUID = -1431358791852025035L;

	public SVNConnectorCancelException(String message) {
		super(message, SVNErrorCodes.cancelled, null);
	}

	public SVNConnectorCancelException(Throwable cause) {
		super(cause == null ? null : cause.getMessage(), SVNErrorCodes.cancelled, cause);
	}

	public SVNConnectorCancelException(String message, Throwable cause) {
		super(message, SVNErrorCodes.cancelled, cause);
	}

}
