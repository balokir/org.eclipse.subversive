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
 * Replacement for org.tigris.subversion.javahl.LogMessage
 * 
 * @author Alexander Gurov
 */
public class LogMessage {
    /**
     * the log message for the revision
     */
    public final String message;
    /**
     * the date of the commit
     */
    public final long date;
    /**
     * the number of the revision
     */
    public final long revision;
    /**
     * the author of the commit
     */
    public final String author;

    /**
     * the items changed by this commit (only set when
     * SVNClientInterface.logmessage is used with discoverPaths true.
     */
    public final ChangePath[] changedPaths;

    /**
     * this constructor is only called only from JNI code
     * @param m     the log message text
     * @param d     the date of the commit
     * @param r     the number of the revision
     * @param a     the author of the commit
     * @param cp    the items changed by this commit
     */
    public LogMessage(String m, long d, long r, String a, ChangePath[] cp) {
        this.message = m;
        this.date = d;
        this.revision = r;
        this.author = a;
        this.changedPaths = cp;
    }

}
