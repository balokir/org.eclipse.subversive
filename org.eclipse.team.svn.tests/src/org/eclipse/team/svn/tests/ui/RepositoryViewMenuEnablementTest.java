/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.tests.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.client.SVNRevision;
import org.eclipse.team.svn.core.operation.local.AddToSVNOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.tests.TestPlugin;
import org.eclipse.team.svn.tests.core.AddOperationTest;
import org.eclipse.team.svn.tests.core.CommitOperationTest;
import org.eclipse.team.svn.tests.core.ShareNewProjectOperationTest;
import org.eclipse.team.svn.tests.core.TestWorkflow;
import org.eclipse.team.svn.ui.RemoteResourceTransferrable;
import org.eclipse.team.svn.ui.action.local.management.CleanupAction;
import org.eclipse.team.svn.ui.action.remote.BranchAction;
import org.eclipse.team.svn.ui.action.remote.CompareAction;
import org.eclipse.team.svn.ui.action.remote.CopyAction;
import org.eclipse.team.svn.ui.action.remote.CreateFolderAction;
import org.eclipse.team.svn.ui.action.remote.CutAction;
import org.eclipse.team.svn.ui.action.remote.DeleteAction;
import org.eclipse.team.svn.ui.action.remote.PasteAction;
import org.eclipse.team.svn.ui.action.remote.RefreshAction;
import org.eclipse.team.svn.ui.action.remote.RenameAction;
import org.eclipse.team.svn.ui.action.remote.ShowAnnotationAction;
import org.eclipse.team.svn.ui.action.remote.ShowHistoryAction;
import org.eclipse.team.svn.ui.action.remote.TagAction;
import org.eclipse.team.svn.ui.action.remote.management.CreateProjectStructureAction;
import org.eclipse.team.svn.ui.action.remote.management.EditRepositoryLocationPropertiesAction;
import org.eclipse.team.svn.ui.operation.PrepareRemoteResourcesTransferrableOperation;
import org.eclipse.team.svn.ui.repository.model.RepositoryFile;
import org.eclipse.team.svn.ui.repository.model.RepositoryFolder;
import org.eclipse.team.svn.ui.repository.model.RepositoryLocation;
import org.eclipse.team.svn.ui.repository.model.RepositoryResource;

/**
 * Menu enablement test for the Subversive menus in Repository View
 *
 * @author Sergiy Logvin
 */
public class RepositoryViewMenuEnablementTest extends TestWorkflow {
    public void setUp() throws Exception {
        super.setUp();
        
        new ShareNewProjectOperationTest() {}.testOperation();
        new AddOperationTest() {}.testOperation();
        new CommitOperationTest() {}.testOperation();
        File newFolder = new File(this.getFirstProject().getLocation().toString() + "/testFolder");
        newFolder.mkdir(); 
        newFolder = new File(this.getSecondProject().getLocation().toString() + "/testFolder");
        newFolder.mkdir();
		IResource []projects = new IResource[] {this.getFirstProject(), this.getSecondProject()};
        new RefreshResourcesOperation(projects).run(new NullProgressMonitor());
        new AddToSVNOperation(new IResource[] {getSecondProject().getFolder("testFolder")}).run(new NullProgressMonitor());
    }

    public void testPasteRemoteResourceAction() {
		RepositoryResource []resources = this.getTwoRepositoryFiles();
        new PrepareRemoteResourcesTransferrableOperation(
                new IRepositoryResource[] {resources[0].getRepositoryResource(), resources[1].getRepositoryResource()},
                RemoteResourceTransferrable.OP_COPY,
                TestPlugin.instance().getWorkbench().getDisplay()
        ).run(new NullProgressMonitor());
        IActionDelegate action = (IActionDelegate) new PasteAction();
        this.assertEnablement(action, this.getAllRepositoryResources(), false);
        this.assertEnablement(action, this.getOneRepositoryContainer(), true);
        this.assertEnablement(action, this.getNotHeadRevisionFiles(), false);
        this.assertEnablement(action, new RepositoryResource[] {this.getNotHeadRevisionFiles()[0]}, false);
    }
    
