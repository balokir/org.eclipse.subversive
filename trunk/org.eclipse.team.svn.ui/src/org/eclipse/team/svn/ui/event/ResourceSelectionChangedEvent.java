/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elena Matokhina - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.event;

import org.eclipse.core.resources.IResource;

/**
 * Resources selection changed event 
 * 
 * @author Elena Matokhina
 */
public class ResourceSelectionChangedEvent {
	public final IResource []resources;
	
	public ResourceSelectionChangedEvent(IResource []resources) {
		this.resources = resources;
	}

}
