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

import java.util.Date;


/**
 * Replacement for org.tigris.subversion.javahl.BlameCallback
 * 
 * @author Alexander Gurov
 */
public interface BlameCallback {
    /**
     * the method will be called for every line in a file.
     * @param changed           the date of the last change.
     * @param revision          the revision of the last change.
     * @param author            the author of the last change.
     * @param merged_date       the date of the last merged change.
     * @param merged_revision   the revision of the last merged change.
     * @param merged_author     the author of the last merged change.
     * @param merged_path       the path of the last merged change.
     * @param line              the line in the file
     */
    public void singleLine(long date, long revision, String author,
            Date merged_date, long merged_revision,
            String merged_author, String merged_path,
            String line);
}
