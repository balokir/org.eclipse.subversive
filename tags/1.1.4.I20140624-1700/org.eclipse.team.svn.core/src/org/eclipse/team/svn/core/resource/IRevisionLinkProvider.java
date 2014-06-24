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

package org.eclipse.team.svn.core.resource;

/**
 * This interface is designed for differed-in-time data acquisition
 * 
 * @author Igor Burilo
 */
public interface IRevisionLinkProvider {
	public class DefaultRevisionLinkProvider implements IRevisionLinkProvider {
		protected IRevisionLink []links;
		
		public DefaultRevisionLinkProvider(IRevisionLink []links) {
			this.links = links;
		}

		public IRevisionLink[] getRevisionLinks() {
			return this.links;
		}
	}
	
	public IRevisionLink []getRevisionLinks();
}