    public void testBranchRemoteAction() {
        IActionDelegate action = (IActionDelegate) new BranchAction();
        this.assertEnablement(action, this.getAllRepositoryResources(), true);
        this.assertEnablement(action, new RepositoryResource[] {this.getAllRepositoryResources()[0]}, true);        
    }
    
    public void testTagRemoteAction() {
        IActionDelegate action = (IActionDelegate) new TagAction();
        this.assertEnablement(action, this.getAllRepositoryResources(), true);
        this.assertEnablement(action, new RepositoryResource[] {this.getAllRepositoryResources()[0]}, true);        
    }
    
    public void testCleanupAction() {
        IActionDelegate action = (IActionDelegate) new CleanupAction();
        this.assertEnablement(action, this.getSelectedProjects(), true);
        this.assertEnablement(action, new IResource[] {this.getSelectedProjects()[0]}, true);        
    }
    
    public void testCompareTwoRepositoryResourcesAction() {
        IActionDelegate action = (IActionDelegate) new CompareAction();
        this.assertEnablement(action, new IResource[] {this.getSelectedProjects()[0]}, false);
        this.assertEnablement(action, this.getOneRepositoryContainer(), true);
        this.assertEnablement(action, this.getOneRepositoryFile(), true);
        this.assertEnablement(action, this.getAllRepositoryResources(), false);
    }
    
    public void testCopyRemoteResourceAction() {
        IActionDelegate action = (IActionDelegate) new CopyAction();
        this.assertEnablement(action, this.getTwoRepositoryContainers(), true);
        this.assertEnablement(action, this.getAllRepositoryResources(), true);
        this.assertEnablement(action, new RepositoryResource[] {this.getAllRepositoryResources()[0]}, true);
    }
    
    public void testCreateProjectStructureAction() {
        IActionDelegate action = (IActionDelegate) new CreateProjectStructureAction();
        this.assertEnablement(action, this.getOneRepositoryContainer(), true);
        this.assertEnablement(action, this.getRepositoryLocation(), true);
    }
    
    public void testCreateRemoteFolderAction() {
        IActionDelegate action = (IActionDelegate) new CreateFolderAction();
        this.assertEnablement(action, this.getTwoRepositoryContainers(), false);
        this.assertEnablement(action, new RepositoryResource[] {this.getTwoRepositoryContainers()[0]}, true);
        this.assertEnablement(action, new RepositoryResource[] {this.getNotHeadRevisionFiles()[0]}, false);        
    }
    
    public void testCutRemoteResourceAction() {
        IActionDelegate action = (IActionDelegate) new CutAction();
        this.assertEnablement(action, this.getTwoRepositoryContainers(), true);
        this.assertEnablement(action, this.getAllRepositoryResources(), true);
        this.assertEnablement(action, this.getNotHeadRevisionFiles(), false);
        this.assertEnablement(action, new RepositoryResource[] {this.getAllRepositoryResources()[0]}, true);
        this.assertEnablement(action, this.getRepositoryLocation(), false);
        this.assertEnablement(action, this.getRepositoryRoots(), false);
    }
    
    public void testDeleteRemoteResourceAction() {
        IActionDelegate action = (IActionDelegate) new DeleteAction();
        this.assertEnablement(action, this.getTwoRepositoryContainers(), true);
        this.assertEnablement(action, this.getAllRepositoryResources(), true);
        this.assertEnablement(action, new RepositoryResource[] {this.getNotHeadRevisionFiles()[0]}, false);
        this.assertEnablement(action, new RepositoryResource[] {this.getAllRepositoryResources()[0]}, true);
        this.assertEnablement(action, this.getRepositoryLocation(), false);
        this.assertEnablement(action, this.getRepositoryRoots(), false);        
    }
    
    public void testEditRepositoryLocationPropertiesAction() {
        IActionDelegate action = (IActionDelegate) new EditRepositoryLocationPropertiesAction();
        this.assertEnablement(action, this.getTwoRepositoryContainers(), false);
        this.assertEnablement(action, this.getRepositoryLocation(), true);
        this.assertEnablement(action, new RepositoryResource[] {this.getAllRepositoryResources()[0]}, false);
    }
    
