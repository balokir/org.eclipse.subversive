/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elena Matokhina (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.view.property;

import java.util.StringTokenizer;

import org.eclipse.team.svn.ui.SVNTeamUIPlugin;

/**
 * SVN keyword property
 * 
 * @author Elena Matokhina
 */
public class SVNKeywordProperty {
    
	// names:
    public static final String []DATE_NAMES = {"LastChangedDate", "Date"};    
    public static final String []REVISION_NAMES = {"LastChangedRevision", "Rev"};
    public static final String []AUTHOR_NAMES = {"LastChangedBy", "Author"};
    public static final String []HEAD_URL_NAMES = {"HeadURL", "URL"};
    public static final String []ID_NAMES = {"Id"};
    
    // descriptions:
    public static String DATE_DESCR() {
    	return SVNTeamUIPlugin.instance().getResource("SVNKeywordProperty.DATE_DESCR");    
    }
    public static String REVISION_DESCR() {
    	return SVNTeamUIPlugin.instance().getResource("SVNKeywordProperty.REVISION_DESCR");    
    }
    public static String AUTHOR_DESCR() {
    	return SVNTeamUIPlugin.instance().getResource("SVNKeywordProperty.AUTHOR_DESCR");    
    }
    public static String HEAD_URL_DESCR() {
    	return SVNTeamUIPlugin.instance().getResource("SVNKeywordProperty.HEAD_URL_DESCR");    
    }
    public static String ID_DESCR() {
    	return SVNTeamUIPlugin.instance().getResource("SVNKeywordProperty.ID_DESCR");    
    }
    
    // samples:
    public static final String DATE_SAMPLE = "$LastChangedDate: 2006-08-07 15:40:37 +0000 (Mon, 08 Aug 2006) $";    
    public static final String REVISION_SAMPLE = "$LastChangedRevision: 7206 $";
    public static final String AUTHOR_SAMPLE = "$LastChangedBy: J.M.Wade $";
    public static final String HEAD_URL_SAMPLE = "$HeadURL: http://svn.polarion.org/community/Subversive/src/svn/ui/PropertyKeywordEditPanel.java $";
    public static final String ID_SAMPLE = "$Id: PropertyKeywordEditPanel.java 7206 2006-08-07 15:40:37 J.M.Wade $";
    
    protected boolean dateEnabled;
    protected boolean revisionEnabled;
    protected boolean lastChangedByEnabled;
    protected boolean headURLEnabled;
    protected boolean idEnabled;

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
    	String result = "";
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
		StringTokenizer st = new StringTokenizer(keywordValue, " ");
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
        }
	}

}
