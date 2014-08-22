/*
 * Created on Feb 13, 2007
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package wtfplugin.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.internal.resources.File;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;

import wtfplugin.Activator;


/**
 * @author mgodoy
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ExecuteScriptsFromSelection extends ActionDelegate {

	private ISelection selection;
	
	public static String lastUsedConteiner;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		this.selection = selection;
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		super.run(action);
		if (this.selection instanceof StructuredSelection) {
			InputDialog inputDialog = new InputDialog(Activator.getDefault().getWorkbench().getWorkbenchWindows()[0].getShell(), "Ingreso el nombre de la base", "Ingreso el nombre de la base, por ejemplo, corp_mgodoy", "corp_" + System.getProperty("user.name"), null);
			int manual = inputDialog.open();
			if (manual == 0) {
				String value = inputDialog.getValue();
				if (!StringUtils.isEmpty(value)) {
					StructuredSelection selection = (StructuredSelection)this.selection;
					if (!selection.isEmpty()) {
						IWorkbench workbench = PlatformUI.getWorkbench();
						Shell shell = workbench.getActiveWorkbenchWindow().getShell();
						StringBuilder fileNames = new StringBuilder();
						Iterator<Object> selected = selection.iterator();
						List<String> onlyFileNames = new ArrayList<String>();
						while (selected.hasNext()) {
							File file = (File)selected.next();
							// Por cada uno lo ejecuto contra la base
							
							fileNames.append(file.getRawLocation().toString());
		//					ejecutar y loguear a la consola
						}
						//TODO MD wizard.setClient(AbstractProyect.getDefaultScriptPrefix());
					} 
				}
			}
		}
	}

}
