
package org.eclipse.team.svn.ui.console;

import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;

/**
 * Console factory is used to show the console from the Console view "Open Console"
 * drop-down action. This factory is registered via the org.eclipse.ui.console.consoleFactory 
 * extension point. 
 * 
 * @author Igor Burilo
 */
public class SVNConsoleFactory implements IConsoleFactory {

	public void openConsole() {		
		SVNConsoleFactory.showConsole();
	}
	
	public static void showConsole() {
		SVNConsole console = SVNTeamUIPlugin.instance().getConsole();
		if (console != null) {
			IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
			IConsole[] existing = manager.getConsoles();
			boolean exists = false;
			for (int i = 0; i < existing.length; i++) {
				if(console == existing[i]) {
					exists = true;
				}
			}
			if(!exists) {
				manager.addConsoles(new IConsole[] {console});
			}
			manager.showConsoleView(console);
		}
	}

}
