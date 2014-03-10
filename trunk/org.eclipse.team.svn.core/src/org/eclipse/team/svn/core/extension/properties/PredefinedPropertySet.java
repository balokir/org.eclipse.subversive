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

package org.eclipse.team.svn.core.extension.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Default IPredefinedPropertySet implementation
 *
 * @author Sergiy Logvin
 */
public class PredefinedPropertySet implements IPredefinedPropertySet {
	private Map<String, PredefinedProperty> properties = new LinkedHashMap<String, PredefinedProperty>();
	
	public List<PredefinedProperty> getPredefinedProperties() {
		return new ArrayList<PredefinedProperty>(this.getPropertiesMap().values());
	}
	
	public PredefinedProperty getPredefinedProperty(String name) {
		return this.getPropertiesMap().get(name);
	}
	
	public Map<String, String> getPredefinedPropertiesRegexps() {
		HashMap<String, String> regexpmap = new HashMap<String, String>();
		for (PredefinedProperty property : this.getPredefinedProperties()) {
			regexpmap.put(property.name, property.validationRegexp);
		}
		return regexpmap;
	}
	
	public void registerProperties(List<PredefinedProperty> properties) {
		for (PredefinedProperty property : properties) {
			this.registerProperty(property);
		}
	}
	
	public void registerProperties(PredefinedProperty []properties) {
		for (PredefinedProperty property : properties) {
			this.registerProperty(property);
		}
	}
	
	public void registerProperty(PredefinedProperty property) {
		this.properties.put(property.name, property);
	}
	
	protected synchronized Map<String, PredefinedProperty> getPropertiesMap() {
		this.init();
		return this.properties;
	}
	
	protected void init()
	{
	}
	
}
