package wtfplugin.launch;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class ProjectsFirstLauncher extends JavaLaunchDelegate implements ILaunchConfigurationDelegate {

	public ProjectsFirstLauncher() {
	}

	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		super.launch(configuration, mode, launch, monitor);
	}

	
	@Override
	public String[] getClasspath(ILaunchConfiguration configuration) throws CoreException {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();
		IProject commons = null;
		IProject commonsWeb = null;
		for (IProject project : projects) {
			if (project.getName().equals("wtf-commons-service")) {
				commons = project;
			}
			if (project.getName().equals("wtf-commons-web")) {
				commonsWeb = project;
			}
		}
		IJavaProject commonsJ = JavaCore.create(commons);
		IJavaProject commonsWebJ = JavaCore.create(commonsWeb);
		String orig[] = super.getClasspath(configuration);
		String cp[] = new String[orig.length + 2];
		System.arraycopy(orig, 0, cp, 2, orig.length);
		IFile file     = commons.getFile(commonsJ.getOutputLocation());
		cp[0] = file.getRawLocation().toOSString();
		IFile fileWeb     = commonsWeb.getFile(commonsWebJ.getOutputLocation());
		cp[1] = fileWeb.getRawLocation().toOSString();
		return cp;
	}
}
