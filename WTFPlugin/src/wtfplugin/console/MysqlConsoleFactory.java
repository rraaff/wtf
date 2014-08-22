package wtfplugin.console;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class MysqlConsoleFactory implements IConsoleFactory {

	private static final String SQLPLUS_CONSOLE_VIEW = "MYSQL Console";

	private static String buffer = "";
	
	public void openConsole() {
		basicOpenConsole();
	}

	private static void basicOpenConsole() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				try {
					IConsole myConsole = findConsole(SQLPLUS_CONSOLE_VIEW);
					String id = IConsoleConstants.ID_CONSOLE_VIEW;
					IConsoleView view = (IConsoleView) page.showView(id);
					view.display(myConsole);

				} catch (PartInitException e) {
					ConsolePlugin.log(e);
				}
			}
		}
	}
	
	public static void log(String text) {
		basicOpenConsole();
		MessageConsole myConsole = findConsole(SQLPLUS_CONSOLE_VIEW);
		MessageConsoleStream out = myConsole.newMessageStream();
		out.println(text);
		buffer = buffer + text;
	}
	


	private static MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

}
