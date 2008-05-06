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
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaReportElement;
import org.eclipse.mylyn.tasks.core.AbstractAttributeFactory;
import org.eclipse.mylyn.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylyn.tasks.core.RepositoryTaskData;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.mylyn.tasks.ui.editors.NewTaskEditorInput;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.utility.StringId;
import org.eclipse.team.svn.ui.debugmail.ReportPartsFactory;
import org.eclipse.team.svn.ui.extension.factory.IReporter;
import org.eclipse.team.svn.ui.extension.factory.IReportingDescriptor;
import org.eclipse.team.svn.ui.extension.factory.IReporterFactory.ReportType;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;


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
		super(SVNMylynIntegrationPlugin.instance().getResource("Operation.OpenReportEditor"));
		
		this.settings = settings;
		this.type = type;
		this.repository = repository;
		this.taskDataHandler = taskDataHandler;
		this.reportId = StringId.generateRandom("ID", 5);
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		String kind = this.repository.getConnectorKind();
		AbstractAttributeFactory attributeFactory = this.taskDataHandler.getAttributeFactory(this.repository.getUrl(), kind, AbstractTask.DEFAULT_TASK_KIND);
		final RepositoryTaskData taskData = new RepositoryTaskData(attributeFactory, kind, this.repository.getUrl(), TasksUiPlugin.getDefault().getNextNewRepositoryTaskId());
		taskData.setNew(true);
		taskData.setAttributeValue(RepositoryTaskAttribute.PRODUCT, this.settings.getProductName());
		
		if (!this.taskDataHandler.initializeTaskData(MylynReporter.this.repository, taskData, monitor)) {
			throw new CoreException(new RepositoryStatus(IStatus.ERROR, TasksUiPlugin.ID_PLUGIN,
					RepositoryStatus.ERROR_REPOSITORY,
					"The selected repository does not support creating new tasks."));
		}
		
		taskData.setSummary(this.buildSubject());
		taskData.setDescription(this.buildReport());
		taskData.setAttributeValue(BugzillaReportElement.BUG_SEVERITY.getKeyString(), this.type == ReportType.BUG ? "normal" : "enhancement");
		
		// open task editor
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				NewTaskEditorInput editorInput = new NewTaskEditorInput(MylynReporter.this.repository, taskData);
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				TasksUiUtil.openEditor(editorInput, TaskEditor.ID_EDITOR, page);
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
