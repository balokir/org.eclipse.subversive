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

import org.eclipse.mylyn.team.ui.AbstractTaskReference;

/**
 * ILinkedTaskInfo implementation.
 * 
 * @author Alexander Gurov
 */
public class SVNLinkedTaskInfo extends AbstractTaskReference {
	protected String repositoryUrl;
	protected String taskId;
	protected String taskFullUrl;
	protected String comment;
	
	public SVNLinkedTaskInfo(String repositoryUrl, String taskId, String taskFullUrl, String comment) {
		this.repositoryUrl = repositoryUrl;
		this.taskId = taskId;
		this.taskFullUrl = taskFullUrl;
		this.comment = comment;
	}

	public String getText() {
		return this.comment;
	}

	public String getRepositoryUrl() {
		return this.repositoryUrl;
	}

	public String getTaskUrl() {
		return this.taskFullUrl;
	}

	public String getTaskId() {
		return this.taskId;
	}

}
