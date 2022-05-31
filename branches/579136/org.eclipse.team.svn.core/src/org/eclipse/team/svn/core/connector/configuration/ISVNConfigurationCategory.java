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

package org.eclipse.team.svn.core.connector.configuration;

import org.eclipse.team.svn.core.connector.SVNConnectorException;

/**
 * Interface for reading and modifying configuration categories.
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL connector
 * library is not EPL compatible and we won't to pin plug-in with concrete connector implementation. So, the only way to
 * do this is providing our own connector interface which will be covered by concrete connector implementation.
 * 
 * @author Alexander Gurov
 */
public interface ISVNConfigurationCategory {
	/**
	 * Returns the names of all the sections in the configuration category.
	 */
	public Iterable<String> listSections();
	
	/**
	 * Calls <code>handler</code> once for each option in the configuration category's section.
	 */
	public void enumerateOptions(String section, Enumerator handler);
	
	/**
	 * Returns the value of a configuration option.
	 * @param section section name
	 * @param option option name
	 * @param defaultValue default value, if option is not set
	 * @return
	 */
	public String get(String section, String option, String defaultValue);
	
	/**
	 * Returns the boolean value of a configuration option. Converts from: true/false, yes/no, 1/0, on/off.
	 * @param section section name
	 * @param option option name
	 * @param defaultValue default value, if option is not set
	 * @return
	 * @throws SVNConnectorException if the value cannot be parsed.
	 */
	public boolean get(String section, String option, boolean defaultValue) throws SVNConnectorException;
	
	/**
	 * Returns the long value of a configuration option.
	 * @param section section name
	 * @param option option name
	 * @param defaultValue default value, if option is not set
	 * @return
	 * @throws SVNConnectorException if the value cannot be parsed.
	 */
	public long get(String section, String option, long defaultValue) throws SVNConnectorException;
	
	/**
	 * Returns the "tristate" value of a configuration option.
	 * @param section section name
	 * @param option option name
	 * @param unknown the value used for "tristate/Unknown"
	 * @param defaultValue default value, if option is not set
	 * @return
	 * @throws SVNConnectorException if the value cannot be parsed.
	 */
	public Boolean getTristate(String section, String option, String unknown, Boolean defaultValue) throws SVNConnectorException;
	
	/**
	 * Returns the TRUE/FALSE/ASK value of a configuration option.
	 * @param section section name
	 * @param option option name
	 * @param defaultValue default value, if option is not set
	 * @return
	 * @throws SVNConnectorException if the value cannot be parsed.
	 */
	public String getYesNoAsk(String section, String option, String defaultValue) throws SVNConnectorException;

	/**
	 * Defines the option's value.
	 * 
	 * @param section section name
	 * @param option option name
	 * @param value value, if <code>null</code>, then the option will be deleted.
	 */
	public void set(String section, String option, String value);
	
	/**
	 * Defines the option's value to represent a boolean.
	 * 
	 * @param section section name
	 * @param option option name
	 * @param value boolean type value
	 */
	public void set(String section, String option, boolean value);
	
	/**
	 * Defines the option's value to represent a long integer.
	 * 
	 * @param section section name
	 * @param option option name
	 * @param value long type value
	 */
	public void set(String section, String option, long value);

    /**
     * Interface for {@link ISVNConfigurationCategory#enumerate} callback handlers.
     */
    public interface Enumerator
    {
        public void option(String name, String value);
    }
}
