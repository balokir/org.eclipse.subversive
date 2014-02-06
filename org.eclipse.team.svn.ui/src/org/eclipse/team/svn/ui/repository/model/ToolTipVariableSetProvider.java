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

package org.eclipse.team.svn.ui.repository.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.team.svn.ui.decorator.IVariable;
import org.eclipse.team.svn.ui.decorator.IVariableSetProvider;
import org.eclipse.team.svn.ui.decorator.PredefinedVariable;

/**
 * Define variables that can be used at the time of tootip creation
 *
 * @author Alexey Mikoyan
 *
 */
public class ToolTipVariableSetProvider implements IVariableSetProvider {

	public static final String DOMAIN_NAME = "ToolTipDecoratorVariable"; //$NON-NLS-1$
	
	public static final String NAME_OF_NAME = "name"; //$NON-NLS-1$
	public static final String NAME_OF_URL = "URL"; //$NON-NLS-1$
	public static final String NAME_OF_LAST_CHANGE_DATE = "last_change_date"; //$NON-NLS-1$
	public static final String NAME_OF_LAST_AUTHOR = "last_author"; //$NON-NLS-1$
	public static final String NAME_OF_SIZE = "size"; //$NON-NLS-1$
	public static final String NAME_OF_LOCK_OWNER = "lock_owner"; //$NON-NLS-1$
	public static final String NAME_OF_LOCK_CREATION_DATE = "lock_creation_date"; //$NON-NLS-1$
	public static final String NAME_OF_LOCK_EXPIRATION_DATE = "lock_expiration_date"; //$NON-NLS-1$
	public static final String NAME_OF_LOCK_COMMENT = "lock_comment"; //$NON-NLS-1$
	
	public static final IVariable VAR_NAME = new PredefinedVariable(ToolTipVariableSetProvider.DOMAIN_NAME, ToolTipVariableSetProvider.NAME_OF_NAME);
	public static final IVariable VAR_URL = new PredefinedVariable(ToolTipVariableSetProvider.DOMAIN_NAME, ToolTipVariableSetProvider.NAME_OF_URL);
	public static final IVariable VAR_LAST_CHANGE_DATE = new PredefinedVariable(ToolTipVariableSetProvider.DOMAIN_NAME, ToolTipVariableSetProvider.NAME_OF_LAST_CHANGE_DATE);
	public static final IVariable VAR_LAST_AUTHOR = new PredefinedVariable(ToolTipVariableSetProvider.DOMAIN_NAME, ToolTipVariableSetProvider.NAME_OF_LAST_AUTHOR);
	public static final IVariable VAR_SIZE = new PredefinedVariable(ToolTipVariableSetProvider.DOMAIN_NAME, ToolTipVariableSetProvider.NAME_OF_SIZE);
	public static final IVariable VAR_LOCK_OWNER = new PredefinedVariable(ToolTipVariableSetProvider.DOMAIN_NAME, ToolTipVariableSetProvider.NAME_OF_LOCK_OWNER);
	public static final IVariable VAR_LOCK_CREATION_DATE = new PredefinedVariable(ToolTipVariableSetProvider.DOMAIN_NAME, ToolTipVariableSetProvider.NAME_OF_LOCK_CREATION_DATE);
	public static final IVariable VAR_LOCK_EXPIRATION_DATE = new PredefinedVariable(ToolTipVariableSetProvider.DOMAIN_NAME, ToolTipVariableSetProvider.NAME_OF_LOCK_EXPIRATION_DATE);
	public static final IVariable VAR_LOCK_COMMENT = new PredefinedVariable(ToolTipVariableSetProvider.DOMAIN_NAME, ToolTipVariableSetProvider.NAME_OF_LOCK_COMMENT);
	
	private static final Map<String, IVariable> name2Variable = new HashMap<String, IVariable>();
	
	static {
		ToolTipVariableSetProvider.name2Variable.put(ToolTipVariableSetProvider.NAME_OF_NAME, ToolTipVariableSetProvider.VAR_NAME);
		ToolTipVariableSetProvider.name2Variable.put(ToolTipVariableSetProvider.NAME_OF_URL, ToolTipVariableSetProvider.VAR_URL);
		ToolTipVariableSetProvider.name2Variable.put(ToolTipVariableSetProvider.NAME_OF_LAST_CHANGE_DATE, ToolTipVariableSetProvider.VAR_LAST_CHANGE_DATE);
		ToolTipVariableSetProvider.name2Variable.put(ToolTipVariableSetProvider.NAME_OF_LAST_AUTHOR, ToolTipVariableSetProvider.VAR_LAST_AUTHOR);
		ToolTipVariableSetProvider.name2Variable.put(ToolTipVariableSetProvider.NAME_OF_SIZE, ToolTipVariableSetProvider.VAR_SIZE);
		ToolTipVariableSetProvider.name2Variable.put(ToolTipVariableSetProvider.NAME_OF_LOCK_OWNER, ToolTipVariableSetProvider.VAR_LOCK_OWNER);
		ToolTipVariableSetProvider.name2Variable.put(ToolTipVariableSetProvider.NAME_OF_LOCK_CREATION_DATE, ToolTipVariableSetProvider.VAR_LOCK_CREATION_DATE);
		ToolTipVariableSetProvider.name2Variable.put(ToolTipVariableSetProvider.NAME_OF_LOCK_EXPIRATION_DATE, ToolTipVariableSetProvider.VAR_LOCK_EXPIRATION_DATE);
		ToolTipVariableSetProvider.name2Variable.put(ToolTipVariableSetProvider.NAME_OF_LOCK_COMMENT, ToolTipVariableSetProvider.VAR_LOCK_COMMENT);
	}
	
	public static ToolTipVariableSetProvider instance = new ToolTipVariableSetProvider();
	
	public IVariable getCenterVariable() {
		return null;
	}

	public String getDomainName() {
		return ToolTipVariableSetProvider.DOMAIN_NAME;
	}

	public IVariable getVariable(String name) {
		return ToolTipVariableSetProvider.name2Variable.get(name);
	}
	
	protected ToolTipVariableSetProvider() {
		
	}

}
