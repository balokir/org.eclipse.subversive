/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.utility;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.LoggedOperation;

/**
 * Monitor management utility class
 * 
 * @author Alexander Gurov
 */
public final class ProgressMonitorUtility {
	public static final int TOTAL_WORK = 100;
	
	public static Job doTaskScheduledDefault(IActionOperation runnable) {
		return ProgressMonitorUtility.doTaskScheduledDefault(runnable, true);
	}
	
	public static Job doTaskScheduled(IActionOperation runnable) {
		return ProgressMonitorUtility.doTaskScheduled(runnable, true);
	}
	
	public static Job doTaskScheduledDefault(IActionOperation runnable, boolean system) {
		return ProgressMonitorUtility.doTaskScheduled(runnable, ILoggedOperationFactory.DEFAULT, system);
	}
	
	public static Job doTaskScheduled(IActionOperation runnable, boolean system) {
		return ProgressMonitorUtility.doTaskScheduled(runnable, SVNTeamPlugin.instance().getOptionProvider().getLoggedOperationFactory(), system);
	}
	
	public static Job doTaskScheduled(IActionOperation runnable, ILoggedOperationFactory factory, boolean system) {
		final IActionOperation logged = factory == null ? runnable : factory.getLogged(runnable);
		Job job = new Job(logged.getOperationName()) {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
						public void run(IProgressMonitor monitor) throws CoreException {
							ProgressMonitorUtility.doTaskExternal(logged, monitor, null);
						}
					}, this.getRule(), IWorkspace.AVOID_UPDATE, monitor);
				} 
				catch (CoreException e) {
					LoggedOperation.reportError(SVNTeamPlugin.instance().getResource("Error.ScheduledTask"), e);
				}
				return Status.OK_STATUS;
			}
		};
		job.setRule(logged.getSchedulingRule());
		job.setSystem(system);
		job.schedule();
		return job;
	}
	
	public static void doTaskExternalDefault(IActionOperation runnable, IProgressMonitor monitor) {
		ProgressMonitorUtility.doTaskExternal(runnable, monitor, ILoggedOperationFactory.DEFAULT);
	}
	
	public static void doTaskExternal(IActionOperation runnable, IProgressMonitor monitor) {
		ProgressMonitorUtility.doTaskExternal(runnable, monitor, SVNTeamPlugin.instance().getOptionProvider().getLoggedOperationFactory());
	}
	
	public static void doTaskExternal(IActionOperation runnable, IProgressMonitor monitor, ILoggedOperationFactory factory) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(null, ProgressMonitorUtility.TOTAL_WORK);
		try {
			ProgressMonitorUtility.doTask(factory == null ? runnable : factory.getLogged(runnable), monitor, IActionOperation.DEFAULT_WEIGHT, IActionOperation.DEFAULT_WEIGHT);
		}
		finally {
			monitor.done();
		}
	}
	
	public static void doTask(IActionOperation runnable, IProgressMonitor monitor, int totalWeight, int currentWeight) {
		if (totalWeight > 0) {
			monitor = new SubProgressMonitorWithInfo(monitor, ProgressMonitorUtility.TOTAL_WORK * currentWeight / totalWeight);
		}
		monitor.beginTask(runnable.getOperationName(), ProgressMonitorUtility.TOTAL_WORK);
		ProgressMonitorUtility.setTaskInfo(monitor, runnable, SVNTeamPlugin.instance().getResource("Progress.Running"));
		try {
			runnable.run(monitor);
		}
		finally {
			monitor.done();
			ProgressMonitorUtility.setTaskInfo(monitor, runnable, SVNTeamPlugin.instance().getResource("Progress.Done"));
		}
	}
	
	public static void doSubTask(IActionOperation runnable, IUnprotectedOperation op, IProgressMonitor monitor, int totalWeight, int currentWeight) throws Exception {
		if (totalWeight > 0) {
			monitor = new SubProgressMonitorWithInfo(monitor, ((double)ProgressMonitorUtility.TOTAL_WORK) * currentWeight / totalWeight);
		}
		monitor.beginTask(runnable.getOperationName(), ProgressMonitorUtility.TOTAL_WORK);
		try {
			op.run(monitor);
		}
		finally {
			monitor.done();
		}
	}
	
	public static void progress(IProgressMonitor monitor, int current, int total) {
		if (monitor instanceof SubProgressMonitorWithInfo) {
			SubProgressMonitorWithInfo info = (SubProgressMonitorWithInfo)monitor;
			info.worked(total == IProgressMonitor.UNKNOWN ? IProgressMonitor.UNKNOWN : (ProgressMonitorUtility.TOTAL_WORK * current / total - info.getCurrentProgress()));
		}
		else {
			monitor.worked(1);
		}
	}

	public static void setTaskInfo(IProgressMonitor monitor, IActionOperation op, String subTask) {
		String message = SVNTeamPlugin.instance().getResource("Progress.SubTask", new String[] {op.getOperationName(), subTask == null || subTask.length() == 0 ? SVNTeamPlugin.instance().getResource("Progress.Running") : subTask});
		monitor.subTask(message);
	}

	private ProgressMonitorUtility() {
	}

}
