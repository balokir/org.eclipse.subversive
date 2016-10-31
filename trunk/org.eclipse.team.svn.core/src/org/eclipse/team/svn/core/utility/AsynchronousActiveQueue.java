/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Andrey Loskutov - [scalability] SVN update takes hours if "Synchronize" view is opened
 *******************************************************************************/

package org.eclipse.team.svn.core.utility;

import java.util.LinkedList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;

public class AsynchronousActiveQueue<Data extends IQueuedElement<Data>> {
	
	public static interface IRecordHandler<Data extends IQueuedElement<Data>>{
		public void process(IProgressMonitor monitor, IActionOperation op, Data record);
	}
	
	protected final String name;
	protected final LinkedList<Data> queue;
	protected final IRecordHandler<Data> handler;
	protected final boolean system;
	
	static final boolean DEBUG = SVNTeamPlugin.instance().isDebugging();

	public AsynchronousActiveQueue(String queueName, IRecordHandler<Data> handler, boolean system) {
		this.name = queueName;
		this.queue = new LinkedList<Data>();
		this.handler = handler;
		this.system = system;
	}
	
	public void push(Data data) {
		synchronized (this.queue) {
			// avoid duplicated events, Start search from the end, the possibility
			// to find similar events added recently is higher
			if (!this.queue.isEmpty() && data.canSkip()) {
				for (int i = this.queue.size() - 1; i >= 0; i--) {
					Data old = this.queue.get(i);
					if (old.equals(data)) {
						if (DEBUG) {
							logDebug("skipped: " + data);
						}
						return;
					}
				}
			}
			if (this.queue.size() > 1) {
				// try to merge with all except the first one, which could be
				// being dispatched right now
				for (int i = this.queue.size() - 1; i > 0; i--) {
					Data old = this.queue.get(i);
					if (old.canMerge(data)) {
						this.queue.set(i, old.merge(data));
						if (DEBUG) {
							logDebug("merged " + old + " with " + data);
						}
						return;
					}
				}
			}
			this.queue.add(data);
			if (DEBUG) {
				logDebug("added " + data);
			}
	    	if (this.queue.size() == 1) {
				ProgressMonitorUtility.doTaskScheduledDefault(new QueuedOperation(this.name), this.system);
	    	}
		}		
	}
	
	private final class QueuedOperation extends AbstractActionOperation {
		private QueuedOperation(String operationName) {
			super(operationName, SVNMessages.class);
		}

		@Override
		public ISchedulingRule getSchedulingRule() {
			return null;
		}

		@Override
		protected void runImpl(IProgressMonitor monitor) throws Exception {
			while (true) {
				Data record;
				synchronized (AsynchronousActiveQueue.this.queue) {
					if (monitor.isCanceled() || AsynchronousActiveQueue.this.queue.isEmpty()) {
						AsynchronousActiveQueue.this.queue.clear();
						break;
					}
					record = AsynchronousActiveQueue.this.queue.get(0);
				}
				AsynchronousActiveQueue.this.handler.process(monitor, this, record);
				if (DEBUG) {
					logDebug("processed " + record);
				}
				synchronized (AsynchronousActiveQueue.this.queue) {
					AsynchronousActiveQueue.this.queue.remove(0);
					if (AsynchronousActiveQueue.this.queue.isEmpty()) {
						break;
					}
				}
			}
		}
	}
	
	private void logDebug(String message){
		if (DEBUG) {
			System.out.println("[" + this.name + "] size: " + this.queue.size() + ", " + message);
		}
	}
}
