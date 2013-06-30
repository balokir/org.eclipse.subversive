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

package org.eclipse.team.svn.tests.core;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * FileUtility test
 * 
 * @author Alexander Gurov
 */
public abstract class FileUtilityTest extends AbstractOperationTestCase {

	protected IActionOperation getOperation() {
		return new AbstractLockingTestOperation("File Utility") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				String name = FileUtility.formatResourceName("http://testurl\\data");				
				IResource prj1 = FileUtilityTest.this.getFirstProject();
				IResource prj2 = FileUtilityTest.this.getSecondProject();
				assertFalse("FileUtility.formatResourceName", name.indexOf('\\') == -1 && name.indexOf('.') == -1 && name.indexOf('/') == -1);
				
				assertTrue("FileUtility.alreadyConnectedToSVN", FileUtility.alreadyOnSVN(prj1));
				assertTrue("FileUtility.alreadyConnectedToSVN", FileUtility.alreadyOnSVN(prj2));
				
				assertFalse("FileUtility.isTeamPrivateMember", FileUtility.isSVNInternals(prj1));
				assertFalse("FileUtility.isTeamPrivateMember", FileUtility.isSVNInternals(prj2));
				
				IResource []projectSet = new IResource[] {prj1, prj2};
				
				String []pathArray = FileUtility.asPathArray(projectSet);
				assertTrue("FileUtility.asPathArray", pathArray.length == projectSet.length);
				
				IResource []normal = FileUtility.getResourcesRecursive(projectSet, IStateFilter.SF_NOTMODIFIED);
				IResource []versioned = FileUtility.getResourcesRecursive(projectSet, IStateFilter.SF_VERSIONED);
				assertTrue("FileUtility.getResourcesRecursive", normal.length == versioned.length);
				
				IResource []all = FileUtility.getResourcesRecursive(projectSet, IStateFilter.SF_ALL);
				final int []cntr = new int[1];
				cntr[0] = 0;
				FileUtility.visitNodes(prj1, new IResourceVisitor() {
					public boolean visit(IResource resource) throws CoreException {
						if (FileUtility.isSVNInternals(resource)) {
							return false;
						}
						cntr[0]++;
						return true;
					}
				}, IResource.DEPTH_INFINITE);
				FileUtility.visitNodes(prj2, new IResourceVisitor() {
					public boolean visit(IResource resource) throws CoreException {
						if (FileUtility.isSVNInternals(resource)) {
							return false;
						}
						cntr[0]++;
						return true;
					}
				}, IResource.DEPTH_INFINITE);
				assertTrue("FileUtility.visitNodes != IStateFilter.SF_ALL", cntr[0] == all.length);
				
				IResource []normalFiltered = FileUtility.getResourcesRecursive(projectSet, IStateFilter.SF_NOTMODIFIED, IResource.DEPTH_ZERO);
				IResource []versionedFiltered = FileUtility.getResourcesRecursive(projectSet, IStateFilter.SF_VERSIONED, IResource.DEPTH_ZERO);
				assertTrue("FileUtility.getFilteredResources", normalFiltered.length == versionedFiltered.length);
				
				IResource child = prj1.getProject().getFile(".project");
				IResource []operableParents = FileUtility.addOperableParents(new IResource[] {child}, IStateFilter.SF_NOTMODIFIED);
				assertTrue("FileUtility.addOperableParents", operableParents[0] == prj1 && operableParents.length == 2);
				
				IResource []parents = FileUtility.getOperableParents(new IResource[] {child}, IStateFilter.SF_NOTMODIFIED);
				assertTrue("FileUtility.getOperableParents", parents[0] == prj1 && parents.length == 1);				
				
				File folderToCopy = FileUtilityTest.this.getSecondProject().getFolder("web").getLocation().toFile();
				File folderWhereToCopy = FileUtilityTest.this.getFirstProject().getFolder("src").getLocation().toFile(); 
				FileUtility.copyAll(folderWhereToCopy, folderToCopy, monitor);
				File copiedFolder = new File((folderWhereToCopy.getAbsolutePath() + "/" + folderToCopy.getName()));
				assertTrue("FileUtility.copyFolder", copiedFolder.exists());
				
				File fileToCopy = FileUtilityTest.this.getSecondProject().getFile(".project").getLocation().toFile();
				FileUtility.copyFile (folderWhereToCopy, fileToCopy, monitor);
				File copiedFile = new File ((folderWhereToCopy.getAbsolutePath() + "/" + fileToCopy.getName()));
				assertTrue ("FileUtility.copyFile", copiedFile.exists());
				
				IResource child1 = FileUtilityTest.this.getFirstProject().getFolder("src/web");
				IResource child2 = FileUtilityTest.this.getFirstProject().getFile("src/.project");
				IResource parent = FileUtilityTest.this.getFirstProject().getFolder("src");
				IResource []shrinked = FileUtility.shrinkChildNodes(new IResource[] {child1, child2, parent});
				assertTrue ("FileUtility.shrinkChildNodes", shrinked.length == 1 && shrinked[0] == parent);
				
				assertTrue ("FileUtility.deleteRecursive", FileUtility.deleteRecursive(folderToCopy));
				
				IResource []pathNodes = FileUtility.getPathNodes(child1);
				assertTrue ("FileUtility.getPathNodes", pathNodes.length == 2);
				
				pathNodes = FileUtility.getPathNodes(new IResource[] {child1, child2});
				assertTrue ("FileUtility.getPathNodes(IResourse[])", pathNodes.length == 2);
				
				FileUtility.reorder(all, true);
				assertTrue ("FileUtility.reorder", all[1].getName().equals(".classpath"));
				
				FileUtility.reorder(all, false);
				assertTrue ("FileUtility.reorder", all[0].getName().equals("site.xsl"));			
			}
		};
	}
	
}
