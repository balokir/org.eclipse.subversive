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

package org.eclipse.team.svn.core.resource.events;

import org.eclipse.team.svn.core.resource.IRepositoryLocation;

/**
 * Repository location change listener
 * 
 * @author Alexander Gurov
 */
public interface ISSLSettingsStateListener
{
	public final String SSL_CERTIFICATE_PATH = "sslCertificatePath";
	public final String SSL_PASS_PHRASE = "sslPassPhrase";
	public final String SSL_PASS_PHRASE_SAVED = "sslPassPhraseSaved";
	public final String SSL_AUTHENTICATION_ENABLED = "sslAuthenticationEnabled";
	
	public void sslChanged(IRepositoryLocation where, String field, Object oldValue, Object newValue);
}
