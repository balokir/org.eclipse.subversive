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

package org.eclipse.team.svn.ui.verifier;

import java.net.URL;

import org.eclipse.swt.widgets.Control;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNUIMessages;

/**
 * SVN URL verifier
 * 
 * @author Alexander Gurov
 */
public class URLVerifier extends AbstractFormattedVerifier {
    protected static final String ERROR_REASON = "$ERROR_REASON$"; //$NON-NLS-1$
    protected static String ERROR_MESSAGE_SHORT;
    protected static String ERROR_MESSAGE_FULL;

    public URLVerifier(String fieldName) {
        super(fieldName);
        URLVerifier.ERROR_MESSAGE_SHORT = SVNUIMessages.format(SVNUIMessages.Verifier_URL_Short, new String[] {AbstractFormattedVerifier.FIELD_NAME});
        URLVerifier.ERROR_MESSAGE_FULL = SVNUIMessages.format(SVNUIMessages.Verifier_URL_Full, new String[] {AbstractFormattedVerifier.FIELD_NAME, URLVerifier.ERROR_REASON});
    }

    protected String getErrorMessageImpl(Control input) {
        String url = this.getText(input);
        try {
        	URL svnUrl = SVNUtility.getSVNUrl(url);
        	String host = svnUrl.getHost();
        	if (!this.isIPAddress(host) && !host.matches("[a-zA-Z0-9_\\-]+(?:\\.[a-zA-Z0-9_\\-]+)*") && host.length() > 0 || //$NON-NLS-1$
        		host.length() == 0 && !"file".equals(svnUrl.getProtocol())) { //$NON-NLS-1$
        		this.setPlaceHolder(URLVerifier.ERROR_REASON, SVNUIMessages.Verifier_URL_NoHost);
                return URLVerifier.ERROR_MESSAGE_FULL;
        	}      	
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

    protected boolean isIPAddress(String host) {
    	int idx = host.indexOf(']');
    	return idx != -1 && URLVerifier.isIPv6Address(host.substring(1, idx)) || URLVerifier.isIPv4Address(host);
    }
    
    // rewrite an IP address validation code since access to the sun.net.util.IPAddressUtil is restricted
    protected static boolean isIPv4Address(String address) {
    	String[] s = address.split("\\.", -1);
    	try {
    		if (s.length == 1) {
    			long val = Long.parseLong(s[0]);
    			if (val < 0 || val > 0xFFFFFFFFL) {
    				return false;
    			}
    		}
    		else if (s.length == 2) {
    			long val = Long.parseLong(s[0]), val1 = Long.parseLong(s[1]);
    			if (val < 0 || val > 0xFFL || val1 < 0 || val1 > 0xFFFFFFL) {
    				return false;
    			}
    		}
    		else if (s.length == 3) {
    			long val = Long.parseLong(s[0]), val1 = Long.parseLong(s[1]), val2 = Long.parseLong(s[2]);
    			if (val < 0 || val > 0xFFL || val1 < 0 || val1 > 0xFFL || val2 < 0 || val2 > 0xFFFFL) {
    				return false;
    			}
    		}
    		else if (s.length == 4) {
    			for (String p : s) {
    				long val = Long.parseLong(p);
        			if (val < 0 || val > 0xFFL) {
        				return false;
        			}
    			}
    		}
    		else {
    			return false;
    		}
    	}
    	catch (NumberFormatException ex) {
    		return false;
    	}
    	return true;
    }
    
    private final static int IPV4_SIZE = 4;
    private final static int IPV6_SIZE = 16;
    private final static int INT16_SIZE = 2;
    
    protected static boolean isIPv6Address(String address) {
    	if (address.length() < 2) { // :: an empty address is a minimal one
    		return false;
    	}
    	
		char[] addressChars = address.toCharArray();
		int addressLength = addressChars.length;
		
		int scopePos = address.indexOf("%"); //check for scope presence (network interface specifier) 
		if (scopePos == addressLength - 1) { // check if scope format is proper or not
			return false;
		}
		if (scopePos != -1) { // then omit it
			addressLength = scopePos;
		}
		// there is a / character possible in specification of a subnet too, but we don't work with subnets nor with multicasts, so we'll just treat is as a junk address

		int lastColonPosition = -1;
		int i = 0, j = 0;

		if (addressChars[i] == ':') {
			if (addressChars[++i] != ':') { // if starts with : there should be zeros omitted. So, the :: at the beginning is expected
				return false;
			}
		}
		int currentTokenPointer = i;
		boolean hadNumTokenBefore = false;
		int val = 0;
		char ch;
		while (i < addressLength) {
			ch = addressChars[i++];
			int chval = Character.digit(ch, 16);
			if (chval != -1) {
				val <<= 4;
				val |= chval;
				if (val > 0xFFFF) {
					return false;
				}
				hadNumTokenBefore = true;
				continue;
			}
			if (ch == ':') {
				currentTokenPointer = i;
				if (!hadNumTokenBefore) {
					if (lastColonPosition != -1) {
						return false;
					}
					lastColonPosition = j;
					continue;
				} 
				else if (i == addressLength) {
					return false;
				}
				if (j + INT16_SIZE > IPV6_SIZE) {
					return false;
				}
				j += INT16_SIZE;
				hadNumTokenBefore = false;
				val = 0;
				continue;
			}
			if (ch == '.' && ((j + IPV4_SIZE) <= IPV6_SIZE)) {
				String ia4 = address.substring(currentTokenPointer, addressLength);
				int dotCount = 0, index = 0;
				while ((index = ia4.indexOf('.', index)) != -1) {
					dotCount++;
					index++;
				}
				if (dotCount != 3 || !URLVerifier.isIPv4Address(ia4)) { // only 4-part IPv4 address is acceptable
					return false;
				}
				j += IPV4_SIZE;
				hadNumTokenBefore = false;
				break; // IPv4 segment could be the last address part only 
			}
			return false;
		}
		if (hadNumTokenBefore) { // close the numeric token parsing
			if (j + INT16_SIZE > IPV6_SIZE) {
				return false;
			}
			j += INT16_SIZE;
		}

		if (lastColonPosition != -1) { // zero compression at the end
			if (j == IPV6_SIZE) {
				return false;
			}
			j = IPV6_SIZE;
		}
    	
    	return j == IPV6_SIZE;
    }
}
