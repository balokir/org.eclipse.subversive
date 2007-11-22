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

package org.eclipse.team.svn.core.connector;

/**
 * Annotation call-back interface
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL connector library
 * is not EPL compatible and we won't to pin plug-in with concrete connector implementation. So, the only way to do this is
 * providing our own connector interface which will be covered by concrete connector implementation.
 * 
 * @author Alexander Gurov
 */
public interface ISVNAnnotationCallback {
	/**
	 * This method will be called by the connector library for every line in a file.
	 * 
	 * @param line
	 *            the annotated line content
	 * @param revision
	 *            the annotated line revision
	 * @param date
	 *            the annotated line change date
	 * @param author
	 *            the annotated line author
	 * @param mergedWithRevision
	 *            the revision of the last change merged into the line
	 * @param mergedWithDate
	 *            the date of the last change merged into the line
	 * @param mergedWithAuthor
	 *            the author of the last change merged into the line
	 * @param mergedWithPath
	 *            the path of the last change merged into the line
	 */
	public void annotate(String line, long revision, long date, String author, long mergedWithRevision, long mergedWithDate, String mergedWithAuthor, String mergedWithPath);
}
