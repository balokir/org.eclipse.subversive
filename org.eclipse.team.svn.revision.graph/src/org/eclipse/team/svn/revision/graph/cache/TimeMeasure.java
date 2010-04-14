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

/**
 * For debug purposes to track operation duration
 * 
 * @author Igor Burilo
 */
public class TimeMeasure {

	public static boolean isDebug = false;
	
	protected String message;
	
	protected long start;
	
	public TimeMeasure(String message) {
		if (TimeMeasure.isDebug) {
			this.message = message;
			this.start = System.nanoTime();
			System.out.println("Started: " + message); //$NON-NLS-1$
		}
	}
	
	public void end() {
		if (TimeMeasure.isDebug) {			
			long diff = System.nanoTime() - this.start;
			double show = diff / 1000000000.0;
			System.out.println("--- Finished: " + this.message + ": " + show);	 //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}
