package wtfplugin.actions;

import java.io.ByteArrayInputStream;

import wtfplugin.Activator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;


public class NewVersion extends ActionDelegate implements IWorkbenchWindowActionDelegate {
	
	private ISelection selection;
	
	public static String VERSION_SCRIPT = "insert into dbversion (name) values ('DBVERSION');\n" +
			"commit;";
	
	public static String NEW_SCRIPT = "insert into dbscript (name, id_dbversion)\n" +
			"select 'SCRIPTNAME', db.id\n" +
			"from dbversion db\n" +
			"where db.id = (select max(id) from dbversion);\n" +
			"commit;\n" +
			"\n" +
			"\n" +
			"\n" +
			"update dbscript set finished = 1 where name = 'SCRIPTNAME' and id_dbversion = (select max(id) from dbversion);\n" +
			"commit;";

	public NewVersion() {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		this.selection = selection;
	}

	/**
	 * @see ActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if (this.selection instanceof StructuredSelection) {
			StructuredSelection selection = (StructuredSelection)this.selection;
			if (!selection.isEmpty()) {
				IWorkbench workbench = PlatformUI.getWorkbench();
				Shell shell = workbench.getActiveWorkbenchWindow().getShell();
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				InputDialog inputDialog = new InputDialog(Activator.getDefault().getWorkbench().getWorkbenchWindows()[0].getShell(), "Ingreso de version", "Ingrese la version a generar", "", null);
				int manual = inputDialog.open();
				if (manual == 0) {
					String value = inputDialog.getValue();
					IFolder folder = (IFolder)selection.getFirstElement();
					try {
						IFolder folder1 = folder.getFolder(new Path(value));
						folder1.create(true, false, new NullProgressMonitor());
						IFile file = folder1.getFile(new Path("000_db_version_insert.sql"));
						file.create(new ByteArrayInputStream(VERSION_SCRIPT.replace("DBVERSION", value).getBytes()), true, new NullProgressMonitor());
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
	}
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {

	}

	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub

	}
	

}
