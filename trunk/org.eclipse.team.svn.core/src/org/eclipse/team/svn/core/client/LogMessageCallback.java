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
 * Replacement for org.tigris.subversion.javahl.LogMessageCallback
 * 
 * @author Alexander Gurov
 */
public interface LogMessageCallback
{
    /**
     * The method will be called for every log message.
     *
     * @param log   complete log message
     * @param hasChildren    when merge sensitive option was requested,
     *                       whether or not this entry has child entries.
     */
    public void singleMessage(LogMessage log, boolean hasChildren);
}
