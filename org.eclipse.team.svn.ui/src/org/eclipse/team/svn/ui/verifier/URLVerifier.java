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

import java.net.URL;
import java.text.MessageFormat;

import org.eclipse.swt.widgets.Control;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;

/**
 * SVN URL verifier
 * 
 * @author Alexander Gurov
 */
public class URLVerifier extends AbstractFormattedVerifier {
    protected static final String ERROR_REASON = "$ERROR_REASON$";
    protected static String ERROR_MESSAGE_SHORT;
    protected static String ERROR_MESSAGE_FULL;

    public URLVerifier(String fieldName) {
        super(fieldName);
        URLVerifier.ERROR_MESSAGE_SHORT = SVNTeamUIPlugin.instance().getResource("Verifier.URL.Short");
        URLVerifier.ERROR_MESSAGE_SHORT = MessageFormat.format(URLVerifier.ERROR_MESSAGE_SHORT, new String[] {AbstractFormattedVerifier.FIELD_NAME});
        URLVerifier.ERROR_MESSAGE_FULL = SVNTeamUIPlugin.instance().getResource("Verifier.URL.Full");
        URLVerifier.ERROR_MESSAGE_FULL = MessageFormat.format(URLVerifier.ERROR_MESSAGE_FULL, new String[] {AbstractFormattedVerifier.FIELD_NAME, URLVerifier.ERROR_REASON});
    }

    protected String getErrorMessageImpl(Control input) {
        String url = this.getText(input);
        try {
        	URL svnUrl = SVNUtility.getSVNUrl(url);
        	String host = svnUrl.getHost();
        	if (!host.matches("[a-zA-Z0-9_\\-]+(?:\\.[a-zA-Z0-9_\\-]+)*") && host.length() > 0 ||
        		host.length() == 0 && !"file".equals(svnUrl.getProtocol())) {
        		this.setPlaceHolder(URLVerifier.ERROR_REASON, SVNTeamUIPlugin.instance().getResource("Verifier.URL.NoHost"));
                return URLVerifier.ERROR_MESSAGE_FULL;
        	}
        	
        	//try to decode url - if there is any exception thrown - it should be displayed
        	SVNUtility.decodeURL(url);
        	
            return null;
        }
        catch (Exception ex) {
            this.setPlaceHolder(URLVerifier.ERROR_REASON, ex.getMessage());
            return ex.getMessage() == null ? URLVerifier.ERROR_MESSAGE_SHORT : URLVerifier.ERROR_MESSAGE_FULL;
        }
    }

    protected String getWarningMessageImpl(Control input) {
        return null;
    }

}
