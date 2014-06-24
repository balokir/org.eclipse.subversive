/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.verifier;

import org.eclipse.swt.widgets.Control;
import org.eclipse.team.svn.ui.SVNUIMessages;

/**
 * Proxy port verifier
 *
 * @author Sergiy Logvin
 */
public class ProxyPortVerifier extends AbstractFormattedVerifier {
    protected static String ERROR_RANGE;
    protected static String ERROR_NAN;

    public ProxyPortVerifier(String fieldName) {
        super(fieldName);
        ProxyPortVerifier.ERROR_RANGE = SVNUIMessages.format(SVNUIMessages.Verifier_ProxyPort_Range, new String[] {AbstractFormattedVerifier.FIELD_NAME});
        ProxyPortVerifier.ERROR_NAN = SVNUIMessages.format(SVNUIMessages.Verifier_ProxyPort_NaN, new String[] {AbstractFormattedVerifier.FIELD_NAME});
    }

    protected String getErrorMessageImpl(Control hostField) {
    	String portString = this.getText(hostField);
    	if (portString.trim().length() == 0) {
            return null;
        }        
        try {
        	int port = new Integer(portString).intValue();
        	if (port < 0 || port > 65535) {
        		return ProxyPortVerifier.ERROR_RANGE;
        	}
        }
        catch (IllegalArgumentException ex) {
            return ProxyPortVerifier.ERROR_NAN;
        }
        return null;
    }

    protected String getWarningMessageImpl(Control input) {
        return null;
    }

}

