/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.history.model;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;


/**
 * History view UI node interface
 * 
 * @author Alexander Gurov
 */
public interface ILogNode extends IAdaptable {
	public static final int TYPE_NONE = 0;
	public static final int TYPE_CATEGORY = 1;
	public static final int TYPE_SVN = 2;
	public static final int TYPE_LOCAL = 3;
	
	public static final int LABEL_TRIM = 0;
	public static final int LABEL_FLAT = 1;
	public static final int LABEL_FULL = 2;
	
	public static final int COLUMN_REVISION = 0;
	public static final int COLUMN_DATE = 1;
	public static final int COLUMN_CHANGES = 2;
	public static final int COLUMN_AUTHOR = 3;
	public static final int COLUMN_COMMENT = 4;
	public static final int NUM_OF_COLUMNS = 5;
	
	public int getType();
	public ILogNode []getChildren();
	public boolean hasChildren();
	public boolean requiresBoldFont(long currentRevision);
	public ImageDescriptor getImageDescriptor();
	public String getLabel(int columnIndex, int labelType, long currentRevision);
	
	public Object getEntity();
	
	public long getRevision();
	public long getTimeStamp();
	public String getComment();
	public String getAuthor();
	public int getChangesCount();
	
	public ILogNode getParent();
	
}