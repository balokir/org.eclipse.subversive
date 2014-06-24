
package org.eclipse.team.svn.ui.console;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * Console helper that allows contributing actions to the console view when
 * the SVN console is visible. Added to the console from 
 * org.eclipse.ui.console.consolePageParticipants extension point.
 * 
 * @author Igor Burilo
 */
public class SVNConsolePageParticipant implements IConsolePageParticipant {

	private SVNConsoleRemoveAction consoleRemoveAction;
	
	public void init(IPageBookViewPage page, IConsole console) {		
		this.consoleRemoveAction = new SVNConsoleRemoveAction();
		IActionBars bars = page.getSite().getActionBars();
		bars.getToolBarManager().appendToGroup(IConsoleConstants.LAUNCH_GROUP, this.consoleRemoveAction);
	}
	
	public void dispose() {		
		this.consoleRemoveAction = null;
	}
	
	public void activated() {
	
	}

	public void deactivated() {
		
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

}
