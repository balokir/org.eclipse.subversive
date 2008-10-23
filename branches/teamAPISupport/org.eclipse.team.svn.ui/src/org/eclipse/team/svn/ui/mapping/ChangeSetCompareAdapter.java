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

package org.eclipse.team.svn.ui.mapping;

import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.team.core.mapping.ISynchronizationScope;
import org.eclipse.team.internal.core.subscribers.ChangeSet;
import org.eclipse.team.internal.ui.mapping.DiffTreeChangesSection.ITraversalFactory;
import org.eclipse.team.ui.mapping.SynchronizationCompareAdapter;
import org.eclipse.ui.IMemento;

/**
 * @author Igor Burilo
 *
 */
public class ChangeSetCompareAdapter extends SynchronizationCompareAdapter implements ITraversalFactory {

	public void save(ResourceMapping[] mappings, IMemento memento) {
		// Don't save
	}

	public ResourceMapping[] restore(IMemento memento) {
		// Don't restore
		return new ResourceMapping[0];
	}

	public ResourceTraversal[] getTraversals(ISynchronizationScope scope) {
		return scope.getTraversals(ModelProvider.RESOURCE_MODEL_PROVIDER_ID);
	}
	
	public String getName(ResourceMapping mapping) {
		Object modelObject = mapping.getModelObject();
		if (modelObject instanceof ChangeSet) {
			ChangeSet changeSet = (ChangeSet) modelObject;
			return changeSet.getName();
		}
		return super.getName(mapping);
	}
}
