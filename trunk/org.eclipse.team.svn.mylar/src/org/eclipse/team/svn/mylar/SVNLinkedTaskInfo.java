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

import org.eclipse.mylar.tasks.core.ILinkedTaskInfo;
import org.eclipse.mylar.tasks.core.ITask;

/**
 * ILinkedTaskInfo implementation.
 * 
 * @author Alexander Gurov
 */
public class SVNLinkedTaskInfo implements ILinkedTaskInfo {
	protected ITask task;
	protected String repositoryUrl;
	protected String taskId;
	protected String taskFullUrl;
	protected String comment;
	
	public SVNLinkedTaskInfo(ITask task, String repositoryUrl, String taskId, String taskFullUrl, String comment) {
		this.task = task;
		this.repositoryUrl = repositoryUrl;
		this.taskId = taskId;
		this.taskFullUrl = taskFullUrl;
		this.comment = comment;
	}

	public String getComment() {
		return this.comment;
	}

	public String getRepositoryUrl() {
		return this.repositoryUrl;
	}

	public ITask getTask() {
		return this.task;
	}

	public String getTaskFullUrl() {
		return this.taskFullUrl;
	}

	public String getTaskId() {
		return this.taskId;
	}

}
