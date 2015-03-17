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

import java.util.Arrays;
import java.util.LinkedHashSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.operation.local.change.IActionOperationProcessor;
import org.eclipse.team.svn.core.operation.local.change.IResourceChangeVisitor;
import org.eclipse.team.svn.core.operation.local.change.ResourceChange;

/**
 * Composite visitor
 * 
 * @author Alexander Gurov
 */
public class CompositeVisitor implements IResourceChangeVisitor {
	protected static IResourceChangeVisitor []EMPTY = new IResourceChangeVisitor[0];
	protected IResourceChangeVisitor []visitors;
	
	public CompositeVisitor() {
		this.visitors = CompositeVisitor.EMPTY;
	}
	
	public void add(IResourceChangeVisitor visitor) {
		LinkedHashSet<IResourceChangeVisitor> visitors = new LinkedHashSet<IResourceChangeVisitor>(Arrays.asList(this.visitors));
		visitors.add(visitor);
		this.visitors = visitors.toArray(new IResourceChangeVisitor[visitors.size()]);
	}

	public void postVisit(ResourceChange change, IActionOperationProcessor processor, IProgressMonitor monitor) throws Exception {
		for (int i = 0; i < this.visitors.length && !monitor.isCanceled(); i++) {
			this.visitors[i].postVisit(change, processor, monitor);
		}
	}

	public void preVisit(ResourceChange change, IActionOperationProcessor processor, IProgressMonitor monitor) throws Exception {
		for (int i = 0; i < this.visitors.length && !monitor.isCanceled(); i++) {
			this.visitors[i].preVisit(change, processor, monitor);
		}
	}

}
