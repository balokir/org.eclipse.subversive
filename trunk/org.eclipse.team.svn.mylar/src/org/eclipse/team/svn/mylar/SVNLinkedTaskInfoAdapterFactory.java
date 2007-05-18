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

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylar.tasks.core.ILinkedTaskInfo;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;
import org.eclipse.team.ui.history.IHistoryPage;
import org.eclipse.team.ui.history.IHistoryView;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.team.svn.core.client.LogMessage;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.history.SVNHistoryPage;
import org.eclipse.team.svn.ui.panel.local.CommitPanel;
import org.eclipse.team.svn.ui.panel.local.CommitPanel.GetBugTraqPropertiesModelOperation;
import org.eclipse.team.svn.ui.properties.bugtraq.BugtraqModel;
import org.eclipse.team.svn.ui.properties.bugtraq.IssueList;
import org.eclipse.team.svn.ui.synchronize.SVNChangeSetCollector;

/**
 * LinkedTaskInfo adapter factory
 * 
 * @author Alexander Gurov
 */
public class SVNLinkedTaskInfoAdapterFactory implements IAdapterFactory {
	private static final Class []ADAPTED_TYPES = new Class[] {ILinkedTaskInfo.class};
	
	public Class[] getAdapterList() {
		return SVNLinkedTaskInfoAdapterFactory.ADAPTED_TYPES;
	}

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (!ILinkedTaskInfo.class.equals(adapterType)) {
			return null;
		}
		
		if (adaptableObject instanceof SVNChangeSetCollector.SVNCheckedInChangeSet) {
			return this.createFromCheckedInChangeSet((SVNChangeSetCollector.SVNCheckedInChangeSet)adaptableObject);
		}
		
		if (adaptableObject instanceof LogMessage) {
			LogMessage historyEntry = (LogMessage)adaptableObject;
			String comment = historyEntry.message == null ? "" : historyEntry.message;
			IWorkbenchWindow window = SVNTeamUIPlugin.instance().getWorkbench().getActiveWorkbenchWindow();
			if (window != null) {
				IWorkbenchPage page = window.getActivePage();
				if (page != null) {
					IViewPart view = page.findView(SVNHistoryPage.VIEW_ID);
					if (view instanceof IHistoryView) {
						IHistoryPage historyPage = ((IHistoryView)view).getHistoryPage();
						if (historyPage instanceof SVNHistoryPage) {
							IResource resource = ((SVNHistoryPage)historyPage).getResource();
							return new SVNLinkedTaskInfo(null, this.getTaskRepositoryUrl(resource), null, this.getTaskFullUrl(resource, comment), comment);
						}
					}
				}
			}
			return new SVNLinkedTaskInfo(null, null, null, null, comment);
		}
		
		return null;
	}

	protected ILinkedTaskInfo createFromCheckedInChangeSet(SVNChangeSetCollector.SVNCheckedInChangeSet set) {
		IResource []resources = set.getResources();
		if (resources != null && resources.length > 0) {
			return new SVNLinkedTaskInfo(null, this.getTaskRepositoryUrl(resources[0]), null, this.getTaskFullUrl(resources[0], set.getComment()), set.getComment());
		}
		
		return new SVNLinkedTaskInfo(null, null, null, null, set.getComment());
	}
	
	protected String getTaskRepositoryUrl(IResource resource) {
		if (resource != null) {
			TaskRepository repository = TasksUiPlugin.getDefault().getRepositoryForResource(resource, true);
			if (repository != null) {
				return repository.getUrl();
			}
		}
		return null;
	}
	
	protected String getTaskFullUrl(IResource resource, String comment) {
		BugtraqModel model = SVNLinkedTaskInfoAdapterFactory.getBugtraqModel(resource);
		IssueList linkList = new IssueList();
		linkList.parseMessage(comment, model);
		List issues = linkList.getIssues();
		if (issues.size() > 0) {
			return ((IssueList.Issue)issues.get(0)).getURL();
		}
		return null;
	}
	
	public static BugtraqModel getBugtraqModel(IResource resource) {
		CommitPanel.GetBugTraqPropertiesModelOperation bugtraqOp = new GetBugTraqPropertiesModelOperation(new IResource[] {resource});
		bugtraqOp.run(new NullProgressMonitor());
		return bugtraqOp.getBugtraqModel();
	}
	
}
