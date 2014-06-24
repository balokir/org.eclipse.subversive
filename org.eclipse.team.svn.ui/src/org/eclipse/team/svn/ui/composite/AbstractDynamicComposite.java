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
package org.eclipse.team.svn.ui.composite;

import org.eclipse.swt.widgets.Composite;

/**
 * Helps to create components that can be made visible or invisible on demand
 *
 * @author Alexey Mikoyan
 *
 */
public abstract class AbstractDynamicComposite extends Composite {
	
	public AbstractDynamicComposite(Composite parent, int style) {
		super(parent, style);
	}
	
	public abstract void saveAppearance();
	
	public abstract void restoreAppearance();
	
	public abstract void revalidateContent();

}
