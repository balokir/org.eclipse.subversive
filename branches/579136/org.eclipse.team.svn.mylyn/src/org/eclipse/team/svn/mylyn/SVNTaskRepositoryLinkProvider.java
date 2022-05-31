/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.mylyn;

import org.eclipse.core.resources.IResource;
import org.eclipse.mylyn.tasks.core.IRepositoryManager;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractTaskRepositoryLinkProvider;

/**
 * Task repository link provider
 * 
 * @author Alexander Gurov
 */
public class SVNTaskRepositoryLinkProvider extends AbstractTaskRepositoryLinkProvider {

	public TaskRepository getTaskRepository(IResource resource, IRepositoryManager repositoryManager) {
		String url = SVNLinkedTaskInfoAdapterFactory.getBugtraqModel(resource).getUrl();
		if (url != null) {
		    for (TaskRepository repository : repositoryManager.getAllRepositories()) {
		    	String tUrl = repository.getRepositoryUrl();
		    	if (url.startsWith(tUrl)) {
		    		return repository;
		    	}
		    }
		}
		return null;
	}
	
}
