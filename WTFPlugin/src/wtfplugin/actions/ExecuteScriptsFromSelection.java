/*
 * Created on Feb 13, 2007
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package wtfplugin.actions;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.internal.resources.File;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;

import wtfplugin.Activator;
import wtfplugin.Configuration;

/**
 * @author mgodoy
 * 
 *         To change the template for this generated type comment go to Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ExecuteScriptsFromSelection extends ActionDelegate {

	private ISelection selection;

	public static String lastUsedConteiner;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.ActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		this.selection = selection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.ActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		super.run(action);
		if (this.selection instanceof StructuredSelection) {
			String value = Activator.getDBSchema();
			if (value != null) {
				if (!StringUtils.isEmpty(value)) {
					StructuredSelection selection = (StructuredSelection) this.selection;
					if (!selection.isEmpty()) {
						IWorkbench workbench = PlatformUI.getWorkbench();
						Shell shell = workbench.getActiveWorkbenchWindow().getShell();
						StringBuilder fileNames = new StringBuilder();
						Iterator<Object> selected = selection.iterator();
						List<String> onlyFileNames = new ArrayList<String>();
						while (selected.hasNext()) {
							File file = (File) selected.next();
							// Por cada uno lo ejecuto contra la base

							fileNames.append(file.getRawLocation().toString());
							try {
								String s = IOUtils.toString(file.getContents());
								executeScript(value, Configuration.DBUSER, Configuration.DBPASSWORD, file.getRawLocation().toString(), true);
							} catch (IOException e) {
								Activator.showException(e);
							} catch (CoreException e) {
								Activator.showException(e);
							}
							// ejecutar y loguear a la consola
						}
						// TODO MD wizard.setClient(AbstractProyect.getDefaultScriptPrefix());
					}
				}
			}
		}
	}

	public static void main(String[] args) throws IOException {
		// Process p = Runtime.getRuntime().exec("mysql -uroot -proot -hlocalhost", null, new java.io.File("."));
		executeScript("corp_mgodoy", "root", "root", "/home/mgodoy/temp/1.sql", false);
	}

	public static String executeScript(String dbname, String dbuser, String dbpassword, String scriptpath, boolean verbose) {
		String output = null;
		try {
//			String[] cmds = { "/bin/sh", "-c", "mysql corp_mgodoy -uroot -proot -e\"select * from employee;\"" };
			String[] cmds = { "/bin/sh", "-c", "mysql "+dbname+  " -h"+Configuration.DATABASE+" -u"+dbuser+" -p"+dbpassword+" -v < \"" +scriptpath+"\"" };
			runAndLogToConsole(cmds);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}

	public static void runAndLogToConsole(String[] cmds) throws IOException {
		Process ps = Runtime.getRuntime().exec(cmds);
		new wtfplugin.console.ConsoleWriter().write(loadStream(ps.getInputStream()));
		new wtfplugin.console.ConsoleWriter().write(loadStream(ps.getErrorStream()));
	}

	private static String loadStream(InputStream in) throws IOException {
		int ptr = 0;
		in = new BufferedInputStream(in);
		StringBuffer buffer = new StringBuffer();
		while ((ptr = in.read()) != -1) {
			buffer.append((char) ptr);
		}
		return buffer.toString();
	}

	public static final int exec(Process proc) {
		return exec(proc, new PrintWriter(System.out));
	}

	public static final int exec(Process proc, Writer writer) {
		try {
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			// read the output from the command
			String s;
			while ((s = stdInput.readLine()) != null) {
				if (writer != null) {
					writer.write(s);
					writer.write("\n");
					writer.flush();
				}
			}
			// read any errors from the attempted command
			while ((stdError.readLine()) != null) {
				// System.out.println(s);
			}
			return proc.waitFor();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
