package wtfplugin.actions;

import java.io.IOException;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;

import wtfplugin.Activator;
import wtfplugin.Configuration;


public class CheckActualDBVersion extends ActionDelegate implements IWorkbenchWindowActionDelegate {

	public CheckActualDBVersion() {
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
				String[] cmds = { "/bin/sh", "-c", "mysql "+value+  " -h"+Configuration.DATABASE+" -u"+Configuration.DBUSER+" -p"+Configuration.DBPASSWORD+" -e\"select * from dbversion where id = (select max(id) from dbversion);\"" };
				new wtfplugin.console.ConsoleWriter().write("Version Actual");
				ExecuteScriptsFromSelection.runAndLogToConsole(cmds);
				
				new wtfplugin.console.ConsoleWriter().write("Ultimos scripts");
				String[] cmds1 = { "/bin/sh", "-c", "mysql "+value+  " -h"+Configuration.DATABASE+" -u"+Configuration.DBUSER+" -p"+Configuration.DBPASSWORD+" -e\"select * from dbscript where id_dbversion = (select max(id) from dbversion);\"" };
				ExecuteScriptsFromSelection.runAndLogToConsole(cmds1);
				
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
	

}
