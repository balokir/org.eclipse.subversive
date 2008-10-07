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

package org.eclipse.team.svn.core.operation.local.change.visitors;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.local.change.IActionOperationProcessor;
import org.eclipse.team.svn.core.operation.local.change.IResourceChangeVisitor;
import org.eclipse.team.svn.core.operation.local.change.ResourceChange;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Remove non versioned resources visitor
 * 
 * @author Alexander Gurov
 */
public class RemoveNonVersionedVisitor implements IResourceChangeVisitor {
	protected boolean addedAlso;
	
	public RemoveNonVersionedVisitor(boolean addedAlso) {
		this.addedAlso = addedAlso;
	}

	public void postVisit(ResourceChange change, IActionOperationProcessor processor, IProgressMonitor monitor) throws Exception {

	}

	public void preVisit(ResourceChange change, IActionOperationProcessor processor, IProgressMonitor monitor) throws Exception {
		ILocalResource local = change.getLocal();
		if (IStateFilter.SF_UNVERSIONED.accept(local) || this.addedAlso && local.getStatus() == IStateFilter.ST_ADDED) {
	    	File real = new File(FileUtility.getWorkingCopyPath(local.getResource()));
	    	FileUtility.deleteRecursive(real);
		}
	}

}
