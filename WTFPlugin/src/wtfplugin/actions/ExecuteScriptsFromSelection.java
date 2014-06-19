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

import org.eclipse.core.internal.resources.File;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;


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
			StructuredSelection selection = (StructuredSelection)this.selection;
			if (!selection.isEmpty()) {
				IWorkbench workbench = PlatformUI.getWorkbench();
				Shell shell = workbench.getActiveWorkbenchWindow().getShell();
				StringBuilder fileNames = new StringBuilder();
				Iterator<Object> selected = selection.iterator();
				List<String> onlyFileNames = new ArrayList<String>();
				while (selected.hasNext()) {
					File file = (File)selected.next();
					fileNames.append(file.getRawLocation().toString());
//					ejecutar y loguear a la consola
				}
				//TODO MD wizard.setClient(AbstractProyect.getDefaultScriptPrefix());
			} 
		}
	}

}
