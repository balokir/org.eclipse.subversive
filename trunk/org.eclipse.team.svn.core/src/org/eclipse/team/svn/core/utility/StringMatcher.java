/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elena Matokhina - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.utility;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Matcher for some string mask
 * 
 * @author Elena Matokhina 
 */
public class StringMatcher {

    protected static final String NOT_MASKING = "^[a-zA-Z_0-9]$";

    protected Pattern filter;

    public StringMatcher(String mask) {
        this.filter = Pattern.compile(this.getRegexp(mask), Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    }

    protected String getRegexp(String pattern) {
        Pattern regexp = Pattern.compile(NOT_MASKING);
        StringBuffer ret = new StringBuffer();
        
        if (pattern.length() == 0) {
			return "\\s*";
		}
        
        for (int i = 0; i < pattern.length(); i++) {
            String ch = pattern.substring(i, i + 1);
            Matcher matcher = regexp.matcher(ch);
            if (matcher.matches()) {
                ret.append(ch);
            } else if ("?".equals(ch)) {
                ret.append(".");
            } else if ("*".equals(ch)) {
                ret.append(".*");
            } else {
                ret.append("\\");
                ret.append(ch);
            }
        }
        return ret.toString();
    }
    
    public boolean match(String text) {
        if (text == null) {
            return false;
        }
        Matcher matcher = filter.matcher(text);
        return matcher.matches();
    }
}
