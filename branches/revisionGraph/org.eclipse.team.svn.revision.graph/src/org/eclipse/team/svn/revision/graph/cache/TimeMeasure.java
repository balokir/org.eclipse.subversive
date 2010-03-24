/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.revision.graph.cache;

import java.util.Date;

/**
 * For debug purposes to track operation duration
 * 
 * @author Igor Burilo
 */
public class TimeMeasure {

	boolean isActive = false;
	
	String message;
	
	Date start;
	Date end;
	
	public TimeMeasure(String message) {
		if (this.isActive) {
			this.message = message;
			this.start = new Date();
			System.out.println("Started: " + message);
		}
	}
	
	public void end() {
		if (this.isActive) {
			this.end = new Date();
			long diff = this.end.getTime() - this.start.getTime();
			double show = diff / 1000.0;
			System.out.println("--- Finished: " + message + ": " + show);	
		}
	}
}
