package subverivecommitintegration;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IRevisionProvider;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.event.IResourceSelectionChangeListener;
import org.eclipse.team.svn.ui.extension.factory.ICommentDialogPanel;
import org.eclipse.team.svn.ui.extension.factory.ICommentManager;
import org.eclipse.team.svn.ui.extension.factory.ICommitActionFactory;
import org.eclipse.team.svn.ui.extension.factory.ICommitDialog;
import org.eclipse.team.svn.ui.panel.IDialogManager;
import org.eclipse.ui.IWorkbenchPart;

public class CommitActionFactory1 implements ICommitActionFactory {

	public CommitActionFactory1() {
	}

	public ICommitDialog getCommitDialog(final Shell shell, Collection allFilesToCommit, final ICommentDialogPanel panel) {
		// The task we implement is as such: we prevent a commit from happening until the end of working hours
		// We do not want to just hide the commit dialog from committer. We want to tell the reason for preventing the commit.
		// And that is why we need to set a specific validation message and status in time of dialog initialization.
		// Please note, that you can't do so prior to initialization is complete. For that reason we wrap the ICommentDialogPanel panel
		// and handle the postInit() event.
		return new ICommitDialog(){
			public String getMessage() {
				return panel.getMessage();
			}
			
			public int open() {
				ICommentDialogPanel panelWrap = new ICommentDialogPanel() {
					private IDialogManager manager;
					public void postInit() {
						panel.postInit();
						
						Calendar c = Calendar.getInstance();
						Date date = c.getTime();
						c.set(Calendar.HOUR_OF_DAY, 17);
						c.set(Calendar.MINUTE, 0);
						c.set(Calendar.SECOND, 0);
						c.set(Calendar.MILLISECOND, 0);
						Date limitation = c.getTime();
						
						if (limitation.after(date)) {
							manager.setMessage(IDialogManager.LEVEL_ERROR, "It is not the time to commit yet! Please wait until work hours are over.");
							manager.setButtonEnabled(0, false);
						}
					}
					public void initPanel(IDialogManager manager) {
						panel.initPanel(manager);
						this.manager = manager;
					}
					public Point getPrefferedSize() {
						return panel.getPrefferedSize();
					}
					public String getImagePath() {
						return panel.getImagePath();
					}
					public String getHelpId() {
						return panel.getHelpId();
					}
					public String getDialogTitle() {
						return panel.getDialogTitle();
					}
					public String getDialogDescription() {
						return panel.getDialogDescription();
					}
					public String getDefaultMessage() {
						return panel.getDefaultMessage();
					}
					public String[] getButtonNames() {
						return panel.getButtonNames();
					}
					public void dispose() {
						panel.dispose();
					}
					public void createControls(Composite parent) {
						panel.createControls(parent);
					}
					public boolean canClose() {
						return panel.canClose();
					}
					public void buttonPressed(int idx) {
						panel.buttonPressed(idx);
					}
					public void addListeners() {
						panel.addListeners();
					}
					public void removeResourcesSelectionChangedListener(IResourceSelectionChangeListener listener) {
						panel.removeResourcesSelectionChangedListener(listener);
					}
					public String getMessage() {
						return panel.getMessage();
					}
					public void addResourcesSelectionChangedListener(IResourceSelectionChangeListener listener) {
						panel.addResourcesSelectionChangedListener(listener);
					}
				};
				
				DefaultDialog dialog = new DefaultDialog(shell, panelWrap);
				return dialog.open();
			}
		};
	}

	public void performAfterCommitTasks(CompositeOperation operation, IRevisionProvider revisionProvider, IActionOperation[] dependsOn, IWorkbenchPart part) {
		
	}

	public void initCommentManager(ICommentManager commentManager) {
		commentManager.addCommentsToSection(ICommentManager.TEMPLATE_HEADER, Arrays.asList(new String[] {"Template comment #1 using integration API"}));
		String newSection = "------------------------------ New Section ------------------------------";
		commentManager.addCommentsSection(newSection, "Added using integration API");
		commentManager.addCommentsToSection(newSection, Arrays.asList(new String[] {"Template comment #2 using integration API"}));
	}

	public void confirmMessage(ICommentManager commentManager) {
		
	}

	public void cancelMessage(ICommentManager commentManager) {
		
	}

}