    public void testRefreshRemoteAction() {
        IActionDelegate action = (IActionDelegate) new RefreshAction();
        this.assertEnablement(action, this.getTwoRepositoryContainers(), true);
        this.assertEnablement(action, this.getNotHeadRevisionFiles(), true);
        this.assertEnablement(action, this.getRepositoryLocation(), true);
        this.assertEnablement(action, new RepositoryResource[] {this.getAllRepositoryResources()[0]}, true);
    }
    
    public void testRenameRemoteResourceAction() {
        IActionDelegate action = (IActionDelegate) new RenameAction();
        this.assertEnablement(action, this.getTwoRepositoryContainers(), false);
        this.assertEnablement(action, this.getAllRepositoryResources(), false);
        this.assertEnablement(action, new RepositoryResource[] {this.getNotHeadRevisionFiles()[0]}, false);
        this.assertEnablement(action, new RepositoryResource[] {this.getAllRepositoryResources()[0]}, true);
        this.assertEnablement(action, this.getRepositoryLocation(), false);
        this.assertEnablement(action, this.getRepositoryRoots(), false);
    }
    
    public void testShowRemoteAnnotationAction() {
        IActionDelegate action = (IActionDelegate) new ShowAnnotationAction();
        this.assertEnablement(action, this.getAllRepositoryResources(), false);
        this.assertEnablement(action, new RepositoryResource[] {this.getAllRepositoryResources()[0]}, true);        
    }

    public void testShowRemoteResourceHistoryAction() {
        IActionDelegate action = (IActionDelegate) new ShowHistoryAction();
        this.assertEnablement(action, this.getAllRepositoryResources(), false);
        this.assertEnablement(action, new RepositoryResource[] {this.getAllRepositoryResources()[0]}, true);        
    } 
    
    protected void assertEnablement(IActionDelegate actionDelegate, RepositoryResource []resources, boolean expectedEnablement) {
		IAction action = new Action() {};
		ISelection selection = this.asSelection(resources);
		actionDelegate.selectionChanged(action, selection);
		assertEquals(this.getName(actionDelegate) + " enablement wrong!", expectedEnablement, action.isEnabled());
	}
    
    protected void assertEnablement(IActionDelegate actionDelegate, IResource []resources, boolean expectedEnablement) {
		IAction action = new Action() {};
		ISelection selection = this.asSelection(resources);
		actionDelegate.selectionChanged(action, selection);
		assertEquals(this.getName(actionDelegate) + " enablement wrong!", expectedEnablement, action.isEnabled());
	}
	
	protected void assertEnablement(IActionDelegate actionDelegate, RepositoryLocation []locations, boolean expectedEnablement) {
		IAction action = new Action() {};
		ISelection selection = this.asSelection(locations);
		actionDelegate.selectionChanged(action, selection);
		assertEquals(this.getName(actionDelegate) + " enablement wrong!", expectedEnablement, action.isEnabled());
	}
		    
    protected ISelection asSelection(Object[] resources) {
		return new StructuredSelection(resources);
	}
    
    protected String getName(IActionDelegate actionDelegate) {
		return actionDelegate.getClass().getName();
	}
    
    protected RepositoryLocation []getRepositoryLocation() {    
    	return new RepositoryLocation[] {new RepositoryLocation(this.getAllRepositoryResources()[0].getRepositoryResource().getRepositoryLocation())}; 
	}
    
    protected RepositoryResource []getAllRepositoryResources() {
        SVNRemoteStorage storage = SVNRemoteStorage.instance();
        List remoteResources = new ArrayList();
        IResource[] resources = FileUtility.getResourcesRecursive(new IResource[] {this.getFirstProject(), this.getSecondProject()}, IStateFilter.SF_ONREPOSITORY);
        for (int i = 0; i < resources.length; i++) {
            remoteResources.add(RepositoryFolder.wrapChild(null, storage.asRepositoryResource(resources[i])));
        }
        return (RepositoryResource []) remoteResources.toArray(new RepositoryResource[remoteResources.size()]);
    }
    
