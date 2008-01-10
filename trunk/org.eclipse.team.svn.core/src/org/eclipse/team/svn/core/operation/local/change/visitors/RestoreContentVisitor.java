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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.local.AddToSVNOperation;
import org.eclipse.team.svn.core.operation.local.change.IActionOperationProcessor;
import org.eclipse.team.svn.core.operation.local.change.IResourceChangeVisitor;
import org.eclipse.team.svn.core.operation.local.change.ResourceChange;
import org.eclipse.team.svn.core.operation.local.refactor.DeleteResourceOperation;
import org.eclipse.team.svn.core.resource.ILocalFile;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Restores local content
 * 
 * @author Alexander Gurov
 */
public class RestoreContentVisitor implements IResourceChangeVisitor {
	protected boolean nodeKindChanged;
	
	public RestoreContentVisitor(boolean nodeKindChanged) {
		this.nodeKindChanged = nodeKindChanged;
	}
	
	public void preVisit(ResourceChange change, IActionOperationProcessor processor, IProgressMonitor monitor) throws Exception {
	}

	public void postVisit(ResourceChange change, IActionOperationProcessor processor, IProgressMonitor monitor) throws Exception {
		ILocalResource local = change.getLocal();
		
		if (local instanceof ILocalFile) {
	    	File real = new File(FileUtility.getWorkingCopyPath(local.getResource()));
			
	        boolean exists = real.exists();
	        
			real.delete();
	        
	    	if (IStateFilter.SF_DELETED.accept(local)) {
	    		if (exists && !IStateFilter.SF_MISSING.accept(local)) {
	    			processor.doOperation(new DeleteResourceOperation(local.getResource()), monitor);
	    		}
	    		if (!IStateFilter.SF_PREREPLACEDREPLACED.accept(local)) {
	        		return;//skip save file content for deleted files
	    		}
        	}
	    	
	    	if (change.getTemporary().exists() && !change.getTemporary().renameTo(real)) {
	    		File parent = real.getParentFile();
	    		if (parent != null) {
	    			parent.mkdirs();
	    		}
				FileUtility.copyFile(real, change.getTemporary(), monitor);
				change.getTemporary().delete();
	    	}
	    	
			if (!this.nodeKindChanged && (IStateFilter.SF_REPLACED.accept(local) || !exists && IStateFilter.SF_VERSIONED.accept(local))) { 
			    processor.doOperation(new AddToSVNOperation(new IResource[] {local.getResource()}, false), monitor);
			}
		}
	}

}
