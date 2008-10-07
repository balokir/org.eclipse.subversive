/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexey Mikoyan - Initial implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.verifier;

import org.eclipse.swt.widgets.Control;
import org.eclipse.team.svn.core.utility.StringMatcher;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;

/**
 * File Name Template Verifier
 *
 * @author Alexey Mikoyan
 *
 */
public class FileNameTemplateVerifier extends AbstractFormattedVerifier {

	public static String ERROR_MESSAGE;

	public FileNameTemplateVerifier(String fieldName) {
		super(fieldName);
		FileNameTemplateVerifier.ERROR_MESSAGE = SVNTeamUIPlugin.instance().getResource("Verifier.FileNameTemplate", new String[] {AbstractFormattedVerifier.FIELD_NAME});
	}

	protected String getErrorMessageImpl(Control input) {
		try {
			new StringMatcher(this.getText(input));
		}
		catch (Exception e) {
			return FileNameTemplateVerifier.ERROR_MESSAGE;
		}
		return null;
	}

	protected String getWarningMessageImpl(Control input) {
		return null;
	}

}
