/*******************************************************************************
 * Copyright (c) 2016 Andrey Loskutov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrey Loskutov <loskutov@gmx.de> - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.utility;

/**
 * An element suitable for queuing into  {@link AsynchronousActiveQueue}.
 * @param <Data>
 */
public interface IQueuedElement<Data extends IQueuedElement<Data>> {

	/**
	 * Helps {@link AsynchronousActiveQueue} to decide if the element can be
	 * skipped instead of being added to the queue.
	 * 
	 * @return {@code true} if this element supports skipping
	 */	
	boolean canSkip();
	
	/**
	 * Helps {@link AsynchronousActiveQueue} to decide if two elements can be merged together
	 * @param d non null
	 * @return {@code true} if this element can be merged with given element
	 */
	boolean canMerge(Data d);

	/**
	 * Merges two elements together
	 * @param d non null
	 * @return a new instance containing data from this and given element
	 */
	Data merge(Data d);

}