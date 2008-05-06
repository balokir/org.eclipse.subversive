/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.local.change;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Visit resource changes
 * 
 * @author Alexander Gurov
 */
public interface IResourceChangeVisitor {
	public void preVisit(ResourceChange change, IActionOperationProcessor processor, IProgressMonitor monitor) throws Exception;
	public void postVisit(ResourceChange change, IActionOperationProcessor processor, IProgressMonitor monitor) throws Exception;
}
