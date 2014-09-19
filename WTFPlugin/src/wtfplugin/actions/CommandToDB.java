package wtfplugin.actions;

import java.io.IOException;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;

import wtfplugin.Activator;
import wtfplugin.Configuration;


public class CommandToDB extends ActionDelegate implements IWorkbenchWindowActionDelegate {

	public CommandToDB() {
	}
	

	/**
	 * @see ActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		Shell shell = workbench.getActiveWorkbenchWindow().getShell();
		String value = Activator.getDBSchema();
		if (value != null) {
			try {
//				String[] command = { "xterm", "-e", "mysql", "-u"+Configuration.DBUSER, "-p"+Configuration.DBPASSWORD, "-h"+Configuration.DATABASE, value };
				String[] command = { "gnome-terminal", "-x",  "sh", "-c", "mysql -u"+Configuration.DBUSER+ " -p"+Configuration.DBPASSWORD + " -h"+Configuration.DATABASE + " " + value , "&"};
				Runtime.getRuntime().exec(command);
				
				//Runtime.getRuntime().exec("/bin/bash -c mysql -uroot -proot -hlocalhost "/* + value*/);
			} catch (IOException e) {
				Activator.showException(e);
			}
		}
		
	}


	public void setActiveEditor(IAction action, IEditorPart targetEditor) {

	}

	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) throws IOException {
		String[] command = { "gnome-terminal", "-x",  "sh", "-c", "mysql -u"+Configuration.DBUSER+ " -p"+Configuration.DBPASSWORD + " -h"+Configuration.DATABASE + " corp_mgodoy" , "&"};
		Runtime.getRuntime().exec(command);
	}

}
