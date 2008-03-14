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

package org.eclipse.team.svn.core.resource;

/**
 * This interface is desinged for deffered-in-time data acquisition
 * 
 * @author Alexander Gurov
 */
public interface IRepositoryResourceProvider {
	public static class DefaultRepositoryResourceProvider implements IRepositoryResourceProvider {
		protected IRepositoryResource []resources;
		
		public DefaultRepositoryResourceProvider(IRepositoryResource []resources) {
			this.resources = resources;
		}

		public IRepositoryResource[] getRepositoryResources() {
			return this.resources;
		}
		
	}
	
	public IRepositoryResource []getRepositoryResources();
}
