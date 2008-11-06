/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.mapping;

import org.eclipse.team.internal.ui.synchronize.ChangeSetCapability;
import org.eclipse.team.svn.ui.synchronize.update.UpdateParticipant;

import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizeScope;

/**
 * Commit panel participant. 
 * Used by ParticipantPagePane to show resources to commit 
 * 
 * @author Igor Burilo
 * 
 * TODO
 * - make correct implementation. see CommitWizardParticipant
 */
public class CommitPanelParticipant extends UpdateParticipant {

	protected static final String ACTION_GROUP = "org.eclipse.team.svn.ui.CommitActions";
	
	//protected Action showComparePaneAction;
	
	public CommitPanelParticipant(ISynchronizeScope scope) {
		super(scope);	      
	}
	
    public ChangeSetCapability getChangeSetCapability() {
        return null; // we don't want that button
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.ui.synchronize.AbstractSynchronizeParticipant#doesSupportSynchronize()
     */
    public boolean doesSupportSynchronize() {
        return false;
    }
    
    protected void initializeConfiguration( final ISynchronizePageConfiguration configuration) {
        super.initializeConfiguration(configuration);
        configuration.setProperty(ISynchronizePageConfiguration.P_TOOLBAR_MENU, new String[] {ACTION_GROUP, ISynchronizePageConfiguration.LAYOUT_GROUP});
        configuration.setProperty(ISynchronizePageConfiguration.P_CONTEXT_MENU, ISynchronizePageConfiguration.DEFAULT_CONTEXT_MENU);
        
        
        
//		configuration.addMenuGroup(
//				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
//				CONTEXT_MENU_CONTRIBUTION_GROUP_3);
        
        
        //TODO it seems we don't need ActionContribution like in CVS
        //configuration.addActionContribution(new ActionContribution());
        
        //TODO do we need it ?
        //configuration.setRunnableContext(fWizard.getContainer());
        
//        // Wrap the container so that we can update the enablements after the runnable
//        // (i.e. the container resets the state to what it was at the beginning of the
//        // run even if the state of the page changed. Remove from View changes the state)
//        configuration.setRunnableContext(new IRunnableContext() {
//            public void run(boolean fork, boolean cancelable,
//                    IRunnableWithProgress runnable)
//                    throws InvocationTargetException, InterruptedException {
//                fWizard.getContainer().run(fork, cancelable, runnable);
//                final CommitWizardCommitPage page= fWizard.getCommitPage();
//                if (page != null)
//                    page.updateEnablements();
//            }
//        });
        
        configuration.setSupportedModes(ISynchronizePageConfiguration.OUTGOING_MODE);
        configuration.setMode(ISynchronizePageConfiguration.OUTGOING_MODE);
        
//        configuration.addActionContribution(new SynchronizePageActionGroup() {
//        	public void initialize(ISynchronizePageConfiguration configuration) {
//        		super.initialize(configuration);
//        		showComparePaneAction = new Action(null, Action.AS_CHECK_BOX) {
//        			public void run() {
//        				fWizard.getCommitPage().showComparePane(this.isChecked());
//        			}
//        		};
//        		Utils.initAction(showComparePaneAction, "ComnitWizardComparePaneToggle.", Policy.getActionBundle()); //$NON-NLS-1$
//        		showComparePaneAction.setChecked(isComparePaneVisible());
//        		appendToGroup(ISynchronizePageConfiguration.P_TOOLBAR_MENU, ACTION_GROUP, showComparePaneAction);
//        	}
//		});        
//        configuration.setProperty(SynchronizePageConfiguration.P_OPEN_ACTION, new Action() {
//			public void run() {
//				ISelection selection = configuration.getSite().getSelectionProvider().getSelection();
//				if(selection instanceof IStructuredSelection) {
//					final Object obj = ((IStructuredSelection) selection).getFirstElement();
//					if (obj instanceof SyncInfoModelElement) {
//						try {
//							fWizard.getContainer().run(true, true, new IRunnableWithProgress() {
//								public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
//									try {
//										((SyncInfoModelElement)obj).cacheContents(monitor);
//									} catch (TeamException e) {
//										throw new InvocationTargetException(e);
//									}
//							    	fWizard.getContainer().getShell().getDisplay().syncExec(new Runnable() {
//										public void run() {
//											fWizard.getCommitPage().showComparePane(true);
//											showComparePaneAction.setChecked(true);
//											fWizard.getCommitPage().setCompareInput(obj);						
//										}
//							    	});
//								}
//							});
//						} catch (InvocationTargetException e) {
//						} catch (InterruptedException e) {
//						}
//					}
//				}
//			}
//        });
    }
	 
}
