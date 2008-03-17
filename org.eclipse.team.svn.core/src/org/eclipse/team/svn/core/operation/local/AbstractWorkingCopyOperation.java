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

package org.eclipse.team.svn.core.operation.local;

import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.resource.IResourceProvider;

/**
 * Abstract IActionOperation implementation that have possibility of resource set roots calculation.
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractWorkingCopyOperation extends AbstractActionOperation {
	private IResource []resources;
	private IResourceProvider provider;

	public AbstractWorkingCopyOperation(String operationName, IResource []resources) {
		super(operationName);
		this.resources = resources;
	}
	
	public AbstractWorkingCopyOperation(String operationName, IResourceProvider provider) {
		super(operationName);
		this.provider = provider;
	}
	
    public ISchedulingRule getSchedulingRule() {
    	if (this.resources == null) {
    		return ResourcesPlugin.getWorkspace().getRoot();
    	}
    	HashSet<IResource> ruleSet = new HashSet<IResource>();
    	for (int i = 0; i < this.resources.length; i++) {
			ruleSet.add(this.resources[i] instanceof IProject ? this.resources[i] : this.resources[i].getParent());
    	}
    	return new MultiRule(ruleSet.toArray(new IResource[ruleSet.size()]));
    }

	protected IResource []operableData() {
		return this.resources == null ? this.provider.getResources() : this.resources;
	}
	
}
