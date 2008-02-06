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

package org.eclipse.team.svn.core.utility;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ProgressMonitorWrapper;

/**
 * This subprogress monitor allow us to get current progress state
 * 
 * @author Alexander Gurov
 */
public class SubProgressMonitorWithInfo extends ProgressMonitorWrapper {
	protected double currentProgress;
	protected double unknownProgress;
	protected long lastTime;
	
	protected double parentTicks;
	protected double scale;
	protected int totalWork;
	protected String subTaskOp;
	
	public SubProgressMonitorWithInfo(IProgressMonitor monitor, double parentTicks) {
		super(monitor);
		this.parentTicks = parentTicks;
	}

	public void beginTask(String name, int totalWork) {
		this.lastTime = 0;
		this.currentProgress = 0;
		this.unknownProgress = 0;
		this.totalWork = totalWork > 0 ? totalWork : ProgressMonitorUtility.TOTAL_WORK;
		this.scale = this.parentTicks / this.totalWork;
	}

	public void done() {
		this.subTaskOp = null;
		this.internalWorked(this.totalWork - this.currentProgress);
	}
	
	public void internalWorked(double work) {
		if (this.currentProgress + work > this.totalWork) {
			work = this.totalWork - this.currentProgress;
		}
		if (this.currentProgress < this.totalWork) {
			this.currentProgress += work;
			super.internalWorked(work * this.scale);
		}
	}
	
	public void worked(int work) {
		if (work == IProgressMonitor.UNKNOWN) {
			double delta = (this.totalWork - this.currentProgress) / this.totalWork;
			delta /= this.unknownProgress < 30 ? 1 : (this.unknownProgress < 50 ? 2 : (this.unknownProgress < 70 ? 6 : (this.unknownProgress < 85 ? 12 : 25)));
			this.unknownProgress += delta;
			int offset = (int)(this.unknownProgress - this.currentProgress);
			this.internalWorked(offset);
		}
		else {
			this.internalWorked(work);
		}
	}
	
	public void unknownProgress(int current) {
		this.worked(IProgressMonitor.UNKNOWN);
	}
	
	public void subTask(String name) {
		long time = System.currentTimeMillis();
		// redraw four times per second or if operation was changed
		boolean operationChanged = this.subTaskOp == null || !name.startsWith(this.subTaskOp) || name.charAt(this.subTaskOp.length()) != ':';
		if (this.lastTime == 0 || (time - this.lastTime) >= 250 || operationChanged) {
			this.lastTime = time;
			int idx = name.indexOf(':');
			if (idx != -1) {
				this.subTaskOp = name.substring(0, idx);
			}
			super.subTask(name);
		}
	}
	
	public int getCurrentProgress() {
		return (int)this.currentProgress;
	}
	
}