    protected RepositoryResource []getOneRepositoryFile() {
    	return new RepositoryResource[] {this.getTwoRepositoryFiles()[0]};
    }
    
    protected RepositoryResource []getTwoRepositoryFiles() {
        List twoRemoteFiles = new ArrayList();
        RepositoryResource []resources = this.getAllRepositoryResources();
        for (int i = 0; i < resources.length; i++) {
            if (resources[i] instanceof RepositoryFile) {
                twoRemoteFiles.add(resources[i]);
                if (twoRemoteFiles.size() == 2) {
                    return (RepositoryResource [])twoRemoteFiles.toArray(new RepositoryResource[twoRemoteFiles.size()]);
                }
            }            
        }
        return (RepositoryResource []) twoRemoteFiles.toArray(new RepositoryResource[twoRemoteFiles.size()]);
    }
    
    protected RepositoryResource []getNotHeadRevisionFiles() {
        List twoRemoteFiles = new ArrayList();
        RepositoryResource []resources = this.getAllRepositoryResources();
        for(int i = 0; i < resources.length; i++) {
            if (resources[i] instanceof RepositoryFile) {
                resources[i].getRepositoryResource().setSelectedRevision(SVNRevision.fromNumber(123));
                twoRemoteFiles.add(resources[i]);
                if (twoRemoteFiles.size() == 2) {
                    return (RepositoryResource [])twoRemoteFiles.toArray(new RepositoryResource[twoRemoteFiles.size()]);
                }
            }            
        }
        return (RepositoryResource [])twoRemoteFiles.toArray(new RepositoryResource[twoRemoteFiles.size()]);
    }
    
    protected RepositoryResource []getOneRepositoryContainer() {
    	return new RepositoryResource[] {this.getTwoRepositoryContainers()[0]};
    }
    
    protected RepositoryResource[] getTwoRepositoryContainers() {
        List twoRemoteFolders = new ArrayList();
        RepositoryResource []resources = this.getAllRepositoryResources();
        for(int i = 0; i < resources.length; i++) {
            if (resources[i] instanceof RepositoryFolder) {
                twoRemoteFolders.add(resources[i]);
                if (twoRemoteFolders.size() == 2) {
                    return (RepositoryResource [])twoRemoteFolders.toArray(new RepositoryResource[twoRemoteFolders.size()]);
                }
            }            
        }
        return (RepositoryResource [])twoRemoteFolders.toArray(new RepositoryResource[twoRemoteFolders.size()]);
    }
    
    protected IResource[] getSelectedProjects() {
        IResource[] selectedResources = FileUtility.getResourcesRecursive(new IResource[] {this.getFirstProject(), this.getSecondProject()}, IStateFilter.SF_ONREPOSITORY);;
		ArrayList projects = new ArrayList();
		for (int i = 0; i < selectedResources.length; i++) {
		    IResource resource = selectedResources[i];
			if (resource.getType() == IResource.PROJECT) {
				projects.add(resource);
			}
		}
		return (IResource[])projects.toArray(new IResource[projects.size()]);
	}
    
    protected RepositoryResource []getRepositoryRoots() {
        List roots = new ArrayList();
        RepositoryResource []resources = this.getAllRepositoryResources();
        for(int i = 0; i < resources.length; i++) {
            if (resources[0].getRepositoryResource() instanceof IRepositoryRoot) {
                roots.add(resources[i]);
            }
        }
        return (RepositoryResource [])roots.toArray(new RepositoryResource[roots.size()]);
	}
    
	protected IProject getFirstProject() {
		ResourceBundle bundle = TestPlugin.instance().getResourceBundle();
		return ResourcesPlugin.getWorkspace().getRoot().getProject(bundle.getString("Project1.Name"));
	}
	
	protected IProject getSecondProject() {
		ResourceBundle bundle = TestPlugin.instance().getResourceBundle();
		return ResourcesPlugin.getWorkspace().getRoot().getProject(bundle.getString("Project2.Name"));
	}
	
}
