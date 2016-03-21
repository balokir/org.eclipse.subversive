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

package org.eclipse.team.svn.ui.extension.factory;

import java.util.Collection;

/**
 * Accepts comment templates suggested by integrations.
 * The keys should be the actual headers (internationalized strings to be precise).
 * 
 * @author Alexander Gurov
 */
public interface ICommentManager {
	/**
	 * Previous comments section key
	 */
	public static final String PREVIOUS_COMMENTS_HEADER = "CommentComposite_Previous"; //$NON-NLS-1$
	/**
	 * Header of the section of the template comments which are defined in preferences 
	 */
	public static final String TEMPLATE_HEADER = "CommentComposite_Template"; //$NON-NLS-1$
	/**
	 * Header of the section of comment templates defined in the tsvn:logtemplate property 
	 */
	public static final String TSVN_LOGTEMPLATE_HEADER = "CommentComposite_LogTemplate"; //$NON-NLS-1$

	/**
	 * Adds a comment template section. Should be called at the initialization time. Calling later is basically useless.
	 * The sections precedence is as follows:
	 * 	- previous comments
	 * 	- user-defined comment templates
	 * 	- tsvn:logtemplate comments
	 * 	- the sections that belongs to external integrations
	 * @param sectionHeader section header string, always visible
	 * @param sectionHint section hint, visible when there are no templates to display in the section
	 */
	public void addCommentsSection(String sectionHeader, String sectionHint);

	/**
	 * Adds comment templates to the section's end. Should be called at the initialization time. Calling later is basically useless.
	 * @param sectionHeader
	 * @param templates
	 */
	public void addCommentsToSection(String sectionHeader, Collection<String> templates);
	
	/**
	 * Returns the current message text. Could be called any time.
	 * @return
	 */
	public String getMessage();
	
	/**
	 * Redefines the current message text. Should be called in UI thread context if called not at the initialization time (using saved reference).
	 * @param message
	 */
	public void setMessage(String message);
	
	/**
	 * Returns the comment which was entered by user before pressing the cancel button. 
	 * It will be stored until "Ok" button is pressed or the comment is explicitly cleared by user before pressing the "Cancel" button.
	 * The comments precedence is as follows:
	 * 	- initial forced comment if set
	 *  - temporary saved comment
	 *  - tsvn:logtemplate comment
	 * @return
	 */
	public String getTemporarySavedComment();
}
