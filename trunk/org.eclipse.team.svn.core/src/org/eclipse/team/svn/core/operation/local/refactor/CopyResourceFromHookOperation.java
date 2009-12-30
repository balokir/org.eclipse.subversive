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

package org.eclipse.team.svn.core.operation.local.refactor;

import java.io.File;
import java.io.FileFilter;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.svn.core.BaseMessages;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.SVNResourceRuleFactory;
import org.eclipse.team.svn.core.operation.UnreportableException;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Copy only work files without SVN meta information operation implementation
 * 
 * We can't call IResource API to make copying because of restrictions of caller context
 * so we use file system methods.
 * 
 * @author Igor Burilo
 */
public class CopyResourceFromHookOperation extends AbstractActionOperation {
	protected IResource source;
	protected IResource destination;
	
	public CopyResourceFromHookOperation(IResource source, IResource destination) {
		super("Operation_CopyResourceFromHook"); //$NON-NLS-1$
		this.source = source;
		this.destination = destination;
	}

	public ISchedulingRule getSchedulingRule() {
		return SVNResourceRuleFactory.INSTANCE.copyRule(this.source, this.destination);
	}
	
	protected String getShortErrorMessage(Throwable t) {
		return BaseMessages.format(super.getShortErrorMessage(t), new Object[] {this.source.getName(), this.destination.toString()});
	}
		
	@Override
	protected void runImpl(IProgressMonitor monitor) throws Exception {		
		//If we copy folder, then copy it to its parent
		IResource toResource = this.destination;
		if (toResource instanceof IContainer) {
			toResource = toResource.getParent();
			if (toResource == null) {			
				String errMessage = SVNMessages.formatErrorString("Error_NoParent", new String[] {this.destination.getFullPath().toString()}); //$NON-NLS-1$
				throw new UnreportableException(errMessage);
			}
		}			
		FileUtility.copyAll(new File(FileUtility.getWorkingCopyPath(toResource)), new File(FileUtility.getWorkingCopyPath(this.source)), FileUtility.COPY_NO_OPTIONS, new FileFilter() {
			public boolean accept(File pathname) {
				return !pathname.getName().equals(SVNUtility.getSVNFolderName());
			}
		}, monitor);									
	}
}
