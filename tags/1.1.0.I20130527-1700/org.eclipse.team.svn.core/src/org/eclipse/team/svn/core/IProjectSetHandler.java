/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.svn.core.operation.CompositeOperation;

/**
 * As we can support several project set providers, this is an interface to interact with them
 * 
 * @author Igor Burilo
 */
public interface IProjectSetHandler {

	public String asReference(IProject project) throws TeamException;
	public String getProjectNameForReference(String fullReference);
	public IProject configureCheckoutOperation(CompositeOperation op, IProject project, String fullReference) throws TeamException;
	public boolean accept(String referenceString);
	
}
