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

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * SVNUtility test
 * 
 * @author Alexander Gurov
 */
public abstract class SVNUtilityTest extends AbstractOperationTestCase {
	protected IActionOperation getOperation() {
		return new AbstractLockingTestOperation("SVN Utility") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
			    SVNRemoteStorage storage = SVNRemoteStorage.instance();
			    IProject prj1 = SVNUtilityTest.this.getFirstProject();
			    IProject prj2 = SVNUtilityTest.this.getSecondProject();			    
			    
			    String url = "http://test/location/url";
			    String urlEncoded = SVNUtility.encodeURL(url);
			    String decodedUrl = SVNUtility.decodeURL(urlEncoded);
			    assertTrue("SVNUtility.decodeURL", decodedUrl.equals(url));
			    
			    url = "http://test//location//url";
			    url = SVNUtility.normalizeURL(url);
			    assertTrue ("SVNUtility.normalizeURL", url.equals("http://test/location/url"));
			    
			    assertTrue("SVNUtility.isValidRepositoryLocation", SVNUtility.validateRepositoryLocation(SVNRemoteStorage.instance().getRepositoryLocations()[0]) == null);
				
				assertTrue("SVNUtility.getConnectedToSVNInfo", SVNUtility.getSVNInfoForNotConnected(prj1) != null);
				assertTrue("SVNUtility.getConnectedToSVNInfo", SVNUtility.getSVNInfoForNotConnected(prj2) != null);
				
				assertFalse("SVNUtility.isIgnored", SVNUtility.isIgnored(prj1));
				assertFalse("SVNUtility.isIgnored", SVNUtility.isIgnored(prj2));
								 
				IRepositoryResource remote1 = SVNRemoteStorage.instance().asRepositoryResource(prj1);
				IRepositoryResource remote2 = SVNRemoteStorage.instance().asRepositoryResource(prj2);
				IRepositoryResource []remoteProjectSet = new IRepositoryResource[] {remote1, remote2};				
				assertTrue("SVNUtility.asURLArray", (SVNUtility.asURLArray(remoteProjectSet, false)).length == remoteProjectSet.length);
				
				IResource local1 = prj1;
				IResource local2 = prj2;
				IResource []localProjectSet = new IResource[] {local1, local2};				
				assertTrue("SVNUtility.splitWorkingCopies", SVNUtility.splitWorkingCopies(localProjectSet).size() == localProjectSet.length);
				
				Map<?, ?> repositoryLocations = SVNUtility.splitRepositoryLocations(remoteProjectSet);
				assertTrue("SVNUtility.splitRepositoryLocations", repositoryLocations.size() == 1 && repositoryLocations.containsKey(remote1.getRepositoryLocation()));
				
				Map<?, ?> locations = SVNUtility.splitRepositoryLocations(localProjectSet);
				assertTrue("SVNUtility.splitRepositoryLocations", locations.size() == 1 && locations.containsKey(SVNRemoteStorage.instance().asRepositoryResource(local1).getRepositoryLocation()));
				
				IRepositoryResource []remoteProjectParents = SVNUtility.getCommonParents(remoteProjectSet);
				assertTrue("SVNUtility.getCommonParents", remoteProjectParents[0].equals(remote1.getParent()) && remoteProjectParents.length == 1);
				
				IRepositoryResource remoteFile1 = storage.asRepositoryResource(prj1.getFile("src/org/eclipse/team/svn/test/core/AddOperationTest.java"));
				IRepositoryResource remoteFile2 = storage.asRepositoryResource(prj1.getFile("src/org/eclipse/team/svn/test/core/CheckoutOperationTest.java"));
				IRepositoryResource remoteFolder = storage.asRepositoryResource(prj1.getFolder("src/org/eclipse/team/svn/test/core"));				
				IRepositoryResource [] shrinked = SVNUtility.shrinkChildNodes(new IRepositoryResource[] {remoteFile1, remoteFile2, remoteFolder});
				assertTrue("SVNUtility.shrinkChildNodes", shrinked.length == 1 && shrinked[0] == remoteFolder);					
			}
		};
	}
	
}
