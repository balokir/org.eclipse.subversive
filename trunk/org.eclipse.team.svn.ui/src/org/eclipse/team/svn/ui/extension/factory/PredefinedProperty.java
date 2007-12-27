/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
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
	final public String name;
	final public String description;
	final public String value;
	
	public PredefinedProperty(String name) {
		this(name, null, null);
	}

	public PredefinedProperty(String name, String description, String value) {
		this.name = name;
		this.description = description;
		this.value = value;
	}
	
	public String getName() {
		return this.name;
	}
	
	public boolean equals(Object arg0) {
		if (arg0 instanceof PredefinedProperty) {
			return ((PredefinedProperty)arg0).name.equals(this.name);
		}
		return false;
	}
	
}
