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

package org.eclipse.team.svn.ui.decorator.wrapper;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.svn.core.resource.events.IResourceStatesListener;
import org.eclipse.team.svn.core.resource.events.ResourceStatesChangedEvent;

/**
 * Abstract decorator wrapper implementation. Should translate events from real decorators to wrappers.
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractDecoratorWrapper extends LabelProvider implements ILightweightLabelDecorator, IResourceStatesListener {
	public void resourcesStateChanged(ResourceStatesChangedEvent event) {
		this.fireLabelProviderChanged(new LabelProviderChangedEvent(this, event.getResourcesRecursivelly()));
	}
	
	public static boolean isSVNShared(IResource resource) {
		// do not touch Subversive classes
		if (resource == null) {
			return false;
		}
		IProject project = resource.getProject();
		return project != null && RepositoryProvider.getProvider(project, "org.eclipse.team.svn.core.svnnature") != null;
	}
	
}
