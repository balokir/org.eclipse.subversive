/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.extension.factory;

/**
 * Allows modification of the commit panel parameters 
 * 
 * @author Alexander Gurov
 * 
 * @deprecated use {@link ICommentManager} instead.
 */
public interface IModifiableCommentDialogPanel extends ICommentDialogPanel {
	/**
	 * Changes message in the commit panel.
	 * Be sure to call it from UI thread and before the actual widget is disposed or you'll get an exception.
	 */
	public void setMessage(String message);
}
