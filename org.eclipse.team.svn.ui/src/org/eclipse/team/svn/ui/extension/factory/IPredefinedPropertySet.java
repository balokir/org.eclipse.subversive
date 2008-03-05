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

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;

/**
 * Predefined properties provider interface
 *
 * @author Sergiy Logvin
 */
public interface IPredefinedPropertySet {
	public List<PredefinedProperty> getPredefinedProperties(IResource []resources);
	public Map<String, String> getPredefinedPropertiesRegexps(IResource []resources);	
}
