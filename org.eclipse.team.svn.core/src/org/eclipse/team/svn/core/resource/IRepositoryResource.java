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

import java.util.Date;

import org.eclipse.team.svn.core.client.ClientWrapperException;
import org.eclipse.team.svn.core.client.Lock;
import org.eclipse.team.svn.core.client.Revision;

/**
 * Abstract repository resource
 * 
 * @author Alexander Gurov
 */
public interface IRepositoryResource extends IRepositoryBase, IRepositoryResourceFactory {
	
	public static class Info {
		public final Lock lock;
		public final long fileSize;
		public final String lastAuthor;
		public final Date lastChangedDate;
		public final boolean hasProperties;
		
		public Info(Lock lock, long fileSize, String lastAuthor, Date lastChangedDate, boolean hasProperties) {
			this.lock = lock;
			this.fileSize = fileSize;
			this.lastAuthor = lastAuthor;
			this.hasProperties = hasProperties;
			this.lastChangedDate = lastChangedDate;
		}
	}

	public Revision getSelectedRevision();
	
	public void setSelectedRevision(Revision revision);
	
	public Revision getPegRevision();
	
	public void setPegRevision(Revision pegRevision);
	
	public boolean isInfoCached();
	
	public void refresh();

	public boolean exists() throws ClientWrapperException;
	
	public String getName();
	
	public String getUrl();
	
	public long getRevision() throws ClientWrapperException;
	
	public IRepositoryResource getParent();
	
	public IRepositoryResource getRoot();
	
	public IRepositoryLocation getRepositoryLocation();
	
	public Info getInfo();
	
}
