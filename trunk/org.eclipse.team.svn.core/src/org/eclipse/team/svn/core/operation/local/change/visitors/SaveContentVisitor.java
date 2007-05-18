/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
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
import org.eclipse.team.svn.core.resource.ILocalFile;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Saves file content
 * 
 * @author Alexander Gurov
 */
public class SaveContentVisitor implements IResourceChangeVisitor {
	
	public void preVisit(ResourceChange change, IActionOperationProcessor processor, IProgressMonitor monitor) throws Exception {
		ILocalResource local = change.getLocal();
		if (local instanceof ILocalFile) {
	    	if (IStateFilter.SF_DELETED.accept(local.getResource(), local.getStatus(), local.getChangeMask()) &&
        		!IStateFilter.SF_PREREPLACEDREPLACED.accept(local.getResource(), local.getStatus(), local.getChangeMask())) {
        		return;//skip save file content for deleted files
        	}
	    	File real = new File(FileUtility.getWorkingCopyPath(local.getResource()));
		    // optimize operation performance using "move on FS" if possible
			if (real.exists() && !real.renameTo(change.getTemporary())) {
				FileUtility.copyFile(change.getTemporary(), real, monitor);
				real.delete();
			}
		}
	}

	public void postVisit(ResourceChange change, IActionOperationProcessor processor, IProgressMonitor monitor) throws Exception {
	}

}
