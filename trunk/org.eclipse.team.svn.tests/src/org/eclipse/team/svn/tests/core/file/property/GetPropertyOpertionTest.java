/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.tests.core.file.property;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.client.PropertyData;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.file.AbstractFileOperation;
import org.eclipse.team.svn.core.operation.file.property.GetPropertiesOperation;
import org.eclipse.team.svn.tests.core.file.AbstractOperationTestCase;

/**
 * Get property opertion test
 * 
 * @author Sergiy Logvin
 */
public class GetPropertyOpertionTest extends AbstractOperationTestCase {
	
	protected boolean removed;
	
	public GetPropertyOpertionTest(boolean removed) {
		this.removed = removed;
	}
	
	protected IActionOperation getOperation() {
		return new AbstractFileOperation("Get Properties Operation Test", this.getListFiles()) {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				GetPropertiesOperation getOp = new GetPropertiesOperation(GetPropertyOpertionTest.this.getFirstFolder());
				getOp.run(monitor);
				boolean containsTestProperty = false;
				PropertyData []properties = getOp.getProperties();
				for (int i = 0; i < properties.length; i++) {
					if (properties[i].name.equals(SetPropertyOperationTest.TEST_PROPERTY_NAME) &&
							properties[i].value.equals(SetPropertyOperationTest.TEST_PROPERTY_VALUE)) {
						containsTestProperty = true;
						break;
					}
				}
				if (GetPropertyOpertionTest.this.removed) {
					assertFalse(containsTestProperty);
				}
				else {
					assertTrue(containsTestProperty);
				}
			}
		};
	}
	
}
