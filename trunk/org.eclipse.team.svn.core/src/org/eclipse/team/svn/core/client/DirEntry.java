/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.client;

import java.util.Date;

/**
 * Client dir entry presentation
 * 
 * @author Alexander Gurov
 */
public class DirEntry {
	public final Date lastChanged;
	public final long lastChangedRevision;
	public final boolean hasProps;
	public final String lastAuthor;
	public final int nodeKind;
	public final long size;
	public final String path;
	public final Lock lock;

	public DirEntry(Date lastChanged, long lastChangedRevision, boolean hasProps, String lastAuthor, int nodeKind, long size, String path, Lock lock) {
		this.lastChanged = lastChanged;
		this.lastChangedRevision = lastChangedRevision;
		this.hasProps = hasProps;
		this.lastAuthor = lastAuthor;
		this.nodeKind = nodeKind;
		this.size = size;
		this.path = path;
		this.lock = lock;
	}
	
    /**
     * The various field values which can be passed to list()
     */
    public class Fields
    {
        /**
         * An indication that you are interested in the nodeKind field
         */
        public static final int nodeKind = 0x00001;

        /**
         * An indication that you are interested in the size field
         */
        public static final int size = 0x00002;

        /**
         * An indication that you are interested in the hasProps field
         */
        public static final int hasProps = 0x00004;

        /**
         * An indication that you are interested in the lastChangedRevision
         * field
         */
        public static final int lastChangeRevision = 0x00008;

        /**
         * An indication that you are interested in the lastChanged field
         */
        public static final int lastChanged = 0x00010;

        /**
         * An indication that you are interested in the lastAuthor field
         */
        public static final int lastAuthor = 0x00020;

        /**
         * A combination of all the DirEntry fields
         */
        public static final int all = ~0;
    }

}
