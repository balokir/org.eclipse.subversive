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

package org.eclipse.team.svn.ui.properties;

import java.util.ResourceBundle;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.contentmergeviewer.ContentMergeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * SVN property merge viewer
 * 
 * @author Alexander Gurov
 */
public class PropertyMergeViewer extends ContentMergeViewer {
	protected static final String RESOURCES_BUNDLE_NAME = PropertyMergeViewer.class.getName();

	public PropertyMergeViewer(Composite parent, CompareConfiguration cc) {
		super(SWT.NONE, ResourceBundle.getBundle(PropertyMergeViewer.RESOURCES_BUNDLE_NAME), cc);
		this.setConfirmSave(true);
		this.buildControl(parent);
	}

	protected void createControls(Composite composite) {

	}

	protected void handleResizeAncestor(int x, int y, int width, int height) {

	}

	protected void handleResizeLeftRight(int x, int y, int leftWidth, int centerWidth, int rightWidth, int height) {

	}

	protected void updateContent(Object ancestor, Object left, Object right) {

	}

	protected void copy(boolean leftToRight) {

	}

	protected byte[] getContents(boolean left) {
		return null;
	}

}
