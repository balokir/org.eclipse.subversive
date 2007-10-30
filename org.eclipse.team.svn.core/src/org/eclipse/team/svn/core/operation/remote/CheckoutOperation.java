/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
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
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;

/**
 * Checkout content into the project which have a name equals to the remote resource name
 * 
 * @author Alexander Gurov
 */
public class CheckoutOperation extends AbstractActionOperation implements IResourceProvider {
	protected HashMap checkoutMap;
	protected IProject []projects;
	protected CheckoutAsOperation []operations;
	protected ISchedulingRule rule;

	public CheckoutOperation(HashMap checkoutMap, boolean respectHierarchy, String location, boolean checkoutRecursively) {
		super("Operation.CheckOut");
		this.checkoutMap = checkoutMap;
		
		ArrayList projects = new ArrayList();
		ArrayList operations = new ArrayList();
		ArrayList rules = new ArrayList();
		for (Iterator iter = this.checkoutMap.keySet().iterator(); iter.hasNext(); ) {
			String name = (String)iter.next();
			IRepositoryResource currentResource = (IRepositoryResource)this.checkoutMap.get(name);
			CheckoutAsOperation coOp = CheckoutOperation.getCheckoutAsOperation(name, currentResource, respectHierarchy, location, checkoutRecursively);
			operations.add(coOp);
			projects.add(coOp.getProject());
			rules.add(coOp.getSchedulingRule());
		}
		this.rule = new MultiRule((ISchedulingRule [])rules.toArray(new ISchedulingRule[rules.size()]));
		this.projects = (IProject [])projects.toArray(new IProject[projects.size()]);
		this.operations = (CheckoutAsOperation [])operations.toArray(new CheckoutAsOperation[operations.size()]);
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
			ProgressMonitorUtility.doTask(this.operations[i], monitor, this.checkoutMap.keySet().size());
			this.reportStatus(this.operations[i].getStatus());
		}
	}
	
	public static CheckoutAsOperation getCheckoutAsOperation(String name, IRepositoryResource currentResource, boolean respectHierarchy, String location, boolean checkoutRecursively) {
		if (location != null && location.trim().length() > 0) {
			return new CheckoutAsOperation(name, currentResource, respectHierarchy, location, checkoutRecursively);
		}
		else {
			return new CheckoutAsOperation(name, currentResource, checkoutRecursively);
		}
	}
	
}
