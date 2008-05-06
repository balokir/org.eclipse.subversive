/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexey Mikoyan - Initial implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.decorator;

import java.util.HashMap;
import java.util.Map;

/**
 * Define variables that can be used at the time of resource decoration 
 *
 * @author Alexey Mikoyan
 *
 */
public class TextVariableSetProvider implements IVariableSetProvider {
	
	public static final String DOMAIN_NAME = "TextDecoratorVariable";
	
	public static final String NAME_OF_LOCATION_URL = "location_url";
	public static final String NAME_OF_LOCATION_LABEL = "location_label";
	public static final String NAME_OF_ROOT_PREFIX = "root_prefix";
	public static final String NAME_OF_ASCENDANT = "ascendant";
	public static final String NAME_OF_DESCENDANT = "descendant";
	public static final String NAME_OF_FULLNAME = "fullname";
	public static final String NAME_OF_RESOURCE_URL = "resource_url";
	public static final String NAME_OF_SHORT_RESOURCE_URL = "short_url";
	public static final String NAME_OF_REMOTE_NAME = "remote_name";
	public static final String NAME_OF_DATE = "date";
	public static final String NAME_OF_NAME = "name";
	public static final String NAME_OF_AUTHOR = "author";
	public static final String NAME_OF_REVISION = "revision";
	public static final String NAME_OF_OUTGOING_FLAG = "outgoing_flag";
	public static final String NAME_OF_ADDED_FLAG = "added_flag";
	
	public static final IVariable VAR_LOCATION_URL = new PredefinedVariable(TextVariableSetProvider.DOMAIN_NAME, TextVariableSetProvider.NAME_OF_LOCATION_URL);
	public static final IVariable VAR_LOCATION_LABEL = new PredefinedVariable(TextVariableSetProvider.DOMAIN_NAME, TextVariableSetProvider.NAME_OF_LOCATION_LABEL);
	public static final IVariable VAR_ROOT_PREFIX = new PredefinedVariable(TextVariableSetProvider.DOMAIN_NAME, TextVariableSetProvider.NAME_OF_ROOT_PREFIX);
	public static final IVariable VAR_ASCENDANT = new PredefinedVariable(TextVariableSetProvider.DOMAIN_NAME, TextVariableSetProvider.NAME_OF_ASCENDANT);
	public static final IVariable VAR_DESCENDANT = new PredefinedVariable(TextVariableSetProvider.DOMAIN_NAME, TextVariableSetProvider.NAME_OF_DESCENDANT);
	public static final IVariable VAR_FULLNAME = new PredefinedVariable(TextVariableSetProvider.DOMAIN_NAME, TextVariableSetProvider.NAME_OF_FULLNAME);
	public static final IVariable VAR_RESOURCE_URL = new PredefinedVariable(TextVariableSetProvider.DOMAIN_NAME, TextVariableSetProvider.NAME_OF_RESOURCE_URL);
	public static final IVariable VAR_SHORT_RESOURCE_URL = new PredefinedVariable(TextVariableSetProvider.DOMAIN_NAME, TextVariableSetProvider.NAME_OF_SHORT_RESOURCE_URL);
	public static final IVariable VAR_REMOTE_NAME = new PredefinedVariable(TextVariableSetProvider.DOMAIN_NAME, TextVariableSetProvider.NAME_OF_REMOTE_NAME);
	public static final IVariable VAR_DATE = new PredefinedVariable(TextVariableSetProvider.DOMAIN_NAME, TextVariableSetProvider.NAME_OF_DATE);
	public static final IVariable VAR_NAME = new PredefinedVariable(TextVariableSetProvider.DOMAIN_NAME, TextVariableSetProvider.NAME_OF_NAME);
	public static final IVariable VAR_AUTHOR = new PredefinedVariable(TextVariableSetProvider.DOMAIN_NAME, TextVariableSetProvider.NAME_OF_AUTHOR);
	public static final IVariable VAR_REVISION = new PredefinedVariable(TextVariableSetProvider.DOMAIN_NAME, TextVariableSetProvider.NAME_OF_REVISION);
	public static final IVariable VAR_OUTGOING_FLAG = new PredefinedVariable(TextVariableSetProvider.DOMAIN_NAME, TextVariableSetProvider.NAME_OF_OUTGOING_FLAG);
	public static final IVariable VAR_ADDED_FLAG = new PredefinedVariable(TextVariableSetProvider.DOMAIN_NAME, TextVariableSetProvider.NAME_OF_ADDED_FLAG);

