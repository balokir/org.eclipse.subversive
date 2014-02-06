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
public interface ISSHSettingsStateListener
{
	public final String SSH_PASS_PHRASE = "sshPassPhrase";
	public final String SSH_PORT = "sshPort";
	public final String SSH_PRIVATE_KEY_PATH = "sshPrivateKeyPath";
	public final String SSH_USE_KEY_FILE = "sshUseKeyFile";
	public final String SSH_PASS_PHRASE_SAVED = "sshPassPhraseSaved";
	
	public void sshChanged(IRepositoryLocation where, String field, Object oldValue, Object newValue);
}
