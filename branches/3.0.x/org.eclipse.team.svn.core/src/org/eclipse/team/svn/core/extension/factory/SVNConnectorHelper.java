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

package org.eclipse.team.svn.core.extension.factory;


/**
 * Helper class for svn connectors
 * 
 * @author Igor Burilo
 */
public class SVNConnectorHelper {

	public static String getConnectorName(ISVNConnectorFactory connectorFactory) {
		if (connectorFactory.getVersion().compareTo("2.2.1.I20090925-2100") > 0) { //$NON-NLS-1$
			return connectorFactory.getName();
		} else {
			return connectorFactory.getName() + " (" + connectorFactory.getClientVersion().replace('\n', ' ') + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}
