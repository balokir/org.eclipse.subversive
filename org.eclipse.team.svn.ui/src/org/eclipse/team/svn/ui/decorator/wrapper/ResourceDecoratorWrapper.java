/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *    Panagiotis Korros - [patch] Lazy-loader for the decorators
 *******************************************************************************/

package org.eclipse.team.svn.ui.decorator.wrapper;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.team.svn.ui.decorator.FileDecorator;
import org.eclipse.team.svn.ui.decorator.FolderDecorator;

/**
 * Wrapper for file and folder decorators
 *
 * @author Sergiy Logvin
 */
public class ResourceDecoratorWrapper extends AbstractDecoratorWrapper {
	private FileDecorator fileDecorator;
	private FolderDecorator folderDecorator;
	
	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof IResource) {
			IResource resource = (IResource)element;
			if (!AbstractDecoratorWrapper.isSVNShared(resource)) {
				return;
			}
		}

		if (element instanceof IFolder) {
			this.getFolderDecorator().decorate(element, decoration);
		}
		else if (element instanceof IFile) {
			this.getFileDecorator().decorate(element, decoration);
		}
	}

	protected synchronized FileDecorator getFileDecorator() {
		if (this.fileDecorator == null) {
			this.fileDecorator = new FileDecorator(this);
		}				
		return this.fileDecorator;
	}

	protected synchronized FolderDecorator getFolderDecorator() {
		if (this.folderDecorator == null) {
			this.folderDecorator = new FolderDecorator(this);
		}
		return this.folderDecorator;
	}

}
