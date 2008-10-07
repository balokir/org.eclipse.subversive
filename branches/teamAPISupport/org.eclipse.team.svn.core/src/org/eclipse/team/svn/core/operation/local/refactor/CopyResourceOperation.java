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

package org.eclipse.team.svn.core.operation.local.refactor;

import java.text.MessageFormat;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.SVNResourceRuleFactory;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Copy only work files without SVN metainformation operation implementation
 * 
 * @author Alexander Gurov
 */
public class CopyResourceOperation extends AbstractActionOperation {
	protected IResource source;
	protected IResource destination;
	protected boolean skipSVNMeta;

	public CopyResourceOperation(IResource source, IResource destination) {
		this(source, destination, true);
	}
	
	public CopyResourceOperation(IResource source, IResource destination, boolean skipSVNMeta) {
		super("Operation.CopyLocal");
		this.source = source;
		this.destination = destination;
		this.skipSVNMeta = skipSVNMeta;
	}

	public ISchedulingRule getSchedulingRule() {
		return SVNResourceRuleFactory.INSTANCE.copyRule(this.source, this.destination);
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		this.source.copy(this.destination.getFullPath(), true, monitor);
		if (this.skipSVNMeta) {
			FileUtility.removeSVNMetaInformation(this.destination, monitor);
		}
	}
	
	protected String getShortErrorMessage(Throwable t) {
		return MessageFormat.format(super.getShortErrorMessage(t), new Object[] {this.source.getName(), this.destination.toString()});
	}

}
