/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.extension.factory;

/**
 * Mail settings provider
 * 
 * @author Sergiy Logvin
 */
public interface IReportingDescriptor {
	/**
	 * Returns report addressee
	 * 
	 * @return report addressee
	 */
	public String getEmailTo();

	/**
	 * Returns report sender
	 * 
	 * @return report sender
	 */
	public String getEmailFrom();

	/**
	 * Returns mail server host
	 * 
	 * @return mail server host
	 */
	public String getHost();

	/**
	 * Returns mail server port
	 * 
	 * @return mail server port
	 */
	public String getPort();

	/**
	 * Returns plug-in name
	 * 
	 * @return plug-in name
	 */
	public String getProductName();

	/**
	 * Returns plug-in version
	 * 
	 * @return plug-in version
	 */
	public String getProductVersion();

	/**
	 * Returns the product tracker URL.
	 * 
	 * @return the product tracker URL
	 */
	public String getTrackerUrl();

	/**
	 * Returns <code>true</code> if a bug tracker supports HTML in reports
	 * 
	 * @return <code>true</code> if tracker supports HTML in reports
	 */
	public boolean isTrackerSupportsHTML();
}
