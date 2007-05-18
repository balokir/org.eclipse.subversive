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

package org.eclipse.team.svn.client.javahl;

import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @author Alexander Gurov
 */
public class JavaHLPlugin extends Plugin {
	private static JavaHLPlugin plugin;
	private String location;
	
	public JavaHLPlugin() {
		super();
		JavaHLPlugin.plugin = this;
	}

	public String getLocation() {
	    return this.location;
	}
	
    public String getVersionString() {
        return (String)this.getBundle().getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION);
    }
    
    public String getResource(String key) {
        return FileUtility.getResource(Platform.getResourceBundle(this.getBundle()), key);
    }
    
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		URL url = Platform.asLocalURL(context.getBundle().getEntry("/"));
		this.location = url.getFile();
		if (this.location.startsWith("/")) {
		    this.location = this.location.substring(1);
		}
		if (this.location.endsWith("/")) {
		    this.location = this.location.substring(0, this.location.length() - 1);
		}
	}

	public static JavaHLPlugin instance() {
		return JavaHLPlugin.plugin;
	}

}
