/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.svnstorage;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;

/**
 * Cached proxy credentials manager.
 * 
 * @author Alexei Goncharov
 */
public class SVNCachedProxyCredentialsManager {
	protected String username;
	protected String password;
	
	public SVNCachedProxyCredentialsManager(IProxyService proxyService) {	
		IProxyData proxyData = proxyService.getProxyData(IProxyData.HTTP_PROXY_TYPE);
		this.username = proxyData.isRequiresAuthentication() ? proxyData.getUserId() : ""; //$NON-NLS-1$
		this.password = proxyData.isRequiresAuthentication() ? proxyData.getPassword() : ""; //$NON-NLS-1$
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
}
