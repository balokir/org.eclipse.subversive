/*******************************************************************************
 * Copyright (c) 2008 Thomas Champagne.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Thomas Champagne - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.verifier;

import java.text.SimpleDateFormat;

import org.eclipse.swt.widgets.Control;
import org.eclipse.team.svn.ui.SVNUIMessages;

/**
 * Date Format Verifier. Check if the text is a good date and time pattern 
 * based on {@link SimpleDateFormat}.
 * @author Thomas Champagne
 */
public class DateFormatVerifier extends AbstractFormattedVerifier {
    
    public DateFormatVerifier(String fieldName) {
        super(fieldName);
    }
	
	@Override
	protected String getErrorMessageImpl(Control input) {
		try {
			new SimpleDateFormat(this.getText(input));
		} catch (IllegalArgumentException e) {
			return SVNUIMessages.Verifier_DateFormat_Error;
		}
		return null;
	}
	/**
	 * There isn't warning message.
	 */
	@Override
	protected String getWarningMessageImpl(Control input) {
		return null;
	}
}
