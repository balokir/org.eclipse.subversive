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
 * Predefined property class
 *
 * @author Sergiy Logvin
 */
public class PredefinedProperty {
	public final String name;
	public final String description;
	public final String value;
	public final String validationRegexp;
	
	public PredefinedProperty(String name) {
		this(name, "", ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public PredefinedProperty(String name, String description, String value) {
		this(name, description, value, null);
	}
	
	public PredefinedProperty(String name, String description, String value, String validationRegexp) {
		this.name = name;
		this.description = description;
		this.value = value;
		this.validationRegexp = validationRegexp;
	}
	
	public boolean equals(Object arg0) {
		if (arg0 instanceof PredefinedProperty) {
			return ((PredefinedProperty)arg0).name.equals(this.name);
		}
		return false;
	}
	
}
