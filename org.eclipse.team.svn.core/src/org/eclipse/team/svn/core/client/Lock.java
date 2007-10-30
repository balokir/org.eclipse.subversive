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
 * Replacement for org.tigris.subversion.javahl.Lock
 * 
 * @author Alexander Gurov
 */
public class Lock {
    /**
     * the owner of the lock
     */
    public final String owner;
    /**
     * the path of the locked item
     */
    public final String path;
    /**
     * the token provided during the lock operation
     */
    public final String token;
    /**
     * the comment provided during the lock operation
     */
    public final String comment;
    /**
     * the date when the lock was created
     */
    public final long creationDate;
    /**
     * the date when the lock will expire
     */
    public final long expirationDate;
    /**
     * this constructor should only called from JNI code
     * @param owner             the owner of the lock
     * @param path              the path of the locked item
     * @param token             the lock token
     * @param comment           the lock comment
     * @param creationDate      the date when the lock was created
     * @param expirationDate    the date when the lock will expire
     */
    public Lock(String owner, String path, String token, String comment, long creationDate, long expirationDate) {
        this.owner = owner;
        this.path = path;
        this.token = token;
        this.comment = comment;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
    }

}
