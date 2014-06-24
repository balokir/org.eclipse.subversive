/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.view.property;

import java.util.StringTokenizer;

import org.eclipse.team.svn.ui.SVNUIMessages;

/**
 * SVN keyword property
 * 
 * @author Sergiy Logvin
 */
public class SVNKeywordProperty {
    
	// names:
    public static final String []DATE_NAMES = {"LastChangedDate", "Date"};     //$NON-NLS-1$ //$NON-NLS-2$
    public static final String []REVISION_NAMES = {"LastChangedRevision", "Rev"};     //$NON-NLS-1$ //$NON-NLS-2$
    public static final String []AUTHOR_NAMES = {"LastChangedBy", "Author"};     //$NON-NLS-1$ //$NON-NLS-2$
    public static final String []HEAD_URL_NAMES = {"HeadURL", "URL"};     //$NON-NLS-1$ //$NON-NLS-2$
    public static final String []ID_NAMES = {"Id"};     //$NON-NLS-1$
    public static final String []HEADER_NAMES = {"Header"};     //$NON-NLS-1$
    
    // descriptions:
    public static String DATE_DESCR() {
    	return SVNUIMessages.SVNKeywordProperty_DATE_DESCR;
    }
    public static String REVISION_DESCR() {
    	return SVNUIMessages.SVNKeywordProperty_REVISION_DESCR;
    }
    public static String AUTHOR_DESCR() {
    	return SVNUIMessages.SVNKeywordProperty_AUTHOR_DESCR;
    }
    public static String HEAD_URL_DESCR() {
    	return SVNUIMessages.SVNKeywordProperty_HEAD_URL_DESCR;
    }
    public static String ID_DESCR() {
    	return SVNUIMessages.SVNKeywordProperty_ID_DESCR;
    }
    public static String HEADER_DESCR() {
    	return SVNUIMessages.SVNKeywordProperty_HEADER_DESCR;
    }
    
    // samples:
    public static final String DATE_SAMPLE = "$LastChangedDate: 2006-08-07 15:40:37 +0000 (Mon, 08 Aug 2006) $";     //$NON-NLS-1$
    public static final String REVISION_SAMPLE = "$LastChangedRevision: 7206 $"; //$NON-NLS-1$
    public static final String AUTHOR_SAMPLE = "$LastChangedBy: J.M.Wade $"; //$NON-NLS-1$
    public static final String HEAD_URL_SAMPLE = "$HeadURL: http://svn.eclipse.org/community/Subversive/src/ui/PropertyKeywordEditPanel.java $"; //$NON-NLS-1$
    public static final String ID_SAMPLE = "$Id: PropertyKeywordEditPanel.java 7206 2006-08-07 15:40:37 J.M.Wade $"; //$NON-NLS-1$
    public static final String HEADER_SAMPLE = "$Header: http://svn.example.com/repos/trunk/calc.c 148 2006-07-28 21:30:43Z sally $"; //$NON-NLS-1$
    
    protected boolean dateEnabled;
    protected boolean revisionEnabled;
    protected boolean lastChangedByEnabled;
    protected boolean headURLEnabled;
    protected boolean idEnabled;
    protected boolean headerEnabled;

    public SVNKeywordProperty(String keywordsValue) {
        if (keywordsValue != null) {
        	this.parsePropertyValue(keywordsValue);
        }
    }
    
    public boolean isHeadUrlEnabled() {
		return this.headURLEnabled;
	}

	public boolean isIdEnabled() {
		return this.idEnabled;
	}

	public boolean isHeaderEnabled() {
		return this.headerEnabled;
	}

	public boolean isLastChangedByEnabled() {
		return this.lastChangedByEnabled;
	}

	public boolean isDateEnabled() {
		return this.dateEnabled;
	}

	public boolean isRevisionEnabled() {
		return this.revisionEnabled;
	}

	public void setHeadUrlEnabled(boolean headURL) {
		this.headURLEnabled = headURL;
	}

	public void setIdEnabled(boolean id) {
		this.idEnabled = id;
	}

	public void setHeaderEnabled(boolean header) {
		this.headerEnabled = header;
	}

	public void setLastChangedByEnabled(boolean lastChangedBy) {
		this.lastChangedByEnabled = lastChangedBy;
	}

	public void setDateEnabled(boolean lastChangedDate) {
		this.dateEnabled = lastChangedDate;
	}

	public void setRevisionEnabled(boolean lastChangedRev) {
		this.revisionEnabled = lastChangedRev;
	}
	
    public String toString() {
    	String result = ""; //$NON-NLS-1$
    	if (this.dateEnabled) {
    		result = this.addKeyword(result, SVNKeywordProperty.DATE_NAMES[0]);
    	}
    	if (this.revisionEnabled) {
    		result = this.addKeyword(result, SVNKeywordProperty.REVISION_NAMES[0]); 
    	}
    	if (this.lastChangedByEnabled) {
    		result = this.addKeyword(result, SVNKeywordProperty.AUTHOR_NAMES[0]);
    	}
        if (this.headURLEnabled) {
        	result = this.addKeyword(result, SVNKeywordProperty.HEAD_URL_NAMES[0]);
        }
        if (this.idEnabled) {
        	result = this.addKeyword(result, SVNKeywordProperty.ID_NAMES[0]);
        }
        if (this.headerEnabled) {
        	result = this.addKeyword(result, SVNKeywordProperty.HEADER_NAMES[0]);
        }
        
        return result;
    }
    
    protected String addKeyword(String result, String keyword) {
    	if (result.trim().length() > 0) {
			result += ' ';
		}
		result += keyword;
		
		return result;
    }
	
	protected void parsePropertyValue(String keywordValue) {
		StringTokenizer st = new StringTokenizer(keywordValue, " "); //$NON-NLS-1$
        while (st.hasMoreTokens()) {
            String name = st.nextToken();
            if (name.equals(SVNKeywordProperty.DATE_NAMES[0]) || name.equals(SVNKeywordProperty.DATE_NAMES[1])) {
            	this.dateEnabled = true;
            }
            else if (name.equals(SVNKeywordProperty.REVISION_NAMES[0]) || name.equals(SVNKeywordProperty.REVISION_NAMES[1])) {
            	this.revisionEnabled = true;
            }
            else if (name.equals(SVNKeywordProperty.AUTHOR_NAMES[0]) || name.equals(SVNKeywordProperty.AUTHOR_NAMES[1])) {
            	this.lastChangedByEnabled = true;
            }
            else if (name.equals(SVNKeywordProperty.HEAD_URL_NAMES[0]) || name.equals(SVNKeywordProperty.HEAD_URL_NAMES[1])) {
            	this.headURLEnabled = true;
            }
            else if (name.equals(SVNKeywordProperty.ID_NAMES[0])) {
            	this.idEnabled = true;
            }
            else if (name.equals(SVNKeywordProperty.HEADER_NAMES[0])) {
            	this.headerEnabled = true;
            }
        }
	}

}
