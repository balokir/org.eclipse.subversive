/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.verifier;

import org.eclipse.swt.widgets.Control;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;

/**
 * Integer field verifier
 * 
 * @author Alexander Gurov
 */
public class IntegerFieldVerifier extends AbstractFormattedVerifier {
    protected static String ERROR_NAN;
    protected static String ERROR_NEGATIVE;
    
    protected boolean positive;
    
    public IntegerFieldVerifier(String fieldName, boolean positive) {
        super(fieldName);
        this.positive = positive;
        IntegerFieldVerifier.ERROR_NAN = SVNTeamUIPlugin.instance().getResource("Verifier.IntegerField.NaN", new String[] {AbstractFormattedVerifier.FIELD_NAME});
        IntegerFieldVerifier.ERROR_NEGATIVE = SVNTeamUIPlugin.instance().getResource("Verifier.IntegerField.Negative", new String[] {AbstractFormattedVerifier.FIELD_NAME});
    }
    
    protected String getErrorMessageImpl(Control input) {
        String text = this.getText(input);
        try {
            long i = Long.parseLong(text);
            if (this.positive && i < 0) {
            	return IntegerFieldVerifier.ERROR_NEGATIVE;
            }
        }
        catch (Exception ex) {
            return IntegerFieldVerifier.ERROR_NAN;
        }
        return null;
    }

    protected String getWarningMessageImpl(Control input) {    	
        return null;
    }

}
