package wtfplugin.actions;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import wtfplugin.utils.FileUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;

public class ReleaseVersion extends ActionDelegate implements IWorkbenchWindowActionDelegate {

	private ISelection selection;

	public ReleaseVersion() {
	}

	/*
	 * (non-Javadoc)
	 * 
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
			StructuredSelection selection = (StructuredSelection) this.selection;
			if (!selection.isEmpty()) {
				IWorkbench workbench = PlatformUI.getWorkbench();
				Shell shell = workbench.getActiveWorkbenchWindow().getShell();
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				IFolder folder = (IFolder) selection.getFirstElement();
				try {
					String  database = folder.getProject().getDescription().getName().contains("wtf") ? "corporate" : "cupones";
					StringBuilder sb = new StringBuilder();
					sb.append("use " + database + ";\n");
					IResource resourcesArray[] = folder.members();
					Arrays.sort(resourcesArray, new Comparator<IResource>() {
						@Override
						public int compare(IResource o1, IResource o2) {
							return o1.getName().compareTo(o2.getName());
						}
					});
					for (IResource resource : resourcesArray) {
						if (resource instanceof IFile) {
							IFile file = (IFile)resource;
							if (file.getName().toLowerCase().endsWith(".sql")) {
								if (!file.getName().toLowerCase().startsWith("post")) {
									String content = FileUtils.readFileContents(file);
									sb.append(content);
									sb.append("\n");
								}
							}
						}
					}
					sb.append("commit;\n");
					IFile file = folder.getFile(new Path(folder.getName() + ".sql"));
					file.create(new ByteArrayInputStream(sb.toString().getBytes()), true, new NullProgressMonitor());
					
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
