package org.eclipse.team.svn.core.utility;

import java.util.LinkedList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;

public class AsynchronousActiveQueue {
	public static interface IRecordHandler {
		public void process(IProgressMonitor monitor, IActionOperation op, Object... record);
	}
	
	protected String name;
	protected LinkedList queue;
	protected IRecordHandler handler;
	protected boolean system;

	public AsynchronousActiveQueue(String queueName, IRecordHandler handler, boolean system) {
		this.name = queueName;
		this.queue = new LinkedList();
		this.handler = handler;
		this.system = system;
	}
	
	public void push(Object... data) {
		synchronized (this.queue) {
    		this.queue.add(data);
	    	if (this.queue.size() == 1) {
				ProgressMonitorUtility.doTaskScheduledDefault(new AbstractActionOperation(this.name, SVNMessages.class) { //$NON-NLS-1$
					public ISchedulingRule getSchedulingRule() {
						return null;
					}
					protected void runImpl(IProgressMonitor monitor) throws Exception {
						while (true) {
							Object []record;
							synchronized (AsynchronousActiveQueue.this.queue) {
								if (monitor.isCanceled() || AsynchronousActiveQueue.this.queue.size() == 0) {
									AsynchronousActiveQueue.this.queue.clear();
									break;
								}
								record = (Object [])AsynchronousActiveQueue.this.queue.get(0);
							}
							AsynchronousActiveQueue.this.handler.process(monitor, this, record);
							synchronized (AsynchronousActiveQueue.this.queue) {
								AsynchronousActiveQueue.this.queue.remove(0);
								if (AsynchronousActiveQueue.this.queue.size() == 0) {
									break;
								}
							}
						}
					}
				}, this.system);
	    	}
		}		
	}
}
