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

package org.eclipse.team.svn.core.extension.factory;

import org.eclipse.team.svn.core.client.ISVNConnector;

/**
 * Wraps real factories in order to produce wrapped SVN clients.
 * 
 * @author Alexander Gurov
 */
public class ThreadNameModifierFactory implements ISVNConnectorFactory {
	protected ISVNConnectorFactory factory;

	public ThreadNameModifierFactory(ISVNConnectorFactory factory) {
		this.factory = factory;
	}

	public String getClientVersion() {
		return this.factory.getClientVersion();
	}

	public String getCompatibilityVersion() {
		return this.factory.getCompatibilityVersion();
	}

	public String getId() {
		return this.factory.getId();
	}

	public String getName() {
		return this.factory.getName();
	}

	public String getVersion() {
		return this.factory.getVersion();
	}

	public int getSupportedFeatures() {
		return this.factory.getSupportedFeatures();
	}
	
	public int getSVNAPIVersion() {
		return this.factory.getSVNAPIVersion();
	}

	public ISVNConnector newInstance() {
		return new ThreadNameModifier(this.factory.newInstance());
	}

}
