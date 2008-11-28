/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core;

import java.lang.reflect.Field;

import org.eclipse.osgi.util.NLS;

public class SVNMessages extends NLS {

	protected static final String BUNDLE_NAME = "org.eclipse.team.svn.core.messages";	 //$NON-NLS-1$
	
	public static String Console_Action_Added;
	public static String Console_Action_Deleted;
	public static String Console_Action_Locked;
	public static String Console_Action_Modified;
	public static String Console_Action_Replaced;
	public static String Console_Action_Restored;
	public static String Console_Action_Reverted;
	public static String Console_Action_Unlocked;
		
	public static String Console_AtRevision;
	public static String Console_CommittedRevision;
	
	public static String Console_Status;
	public static String Console_Status_Added;
	public static String Console_Status_New;
	public static String Console_Status_Modified;
	public static String Console_Status_Deleted;
	public static String Console_Status_Missing;
	public static String Console_Status_Replaced;
	public static String Console_Status_Merged;
	public static String Console_Status_Conflicted;
	public static String Console_Status_Obstructed;
	
	public static String Console_TransmittingData;
	
	public static String MergeScope_Name;
	
	public static String Operation_Error_LogHeader;
	public static String Operation_ExportProjectSet;
	public static String Operation_ExtractTo;
	public static String Operation_ExtractTo_Folders;
	public static String Operation_ExtractTo_LocalFile;
	public static String Operation_ExtractTo_RemoteFile;
	public static String Operation_ShareFile_DefaultComment;		
	public static String Operation_ShareProject_DefaultComment;
	
	public static String Progress_Done;
	public static String Progress_Running;
	public static String Progress_SubTask;
	
	public static String SVNInfo_Author;
	
	static {
		//load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, SVNMessages.class);
	}
	
	public static String format(String translatedString, Object[] args) {
		return NLS.bind(translatedString, args);
	}
	
    /**
     * Gets a resource string by field name. 
     * This is useful when the field name is constructed ad hoc.
     * 
     * @param key
     * @return
     */
    public static String getString(String key) {
    	String str = SVNMessages.getRawString(key);
    	if (str == null) {
    		str = "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
    	}
    	return str;
    }	            
    
    public static boolean hasString(String key) {
    	return SVNMessages.getRawString(key) != null;
    }
    
    protected static String getRawString(String key) {
    	String res = null;
    	Class c = SVNMessages.class;
    	try {
    		Field field = c.getDeclaredField(key);
    		res = (String)field.get(null);
    	} catch (Exception e) {
    		res = null;
    	}
    	return res;
    } 
    
    /**
     * Convenience method used for error messages,
     * where for message we also add its error identifier 
     * 
     * @param key
     * @return
     */
	public static String getErrorString(String key) {						
		String retVal = SVNMessages.getString(key);		
		if (SVNMessages.hasString(key)) {
			if (key.startsWith("Error_")) { //$NON-NLS-1$
				String idKey = key + "_Id"; //$NON-NLS-1$
				if (SVNMessages.hasString(idKey)) {
					retVal = idKey + ": " + retVal; //$NON-NLS-1$
				}				
			}			
		}
		return retVal;
	}
	
	public static String formatErrorString(String key, Object[] args) {
		String retVal = SVNMessages.getErrorString(key);
		return SVNMessages.format(retVal, args);
	}
}
