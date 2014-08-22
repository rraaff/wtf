package wtfplugin.actions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import wtfplugin.Activator;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;


public class CommandToDB extends ActionDelegate implements IWorkbenchWindowActionDelegate {

	public CommandToDB() {
	}
	

	/**
	 * @see ActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		Shell shell = workbench.getActiveWorkbenchWindow().getShell();
		InputDialog inputDialog = new InputDialog(Activator.getDefault().getWorkbench().getWorkbenchWindows()[0].getShell(), "Ingreso el nombre de la base", "Ingreso el nombre de la base, por ejemplo, corp_mgodoy", "corp_" + System.getProperty("user.name"), null);
		int manual = inputDialog.open();
		if (manual == 0) {
			String value = inputDialog.getValue();
			try {
				String[] command = { "xterm", "-e", "mysql", "-uroot", "-proot", "-hlocalhost", value };
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
	

}
