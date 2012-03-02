
package org.eclipse.team.svn.ui.console;

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
	private static SVNConsole console = null;

	public void openConsole() {		
		SVNConsoleFactory.showConsole();
	}
	
    public synchronized static SVNConsole getConsole() {
    	if (SVNConsoleFactory.console == null) {
    		SVNConsoleFactory.console = new SVNConsole();
    	}
    	return SVNConsoleFactory.console;
    }
	
    public synchronized static void destroyConsole() {
    	if (SVNConsoleFactory.console != null) {
    		SVNConsoleFactory.console.destroy();
    	}
    }
	
	public static void showConsole() {
		SVNConsole console = SVNConsoleFactory.getConsole();
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
