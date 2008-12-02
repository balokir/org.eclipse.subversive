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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaAttribute;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.utility.StringId;
import org.eclipse.team.svn.ui.debugmail.ReportPartsFactory;
import org.eclipse.team.svn.ui.extension.factory.IReporter;
import org.eclipse.team.svn.ui.extension.factory.IReportingDescriptor;
import org.eclipse.team.svn.ui.extension.factory.IReporterFactory.ReportType;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;


/**
 * Allows to post a product bug or tip directly to Eclipse.org Bugzilla
 * 
 * @author Alexander Gurov
 */
public class MylynReporter extends AbstractActionOperation implements IReporter {
	protected TaskRepository repository;
	protected AbstractTaskDataHandler taskDataHandler;
	
	protected IReportingDescriptor settings;
	protected ReportType type;
	
	protected IStatus problemStatus;
	protected String summary;
	protected String reportId;
	protected String userComment;
	
	public MylynReporter(TaskRepository repository, AbstractTaskDataHandler taskDataHandler, IReportingDescriptor settings, ReportType type) {
		super(MylynMessages.getErrorString("Operation_OpenReportEditor")); //$NON-NLS-1$
		
		this.settings = settings;
		this.type = type;
		this.repository = repository;
		this.taskDataHandler = taskDataHandler;
		this.reportId = StringId.generateRandom("ID", 5);
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		String kind = this.repository.getConnectorKind();
		TaskAttributeMapper attributeFactory = this.taskDataHandler.getAttributeMapper(this.repository);
		final TaskData taskData = new TaskData(attributeFactory, kind, this.repository.getRepositoryUrl(), ""); // ID must be empty (but not null) for new task
		
		boolean isInitializedSuccessfully = this.taskDataHandler.initializeTaskData(this.repository, taskData, new TaskMapping() {
			public String getSummary() {
				return MylynReporter.this.buildSubject();
			}
			public String getTaskKind() {
				return MylynReporter.this.type == ReportType.BUG ? "normal" : "enhancement";
			}
			public String getDescription() {
				return MylynReporter.this.buildReport();
			}
			public String getProduct() {
				return MylynReporter.this.settings.getProductName();
			}
		}, monitor);
		
		if (!isInitializedSuccessfully) {
			throw new CoreException(new RepositoryStatus(IStatus.ERROR, SVNMylynIntegrationPlugin.ID,
					RepositoryStatus.ERROR_REPOSITORY,
					"The selected repository does not support creating new tasks."));
		}

		//does not work for Bugzilla connector
		taskData.getRoot().getMappedAttribute(TaskAttribute.SUMMARY).setValue(this.buildSubject());
		taskData.getRoot().getMappedAttribute(TaskAttribute.DESCRIPTION).setValue(this.buildReport());
		
		// has no public key
		taskData.getRoot().getAttribute(BugzillaAttribute.BUG_SEVERITY.getKey()).setValue(this.type == ReportType.BUG ? "normal" : "enhancement");
		
		// open task editor
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				try {
					TasksUiInternal.createAndOpenNewTask(taskData);
				}
				catch (CoreException e) {
					MylynReporter.this.reportError(e);
				}
			}
		});
	}

	public String buildReport() {
		String report = ReportPartsFactory.getVersionPart(this.settings);
		if (this.userComment != null) {
			report += ReportPartsFactory.getUserCommentPart(this.userComment);
		}
		report += ReportPartsFactory.getSVNClientPart();
		
		if (this.type == ReportType.BUG) {
			report += ReportPartsFactory.getJVMPropertiesPart();
			if (this.problemStatus != null) {
				report += ReportPartsFactory.getStatusPart(this.problemStatus);
			}
		}
		
		if (!this.settings.isTrackerSupportsHTML()) {
			report = ReportPartsFactory.removeHTMLTags(report);
		}
		return report;
	}

	public String buildSubject() {
		String subject = "[" + this.settings.getProductName() + "] - ";
		subject += this.summary != null ? this.summary : ((this.type == ReportType.BUG ? "Bug report " : "Tip for improvement ") + this.reportId);
		return subject;
	}

	public IReportingDescriptor getReportingDescriptor() {
		return this.settings;
	}

	public boolean isCustomEditorSupported() {
		return true;
	}

	public void setProblemStatus(IStatus problemStatus) {
		this.problemStatus = problemStatus;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public void setUserComment(String userComment) {
		this.userComment = userComment;
	}

	public void setUserEMail(String userEMail) {
	}

	public void setUserName(String userName) {
	}

}
