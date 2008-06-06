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

import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.team.svn.ui.extension.factory.IReporter;
import org.eclipse.team.svn.ui.extension.factory.IReporterFactory;
import org.eclipse.team.svn.ui.extension.factory.IReportingDescriptor;

/**
 * Mylyn-based reporter factory
 * 
 * @author Alexander Gurov
 */
public class MylynReporterFactory implements IReporterFactory {
	public boolean isCustomEditorSupported() {
		return true;
	}

	public IReporter newReporter(IReportingDescriptor settings, ReportType type) {
		TaskRepository repository = MylynReporterFactory.getRepository(settings.getTrackerUrl());
		if (repository == null) {
			return null;
		}
		AbstractRepositoryConnector connector = TasksUi.getRepositoryManager().getRepositoryConnector(repository.getConnectorKind());
		AbstractTaskDataHandler taskDataHandler = connector.getTaskDataHandler();
		if (taskDataHandler == null) {
			return null;
		}
		return new MylynReporter(repository, taskDataHandler, settings, type);
	}

	public static TaskRepository getRepository(String url)
	{
		for (TaskRepository repository : TasksUi.getRepositoryManager().getAllRepositories())
		{
			if (repository.getRepositoryUrl().equals(url)) {
				return repository;
			}
		}
		return null;
	}
	
}
