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

package org.eclipse.team.svn.ui.verifier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.widgets.Control;
import org.eclipse.team.svn.ui.SVNUIMessages;

/**
 * Revision ranges input field verifier
 * 
 * @author Alexander Gurov
 */
public class RevisionRangesVerifier extends AbstractFormattedVerifier {
    protected static String ERROR_MESSAGE;
    
	protected Pattern pattern;

	public RevisionRangesVerifier(String fieldName) {
		super(fieldName);
		RevisionRangesVerifier.ERROR_MESSAGE = SVNUIMessages.format(SVNUIMessages.Verifier_RevisionRanges, new String[] {AbstractFormattedVerifier.FIELD_NAME});
		this.pattern = Pattern.compile("\\d+(-\\d+)?(\\s*,\\s*\\d+(-\\d+)?)*");
	}

	protected String getErrorMessageImpl(Control input) {
        String text = this.getText(input);
        Matcher matcher = this.pattern.matcher(text);
        if (!matcher.matches()) {
        	return RevisionRangesVerifier.ERROR_MESSAGE;
        }
		return null;
	}

	protected String getWarningMessageImpl(Control input) {
		return null;
	}

}
