package wtfplugin.actions;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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
import wtfplugin.console.ConsoleWriter;


public class RestoreDBFromRC extends ActionDelegate implements IWorkbenchWindowActionDelegate {

	public RestoreDBFromRC() {
	}
	

	/**
	 * @see ActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		Shell shell = workbench.getActiveWorkbenchWindow().getShell();
		final String value = Activator.getDBSchema();
		if (value != null) {
			try {
				new wtfplugin.console.ConsoleWriter().write("Haciendo dump de base de datos de RC...");
			} catch (IOException e1) {}
			Job job = new Job("Restaurar base"){ //$NON-NLS-1$
				protected IStatus run(IProgressMonitor monitor) {
					try {
						String dumpName = System.getProperty("user.home") + "/bk_rc.sql";
						// borro dump viejo
						FileUtils.deleteQuietly(new File(dumpName));
						// genero dump
						String[] cmds = { "/bin/sh", "-c", "mysqldump corporate -h"+Configuration.RC_DATABASE+" -u"+Configuration.DBUSER+" -p"+Configuration.DBPASSWORD+" > \"" +dumpName+"\"" };
						ExecuteScriptsFromSelection.runAndLogToConsole(cmds);
						//dropeo database
						try {
							new wtfplugin.console.ConsoleWriter().write("Dropeando base de datos de usuario...");
						} catch (IOException e1) {}
						String[] dropcmds = { "/bin/sh", "-c", "mysql -h"+Configuration.DATABASE+" -u"+Configuration.DBUSER+" -p"+Configuration.DBPASSWORD+" -e\"drop database "+value+"\"" };
						ExecuteScriptsFromSelection.runAndLogToConsole(dropcmds);
						// creo database
						try {
							new wtfplugin.console.ConsoleWriter().write("Creando base de datos de usuario...");
						} catch (IOException e1) {}
						String[] createcmds = { "/bin/sh", "-c", "mysql -h"+Configuration.DATABASE+" -u"+Configuration.DBUSER+" -p"+Configuration.DBPASSWORD+" -e\"create database "+value+"\"" };
						ExecuteScriptsFromSelection.runAndLogToConsole(createcmds);
						// importo dump
						try {
							new wtfplugin.console.ConsoleWriter().write("Importando dump de base de datos...");
						} catch (IOException e1) {}
						String[] importcmds = { "/bin/sh", "-c", "mysql "+value+ " -h"+Configuration.DATABASE+" -u"+Configuration.DBUSER+" -p"+Configuration.DBPASSWORD+" -v < \"" +dumpName+"\"" };
						ExecuteScriptsFromSelection.runAndLogToConsole(importcmds);
						// creo database
						try {
							new wtfplugin.console.ConsoleWriter().write("Cambiando passwords...");
						} catch (IOException e1) {}
						String[] passwordscmds = { "/bin/sh", "-c", "mysql "+value+ " -h"+Configuration.DATABASE+" -u"+Configuration.DBUSER+" -p"+Configuration.DBPASSWORD+" -e\"update employee set password = '26ca23e9d7e861e00803bcf927e5ec03';commit;\"" };
						ExecuteScriptsFromSelection.runAndLogToConsole(passwordscmds );
						// alerta de posibilidad de tener que correr scripts
						new ConsoleWriter().append("Base restaurada, verifique si debe correr scripts");
					} catch (IOException e) {
						Activator.showException(e);
					}
					return Status.OK_STATUS;
				
				}}; 
			//job.setSystem(true);
			job.schedule();
		}
		
	}


	public void setActiveEditor(IAction action, IEditorPart targetEditor) {

	}

	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub

	}
	

}
