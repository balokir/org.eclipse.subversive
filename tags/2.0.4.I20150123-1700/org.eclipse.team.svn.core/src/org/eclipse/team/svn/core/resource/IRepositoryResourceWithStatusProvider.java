/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.resource;

import java.util.Map;

/**
 * This interface is desinged for deffered-in-time data acquisition
 * 
 * @author Alexei Goncharov
 */
public interface IRepositoryResourceWithStatusProvider extends IRepositoryResourceProvider {

	public class DefaultRepositoryResourceWithStatusProvider extends DefaultRepositoryResourceProvider implements IRepositoryResourceWithStatusProvider {

		protected Map<String, String> url2status;
		
		public DefaultRepositoryResourceWithStatusProvider(IRepositoryResource []resources, Map<String, String> url2status) {
			super(resources);
			this.url2status = url2status;
		}

		public Map<String, String> getStatusesMap() {
			return this.url2status;
		}
	}
	
	public Map<String, String> getStatusesMap();
	
}
