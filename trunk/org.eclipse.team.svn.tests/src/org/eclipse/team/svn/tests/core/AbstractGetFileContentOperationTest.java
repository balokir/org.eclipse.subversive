/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alessandro Nistico - [patch] Initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.tests.core;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.operation.AbstractGetFileContentOperation;

/**
 * @author Alessandro Nistico
 */
public class AbstractGetFileContentOperationTest extends TestCase {

	public void testGetSetContent() {
		try {
			final byte[] data = "Hello!".getBytes("US-ASCII");
			AbstractGetFileContentOperation op = new AbstractGetFileContentOperation(
					"Test") {
				protected void runImpl(IProgressMonitor monitor)
						throws Exception {
				}
			};

			InputStream is = op.getContent();
			assertTrue(is instanceof ByteArrayInputStream);
			assertEquals(-1, is.read());
			op.setContent(data);
			is = op.getContent();
			byte[] buffer = new byte[6];
			while (is.read(buffer, 0, buffer.length) > -1)
				;
			assertTrue(Arrays.equals(data, buffer));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
