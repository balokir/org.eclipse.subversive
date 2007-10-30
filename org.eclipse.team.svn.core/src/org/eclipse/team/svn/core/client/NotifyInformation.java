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
 * Replacement for org.tigris.subversion.javahl.NotifyInformation
 * 
 * @author Alexander Gurov
 */
public class NotifyInformation {
    /**
     * the path of the item, which is the source of the event.
     */
    public final String path;
    /**
     * the action, which triggered this event (See NotifyAction).
     */
    public final int action;
    /**
     * the kind of the item (See NodeKind).
     */
    public final int kind;
    /**
     * the mime type of the item.
     */
    public final String mimeType;
    /**
     * any lock for the item
     */
    public final Lock lock;
    /**
     * any error message for the item
     */
    public final String errMsg;
    /**
     * the state of the content of the item (See NotifyStatus).
     */
    public final int contentState;
    /**
     * the state of the properties of the item (See NotifyStatus).
     */
    public final int propState;
    /**
     * the state of the lock of the item (See LockStatus).
     */
    public final int lockState;
    /**
     * the revision of the item.
     */
    public final long revision;

    public NotifyInformation(String path, int action, int kind, String mimeType, Lock lock, String errMsg, int contentState, int propState, int lockState, long revision) {
        this.path = path;
        this.action = action;
        this.kind = kind;
        this.mimeType = mimeType;
        this.lock = lock;
        this.errMsg = errMsg;
        this.contentState = contentState;
        this.propState = propState;
        this.lockState = lockState;
        this.revision = revision;
    }

}
