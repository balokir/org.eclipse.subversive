/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elena Matokhina - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.extension.factory;

import java.util.List;

import org.eclipse.core.resources.IResource;

/**
 * Predefined properties provider interface
 *
 * @author Elena Matokhina
 */
public interface IPredefinedPropertySet {
	
	public List getPredefinedProperties(IResource []resources);
	
}
