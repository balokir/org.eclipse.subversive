/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexey Mikoyan - Initial implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.repository.model;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * Tooltip message container
 * Provides the way to create tooltip like decoration
 *
 * @author Alexey Mikoyan
 *
 */
public class ToolTipMessage implements IDecoration {
	
	public String prefix = ""; //$NON-NLS-1$
	public String suffix = ""; //$NON-NLS-1$

	public void addOverlay(ImageDescriptor overlay) {
	}

	public void addOverlay(ImageDescriptor overlay, int quadrant) {
	}

	public void addPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void addSuffix(String suffix) {
		this.suffix = suffix;
	}
	
	public String getMessage() {
		return this.prefix + this.suffix;
	}
	
	public IDecorationContext getDecorationContext() {
		return null;
	}

	public void setBackgroundColor(Color color) {
	}

	public void setFont(Font font) {
	}

	public void setForegroundColor(Color color) {
	}

}
