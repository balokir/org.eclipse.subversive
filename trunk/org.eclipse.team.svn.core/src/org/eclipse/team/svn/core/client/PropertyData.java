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

package org.eclipse.team.svn.core.client;

/**
 * Replacement for org.tigris.subversion.javahl.PropertyData
 * 
 * @author Alexander Gurov
 */
public class PropertyData {
    /**
     * Standard subversion known properties
     */
    /**
     * mime type of the entry, used to flag binary files
     */
    public static final String MIME_TYPE = "svn:mime-type";
    /**
     * list of filenames with wildcards which should be ignored by add and
     * status
     */
    public static final String IGNORE = "svn:ignore";
    /**
     * how the end of line code should be treated during retrieval
     */
    public static final String EOL_STYLE = "svn:eol-style";
    /**
     * list of keywords to be expanded during retrieval
     */
    public static final String KEYWORDS = "svn:keywords";
    /**
     * flag if the file should be made excutable during retrieval
     */
    public static final String EXECUTABLE = "svn:executable";
    /**
     * value for svn:executable
     */
    public static final String EXECUTABLE_VALUE = "*";
    /**
     * list of directory managed outside of this working copy
     */
    public static final String EXTERNALS = "svn:externals";
    /**
     * the author of the revision
     */
    public static final String REV_AUTHOR = "svn:author";
    /**
     * the log message of the revision
     */
    public static final String REV_LOG = "svn:log";
    /**
     * the date of the revision
     */
    public static final String REV_DATE = "svn:date";
    /**
     * the original date of the revision
     */
    public static final String REV_ORIGINAL_DATE = "svn:original-date";

    /**
     * @since 1.2
     * flag property if a lock is needed to modify this node
     */
    public static final String NEEDS_LOCK = "svn:needs-lock";
    
    /**
     * the name of the property
     */
    public final String name;
    /**
     * the string value of the property
     */
    public final String value;
    /**
     * the byte array value of the property
     */
    public final byte []data;

    public PropertyData(String n, String v, byte[] d) {
        this.name = n;
        this.value = v;
        this.data = d;
    }

}
