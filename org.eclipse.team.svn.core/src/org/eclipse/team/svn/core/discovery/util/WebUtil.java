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
package org.eclipse.team.svn.core.discovery.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.UnknownHostException;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.svn.core.SVNTeamPlugin;

/**
 * @author i.burilo
 */
public class WebUtil {

	private static int STATUS_OK = 200;
	
	/**
	 * Download an HTTP-based resource
	 * 
	 * @param target
	 *            the target file to which the content is saved
	 * @param sourceUrl
	 *            the web location of the content
	 * @param monitor
	 *            the monitor
	 * @throws IOException
	 *             if a network or IO problem occurs
	 */
	public static void downloadResource(File target, URL sourceUrl, IProgressMonitor monitor) throws IOException {		
		monitor.beginTask(NLS.bind(Messages.WebUtil_task_retrievingUrl, sourceUrl.toString()), IProgressMonitor.UNKNOWN);		
		try {		
			WebUtil.initProxySettings(sourceUrl.getHost());
			
			HttpURLConnection con = (HttpURLConnection) sourceUrl.openConnection();    	 
			int result = con.getResponseCode();
			if (result == WebUtil.STATUS_OK) {
				InputStream in = con.getInputStream();
				try {
					in = new BufferedInputStream(in);
					OutputStream out = new BufferedOutputStream(new FileOutputStream(target));
					try {
						int i;
						while ((i = in.read()) != -1) {
							out.write(i);
						}
					} catch (IOException e) {
						// avoid partial content
						out.close();
						target.delete();
						throw e;
					} finally {
						out.close();
					}
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException ie) {
							//ignore
						}
					}					
				}
			} else {
				throw new IOException(NLS.bind(Messages.WebUtil_cannotDownload, sourceUrl.toString(), result));
			}							
		} finally {						
			monitor.done();
		}
	}
	
	/**
	 * Verify availability of resources at the given web locations. Normally this would be done using an HTTP HEAD.
	 * 
	 * @param locations
	 *            the locations of the resource to verify
	 * @param one
	 *            indicate if only one of the resources must exist
	 * @param monitor
	 *            the monitor
	 * @return true if the resource exists
	 */
	public static boolean verifyAvailability(URL[] locations, boolean one, IProgressMonitor monitor) {
		if (locations.length == 0) {
			throw new IllegalArgumentException();
		}		
		monitor.beginTask(NLS.bind(Messages.WebUtil_task_verifyingUrl, locations[0].toString()), IProgressMonitor.UNKNOWN);
		try {						
			WebUtil.initProxySettings(locations[0].getHost());
			
			int countFound = 0;
			for (URL location : locations) {												
				if (monitor.isCanceled()) {
					return false;
				}
				
				int result;
				try {
					HttpURLConnection con = (HttpURLConnection) location.openConnection();
					result = con.getResponseCode();
				} catch (IOException e) {
					if (!one || e instanceof UnknownHostException) {
						return false;
					}
					continue;
				}
				if (result == WebUtil.STATUS_OK) {
					++countFound;
					if (one) {
						return true;
					}
				} else {
					if (!one) {
						return false;
					}
				}
			}
			return countFound == locations.length;			
		} finally {
			monitor.done();
		}
	}
	
	private static void initProxySettings(String host) {
		IProxyService proxyService = SVNTeamPlugin.instance().getProxyService();				
		final IProxyData proxyData = proxyService.getProxyDataForHost(host, IProxyData.HTTP_PROXY_TYPE);
		proxyService.getProxyData();
		
		if (proxyService.isProxiesEnabled() && proxyData != null) {						
			String proxyHost = proxyData.getHost();
			int proxyPort = proxyData.getPort();
			// change the IProxyData default port to the Java default port
			if (proxyPort == -1) {
				proxyPort = 0;
			}			
			System.getProperties().put("http.proxyHost", proxyHost);
		    System.getProperties().put("http.proxyPort", String.valueOf(proxyPort));  	
			
			if (proxyData.isRequiresAuthentication()) {
				Authenticator.setDefault(new Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						char[] pswd = proxyData.getPassword() == null ? new char[0] :proxyData.getPassword().toCharArray(); 
						return new PasswordAuthentication(proxyData.getUserId(), pswd);
		  	    }});
			}
		}
	}
		
}