	public static final IVariable CENTER_VARIABLE = TextVariableSetProvider.VAR_NAME;
	
	private static final Map name2Variable = new HashMap();
	
	static {
		TextVariableSetProvider.name2Variable.put(TextVariableSetProvider.NAME_OF_LOCATION_URL, TextVariableSetProvider.VAR_LOCATION_URL);
		TextVariableSetProvider.name2Variable.put(TextVariableSetProvider.NAME_OF_LOCATION_LABEL, TextVariableSetProvider.VAR_LOCATION_LABEL);
		TextVariableSetProvider.name2Variable.put(TextVariableSetProvider.NAME_OF_ROOT_PREFIX, TextVariableSetProvider.VAR_ROOT_PREFIX);
		TextVariableSetProvider.name2Variable.put(TextVariableSetProvider.NAME_OF_ASCENDANT, TextVariableSetProvider.VAR_ASCENDANT);
		TextVariableSetProvider.name2Variable.put(TextVariableSetProvider.NAME_OF_DESCENDANT, TextVariableSetProvider.VAR_DESCENDANT);
		TextVariableSetProvider.name2Variable.put(TextVariableSetProvider.NAME_OF_FULLNAME, TextVariableSetProvider.VAR_FULLNAME);
		TextVariableSetProvider.name2Variable.put(TextVariableSetProvider.NAME_OF_RESOURCE_URL, TextVariableSetProvider.VAR_RESOURCE_URL);
		TextVariableSetProvider.name2Variable.put(TextVariableSetProvider.NAME_OF_SHORT_RESOURCE_URL, TextVariableSetProvider.VAR_SHORT_RESOURCE_URL);
		TextVariableSetProvider.name2Variable.put(TextVariableSetProvider.NAME_OF_REMOTE_NAME, TextVariableSetProvider.VAR_REMOTE_NAME);
		TextVariableSetProvider.name2Variable.put(TextVariableSetProvider.NAME_OF_DATE, TextVariableSetProvider.VAR_DATE);
		TextVariableSetProvider.name2Variable.put(TextVariableSetProvider.NAME_OF_NAME, TextVariableSetProvider.VAR_NAME);
		TextVariableSetProvider.name2Variable.put(TextVariableSetProvider.NAME_OF_AUTHOR, TextVariableSetProvider.VAR_AUTHOR);
		TextVariableSetProvider.name2Variable.put(TextVariableSetProvider.NAME_OF_REVISION, TextVariableSetProvider.VAR_REVISION);
		TextVariableSetProvider.name2Variable.put(TextVariableSetProvider.NAME_OF_OUTGOING_FLAG, TextVariableSetProvider.VAR_OUTGOING_FLAG);
		TextVariableSetProvider.name2Variable.put(TextVariableSetProvider.NAME_OF_ADDED_FLAG, TextVariableSetProvider.VAR_ADDED_FLAG);
	}
	
	public static final TextVariableSetProvider instance = new TextVariableSetProvider();
	
	public IVariable getCenterVariable() {
		return TextVariableSetProvider.CENTER_VARIABLE;
	}

	public String getDomainName() {
		return TextVariableSetProvider.DOMAIN_NAME;
	}

	public IVariable getVariable(String name) {
		return (IVariable)TextVariableSetProvider.name2Variable.get(name);
	}
	
	protected TextVariableSetProvider() {
		
	}
	
}
