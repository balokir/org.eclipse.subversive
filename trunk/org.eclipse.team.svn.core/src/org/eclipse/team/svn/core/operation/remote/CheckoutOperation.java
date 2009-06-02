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

package org.eclipse.team.svn.core.operation.remote;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;

/**
 * Checkout content into the project which have a name equals to the remote resource name
 * 
 * @author Alexander Gurov
 */
public class CheckoutOperation extends AbstractActionOperation implements IResourceProvider {
	protected IProject []projects;
	protected CheckoutAsOperation []operations;
	protected ISchedulingRule rule;

	public CheckoutOperation(Map<String, IRepositoryResource> checkoutMap, boolean respectHierarchy, String location, int recureDepth, boolean ignoreExternals) {
		super("Operation_CheckOut"); //$NON-NLS-1$
		
		ArrayList<IProject> projects = new ArrayList<IProject>();
		ArrayList<CheckoutAsOperation> operations = new ArrayList<CheckoutAsOperation>();
		ArrayList<ISchedulingRule> rules = new ArrayList<ISchedulingRule>();
		for (Map.Entry<String, IRepositoryResource> entry : checkoutMap.entrySet()) {
			CheckoutAsOperation coOp = CheckoutOperation.getCheckoutAsOperation(entry.getKey(), entry.getValue(), respectHierarchy, location, recureDepth, ignoreExternals);
			operations.add(coOp);
			projects.add(coOp.getProject());
			rules.add(coOp.getSchedulingRule());
		}
		this.rule = new MultiRule(rules.toArray(new ISchedulingRule[rules.size()]));
		this.projects = projects.toArray(new IProject[projects.size()]);
		this.operations = operations.toArray(new CheckoutAsOperation[operations.size()]);
	}
	
	public int getOperationWeight() {
		return 19;
	}
	
	public IResource []getResources() {
		return this.projects;
	}
	
	public ISchedulingRule getSchedulingRule() {
		return this.rule;
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		for (int i = 0; i < this.operations.length && !monitor.isCanceled(); i++) {
			this.operations[i].setConsoleStream(this.getConsoleStream());
			ProgressMonitorUtility.doTask(this.operations[i], monitor, IActionOperation.DEFAULT_WEIGHT * this.projects.length, IActionOperation.DEFAULT_WEIGHT);
			this.reportStatus(this.operations[i].getStatus());
		}
	}
	
	public static CheckoutAsOperation getCheckoutAsOperation(String name, IRepositoryResource currentResource, boolean respectHierarchy, String location, int recureDepth, boolean ignoreExternals) {
		if (location != null && location.trim().length() > 0) {
			return new CheckoutAsOperation(name, currentResource, respectHierarchy, location, recureDepth, ignoreExternals);
		}
		return new CheckoutAsOperation(name, currentResource, recureDepth, ignoreExternals);
	}
	
}
