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

package org.eclipse.team.svn.core.operation.local;

import java.util.HashSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.SVNResourceRuleFactory;
import org.eclipse.team.svn.core.resource.IResourceProvider;

/**
 * Abstract IActionOperation implementation that have possibility of resource set roots calculation.
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractWorkingCopyOperation extends AbstractActionOperation {
	private IResource []resources;
	private IResourceProvider provider;

	public AbstractWorkingCopyOperation(String operationName, Class<? extends NLS> messagesClass, IResource []resources) {
		super(operationName, messagesClass);
		this.resources = resources;
	}
	
	public AbstractWorkingCopyOperation(String operationName, Class<? extends NLS> messagesClass, IResourceProvider provider) {
		super(operationName, messagesClass);
		this.provider = provider;
	}
	
    public ISchedulingRule getSchedulingRule() {
    	// if the resource provider interface is used then we don't know all the resources at the moment of the operation scheduling, so we lock the entire workspace instead
    	if (this.resources == null || this.resources.length == 0) {
    		return ResourcesPlugin.getWorkspace().getRoot();
    	}
    	HashSet<ISchedulingRule> ruleSet = new HashSet<ISchedulingRule>();
    	for (int i = 0; i < this.resources.length; i++) {
    		ruleSet.add(SVNResourceRuleFactory.INSTANCE.refreshRule(this.resources[i]));
    	}
    	return new MultiRule(ruleSet.toArray(new ISchedulingRule[ruleSet.size()]));
    }

	protected IResource []operableData() {
		return this.resources == null ? this.provider.getResources() : this.resources;
	}
	
}
