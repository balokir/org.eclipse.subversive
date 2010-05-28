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
package org.eclipse.team.svn.revision.graph;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.revision.graph.cache.RepositoryCachesManager;
import org.eclipse.team.svn.revision.graph.preferences.SVNRevisionGraphPreferences;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * SVN Revision Graph plug-in implementation
 * 
 * @author Igor Burilo
 */
public class SVNRevisionGraphPlugin extends AbstractUIPlugin {

	private volatile static SVNRevisionGraphPlugin instance = null;
	
	private URL baseUrl;
	
	protected static List<Resource> disposeOnShutdownResources = new ArrayList<Resource>();
	
	protected RepositoryCachesManager cachesManager;
	
	public SVNRevisionGraphPlugin() {	
		SVNRevisionGraphPlugin.instance = this;
	}
	
    public static SVNRevisionGraphPlugin instance() {
    	return SVNRevisionGraphPlugin.instance;
    }
    
    public void start(BundleContext context) throws Exception {
		super.start(context);
		
		this.baseUrl = context.getBundle().getEntry("/"); //$NON-NLS-1$
						
		String cachePath = SVNRevisionGraphPreferences.getCacheString(this.getPreferenceStore(), SVNRevisionGraphPreferences.CACHE_DIRECTORY_NAME);
		this.cachesManager = new RepositoryCachesManager(new File(cachePath));
    }
    
    public void stop(BundleContext context) throws Exception {
    	if (disposeOnShutdownResources != null) {
			Iterator<Resource> iter = disposeOnShutdownResources.iterator();
			while (iter.hasNext()) {
				Resource resource = iter.next();
				if (!resource.isDisposed()) {
					resource.dispose();
				}
			}			
		}
    	
    	if (this.cachesManager != null) {
    		this.cachesManager.dispose();
    	}    	
    	
    	super.stop(context);
    }
    
    public ImageDescriptor getImageDescriptor(String path) {
    	try {
			return ImageDescriptor.createFromURL(new URL(this.baseUrl, path));
		} 
    	catch (MalformedURLException e) {
			LoggedOperation.reportError(SVNUIMessages.getErrorString("Error_GetImageDescriptor"), e); //$NON-NLS-1$
			return null;
		}
    }
    
    public static void disposeOnShutdown(Resource resource) {
		if (resource != null) {
			disposeOnShutdownResources.add(resource);
		}
	}
    
    public RepositoryCachesManager getRepositoryCachesManager() {
    	return this.cachesManager;
    }
	
}
