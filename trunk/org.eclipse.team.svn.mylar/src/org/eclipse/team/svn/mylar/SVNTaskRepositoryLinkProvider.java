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

package org.eclipse.team.svn.mylar;

import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.core.TaskRepositoryManager;
import org.eclipse.mylar.tasks.ui.AbstractTaskRepositoryLinkProvider;

/**
 * Task repository link provider
 * 
 * @author Alexander Gurov
 */
public class SVNTaskRepositoryLinkProvider extends AbstractTaskRepositoryLinkProvider {

	public TaskRepository getTaskRepository(IResource resource, TaskRepositoryManager repositoryManager) {
		String url = SVNLinkedTaskInfoAdapterFactory.getBugtraqModel(resource).getUrl();
		if (url != null) {
		    for (Iterator it = repositoryManager.getAllRepositories().iterator(); it.hasNext(); ) {
		    	TaskRepository repository = (TaskRepository)it.next();
		    	String tUrl = repository.getUrl();
		    	if (url.startsWith(tUrl)) {
		    		return repository;
		    	}
		    }
		}
		return null;
	}

}
