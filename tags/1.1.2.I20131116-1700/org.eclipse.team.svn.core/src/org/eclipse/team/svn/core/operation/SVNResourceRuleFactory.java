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

package org.eclipse.team.svn.core.operation;

import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.team.ResourceRuleFactory;

/**
 * Resource scheduling rule factory implementation.
 * 
 * @author Alexander Gurov
 */
public class SVNResourceRuleFactory extends ResourceRuleFactory {
	public static final IResourceRuleFactory INSTANCE = new SVNResourceRuleFactory();
	
	// no changes to the default policy.
	
}
