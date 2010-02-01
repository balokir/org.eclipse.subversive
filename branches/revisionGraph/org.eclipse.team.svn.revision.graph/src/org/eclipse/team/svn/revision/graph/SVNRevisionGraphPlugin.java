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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.svn.core.operation.LoggedOperation;
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
	
	public SVNRevisionGraphPlugin() {	
		SVNRevisionGraphPlugin.instance = this;
	}
	
    public static SVNRevisionGraphPlugin instance() {
    	return SVNRevisionGraphPlugin.instance;
    }
    
    public void start(BundleContext context) throws Exception {
		super.start(context);
	
		this.baseUrl = context.getBundle().getEntry("/"); //$NON-NLS-1$
		
		//TODO add our start
    }
    
    public void stop(BundleContext context) throws Exception {    	
    	//TODO add our stop
    	
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
	
}
