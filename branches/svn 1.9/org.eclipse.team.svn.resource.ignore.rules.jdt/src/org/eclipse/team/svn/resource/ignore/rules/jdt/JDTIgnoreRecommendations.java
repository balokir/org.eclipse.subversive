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

package org.eclipse.team.svn.resource.ignore.rules.jdt;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.team.svn.core.extension.options.IIgnoreRecommendations;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Provides JDT ignore recommendations
 * 
 * @author Alexander Gurov
 */
public class JDTIgnoreRecommendations implements IIgnoreRecommendations {
	public boolean isAcceptableNature(IResource resource) throws CoreException {
		return FileUtility.hasNature(resource, "org.eclipse.jdt.core.javanature");
	}

	public boolean isIgnoreRecommended(IResource resource) throws CoreException {
		return this.isOutput(resource);
	}

	public boolean isOutput(IResource resource) throws CoreException {
		IProject project = resource.getProject();
		if (project == null) {
			return false;
		}
		IJavaProject javaProject = JavaCore.create(project);
		IPath output = javaProject.getOutputLocation();
		// if this resource not in the output folder or the project itself is the output folder then no need to ignore it
		if (!output.isPrefixOf(resource.getFullPath()) || output.equals(project.getFullPath())) {
			return false;
		}
		if (!"bin".equals(output.lastSegment())) { //default folder name used in eclipse while creating project with separate source and binary folders
			IOpenable openable = javaProject.getOpenable();
			if (openable.isOpen()) { // do not start any time consuming process
				IPackageFragmentRoot []roots = JavaCore.create(project).getPackageFragmentRoots();
				for (int i = 0; i < roots.length; i++) {
					if (output.isPrefixOf(roots[i].getPath())) {
						return false;
					}
				}
			}
		}
		return true;
	}

}
