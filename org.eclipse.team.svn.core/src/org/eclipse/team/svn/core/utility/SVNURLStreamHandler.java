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

package org.eclipse.team.svn.core.utility;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.eclipse.team.svn.core.SVNTeamPlugin;

/**
 * SVN-specific URL stream handler
 * 
 * @author Alexander Gurov
 */
public class SVNURLStreamHandler extends URLStreamHandler {
	protected URL url;
	
	public SVNURLStreamHandler() {
	}
	
	public URL getURL() {
		return this.url;
	}

    protected URLConnection openConnection(URL u) throws IOException {
        return null;
    }
    
    public void setHost(String host) {
    	this.setURL(this.url, url.getProtocol(), host, url.getPort(), url.getAuthority(), url.getUserInfo(), url.getPath(), url.getQuery(), url.getRef());
    }
    
    protected void parseURL(URL u, String spec, int start, int limit) {
    	String protocol = u.getProtocol();
        if (!protocol.equals("file") &&
    		!protocol.equals("svn") &&
            !protocol.equals("http") &&
            !protocol.equals("https") &&
            !protocol.equals("svn+ssh")) {
    		String errMessage = SVNTeamPlugin.instance().getResource("Error.UnknownProtocol", new String[] {protocol});
            throw new RuntimeException(errMessage);
        }
    	this.url = u;
        super.parseURL(u, spec, start, limit);
    }
    
}
