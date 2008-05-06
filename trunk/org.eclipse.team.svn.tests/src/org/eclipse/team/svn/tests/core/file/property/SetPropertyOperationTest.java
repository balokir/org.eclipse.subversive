/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.tests.core.file.property;

import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.file.property.SetPropertyOperation;
import org.eclipse.team.svn.tests.core.file.AbstractOperationTestCase;

/**
 * Set property operation test
 * 
 * @author Sergiy Logvin
 */
public class SetPropertyOperationTest extends AbstractOperationTestCase {
	public static final String TEST_PROPERTY_NAME = "test-property";
	public static final String TEST_PROPERTY_VALUE = "test-value";

	protected IActionOperation getOperation() {
		
		return new SetPropertyOperation(this.getListFilesRecursive(), 
				SetPropertyOperationTest.TEST_PROPERTY_NAME, 
				SetPropertyOperationTest.TEST_PROPERTY_VALUE.getBytes(),
				false);
	}

}
