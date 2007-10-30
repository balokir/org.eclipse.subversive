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
 * Repalcement for org.tigris.subversion.javahl.Notify2
 * 
 * @author Alexander Gurov
 */
public interface Notify2 {
    /**
     * Handler for Subversion notifications.
     *
     * Override this function to allow Subversion to
     * send notifications
     * @param info everything to know about this event
     */
    public void onNotify(NotifyInformation info);
}
