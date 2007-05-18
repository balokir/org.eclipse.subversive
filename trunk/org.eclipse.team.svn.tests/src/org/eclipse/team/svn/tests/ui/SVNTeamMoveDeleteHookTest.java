/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elena Matokhina - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.tests.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.svn.core.SVNTeamMoveDeleteHook;
import org.eclipse.team.svn.core.operation.local.AddToSVNOperation;
import org.eclipse.team.svn.core.operation.local.CommitOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.tests.TestPlugin;
import org.eclipse.team.svn.tests.core.AddOperationTest;
import org.eclipse.team.svn.tests.core.CommitOperationTest;
import org.eclipse.team.svn.tests.core.ShareNewProjectOperationTest;
import org.eclipse.team.svn.tests.core.TestWorkflow;

/**
 * Test for SVNTeamMoveDeleteHook operations for the different cases
 *
 * @author Elena Matokhina
 */ 
public class SVNTeamMoveDeleteHookTest extends TestWorkflow {
    
    public void setUp() throws Exception {
        super.setUp();
        new ShareNewProjectOperationTest() {}.testOperation();
        new AddOperationTest() {}.testOperation();
        new CommitOperationTest() {}.testOperation();
    }
    
    public void testDeleteFile() throws Exception {
        //Deleting commited file
        SVNTeamMoveDeleteHook hook = new SVNTeamMoveDeleteHook(); 
        IFile forDeleteCommited = this.getFirstProject().getFile("maven.xml");
        assertTrue(forDeleteCommited.exists());        
        hook.deleteFile(null, forDeleteCommited, IResource.FORCE, new NullProgressMonitor()); 
        assertFalse(forDeleteCommited.exists());    
        
        //Deleting unversioned file
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(this.getFirstProject().getLocation().toString() + "/testFile");
            fos.write("contents".getBytes());
        }
        finally {
            fos.close();
        }
        IFile forDeleteUnversioned = this.getFirstProject().getFile("testFile");
        assertFalse(hook.deleteFile(null, forDeleteUnversioned, 0, new NullProgressMonitor()));               
    }
    
    public void testDeleteFolder() throws Exception {        
        //Deleting commited folder
        IFolder forDeleteCommited = this.getSecondProject().getFolder("web");
        assertTrue(forDeleteCommited.exists());
        SVNTeamMoveDeleteHook hook = new SVNTeamMoveDeleteHook(); 
        new RefreshResourcesOperation(new IResource[] {this.getSecondProject().getFolder("web"), this.getSecondProject().getFile("web/site.css"), this.getSecondProject().getFile("web/site.xsl")}).run(new NullProgressMonitor());
        hook.deleteFolder(null, forDeleteCommited, IResource.FORCE, new NullProgressMonitor()); 
        assertFalse(this.getSecondProject().getFile("web/site.css").exists());   
        
        //Deleting unversioned folder        
        File newFolder = new File(this.getFirstProject().getLocation().toString() + "/testFolder");
        newFolder.mkdir();          
        IFolder forDeleteUnversioned = this.getFirstProject().getFolder("testFolder");            
        assertFalse(hook.deleteFolder(null, forDeleteUnversioned, 0, new NullProgressMonitor()));
    }
    
    public void testMoveFile() throws Exception {
        //Moving commited file to the commited destination
        IFile source = this.getFirstProject().getFile("maven.xml");
        IFile destination = this.getFirstProject().getFile("src/maven.xml");        
        assertTrue(source.exists());
        SVNTeamMoveDeleteHook hook = new SVNTeamMoveDeleteHook(); 
        hook.moveFile(null, source, destination,IResource.FORCE, new NullProgressMonitor()); 
        assertFalse(source.exists());
        assertTrue(destination.exists());
        
        //Moving commited file to the unversioned destination
        IFile source2 = this.getSecondProject().getFile("site.xml");
        File newFolder = new File(this.getFirstProject().getLocation().toString() + "/testFolder");
        newFolder.mkdirs();
        IFile destination2 = this.getFirstProject().getFile("testFolder/site.xml"); 
        hook.moveFile(null, source2, destination2,IResource.FORCE, new NullProgressMonitor());
        assertFalse(source2.exists());
        assertTrue(destination2.exists());
        
        //Moving unversioned file to the unversioned destination
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(this.getSecondProject().getLocation().toString() + "/testFile.txt");
            fos.write("contents".getBytes());
        }
        finally {
            fos.close();
        }
        IFile source3 = this.getSecondProject().getFile("testFile.txt");
        File newFolder2 = new File(this.getSecondProject().getLocation().toString() + "/testFolder2");
        newFolder2.mkdir();          
        IFile destination3 = this.getSecondProject().getFile("testFolder2/testFile.txt"); 
        assertFalse(hook.moveFile(null, source3, destination3,IResource.FORCE, new NullProgressMonitor())); 
        assertFalse(destination3.exists());
        
        //Moving unversioned file to the commited destination
        assertFalse(hook.moveFile(null, source3, this.getSecondProject().getFile("web/testFile.txt"), IResource.FORCE, new NullProgressMonitor()));        
    }
    
    public void testMoveFolder() throws Exception {
        //Moving commited folder to the commited destination
        File sourceFolder = new File(this.getFirstProject().getLocation().toString() + "/commitedFolder");
        sourceFolder.mkdir();
        IFolder []commitedFolder = new IFolder[] {this.getFirstProject().getFolder("commitedFolder")};
        new AddToSVNOperation(commitedFolder).run(new NullProgressMonitor());
        new CommitOperation(commitedFolder, "", false).run(new NullProgressMonitor());
        SVNRemoteStorage.instance().refreshLocalResources(commitedFolder, IResource.DEPTH_INFINITE);
        commitedFolder[0].refreshLocal(IResource.DEPTH_INFINITE, null);
        IFolder destination = this.getFirstProject().getFolder("src/testFolder");        
        SVNTeamMoveDeleteHook hook = new SVNTeamMoveDeleteHook(); 
        IFolder source = commitedFolder[0];
        assertTrue(hook.moveFolder(null, source, destination, IResource.FORCE, new NullProgressMonitor())); 
        assertTrue(destination.exists());
        
        //Moving commited folder to the unversioned destination
        sourceFolder = new File(this.getFirstProject().getLocation().toString() + "/commitedFolder2");
        sourceFolder.mkdir();
        commitedFolder = new IFolder[] {this.getFirstProject().getFolder("commitedFolder2")};
        new AddToSVNOperation(commitedFolder).run(new NullProgressMonitor());
        new CommitOperation(commitedFolder, "", false).run(new NullProgressMonitor());
        SVNRemoteStorage.instance().refreshLocalResources(commitedFolder, IResource.DEPTH_INFINITE);
        commitedFolder[0].refreshLocal(IResource.DEPTH_INFINITE, null);
        File unversionedFolder = new File(this.getFirstProject().getLocation().toString() + "/destinationFolder");
        unversionedFolder.mkdir();          
        destination = this.getSecondProject().getFolder("destinationFolder");
        source = commitedFolder[0];
        assertTrue(hook.moveFolder(null, source, destination,IResource.FORCE, new NullProgressMonitor())); 
        assertTrue(destination.exists());
        
        //Moving unversioned folder to the unversioned destination
        sourceFolder = new File(this.getFirstProject().getLocation().toString() + "/unversionedSourceFolder");
        sourceFolder.mkdir(); 
        source = this.getFirstProject().getFolder("unversionedSourceFolder");
        unversionedFolder = new File(this.getFirstProject().getLocation().toString() + "/destinationFolder2");
        unversionedFolder.mkdir();
        destination = this.getSecondProject().getFolder("destinationFolder2");        
        assertFalse(hook.moveFolder(null, source, destination, IResource.FORCE, new NullProgressMonitor()));       
        
        //Moving unversioned folder to the commited destination
        assertFalse(hook.moveFolder(null, source, commitedFolder[0], IResource.FORCE, new NullProgressMonitor()));       
    }
    
	protected IProject getFirstProject() {
		ResourceBundle bundle = TestPlugin.instance().getResourceBundle();
		return ResourcesPlugin.getWorkspace().getRoot().getProject(bundle.getString("Project1.Name"));
	}
	
	protected IProject getSecondProject() {
		ResourceBundle bundle = TestPlugin.instance().getResourceBundle();
		return ResourcesPlugin.getWorkspace().getRoot().getProject(bundle.getString("Project2.Name"));
	}
	
    protected IRepositoryLocation getLocation() {
		return SVNRemoteStorage.instance().getRepositoryLocations()[0];
	}    

}
