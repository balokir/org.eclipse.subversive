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
	public static final int TYPE_NONE = 0x0;
	public static final int TYPE_REVISION = 0x1;
	public static final int TYPE_FILE = 0x2;
	public static final int TYPE_FOLDER = 0x4;
	public static final int TYPE_COMMON = PredefinedProperty.TYPE_FOLDER | PredefinedProperty.TYPE_FILE;
	public static final int TYPE_GROUP = 0x8;
	
	public final String name;
	public final String description;
	public final String value;
	public final String validationRegexp;
	public final int type;
	
	public PredefinedProperty(String name) {
		this(name, PredefinedProperty.TYPE_NONE);
	}

	public PredefinedProperty(String name, int type) {
		this(name, "", "", null, type); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public PredefinedProperty(String name, String description, String value) {
		this(name, description, value, null);
	}
	
	public PredefinedProperty(String name, String description, String value, String validationRegexp) {
		this(name, description, value, validationRegexp, PredefinedProperty.TYPE_COMMON);
	}
	
	public PredefinedProperty(String name, String description, String value, String validationRegexp, int type) {
		this.name = name;
		this.description = description;
		this.value = value;
		this.validationRegexp = validationRegexp;
		this.type = type;
	}
	
	public boolean equals(Object arg0) {
		if (arg0 instanceof PredefinedProperty) {
			return ((PredefinedProperty)arg0).name.equals(this.name);
		}
		return false;
	}
	
}
