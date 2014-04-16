package wtfplugin.actions;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;


public class JarsToProjects extends ActionDelegate implements IWorkbenchWindowActionDelegate {

	public JarsToProjects() {
	}
	
	private boolean hasRBBuilder(ICommand[] builders) {
		for (ICommand command : builders) {
			if (command.getBuilderName().equals("WTFPlugin.rbBuilder")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @see ActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		Shell shell = workbench.getActiveWorkbenchWindow().getShell();
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		{
			try {
				IProject[] projects = root.getProjects();
				for (IProject pro : projects) {
					if (pro.isOpen()) {
						ICommand builders[] = pro.getDescription().getBuildSpec();
						if (!hasRBBuilder(builders)) {
							ICommand newbuilders[] = new ICommand[builders.length + 1];
							System.arraycopy(builders, 0, newbuilders, 0, builders.length);
							IProjectDescription desc =pro.getDescription(); 
							ICommand buildCommand = desc.newCommand();
							//buildCommand.setBuilder(new ClasspathVerifier());
							buildCommand.setBuilderName("WTFPlugin.rbBuilder");
							newbuilders[newbuilders.length - 1] = buildCommand;
							desc.setBuildSpec(newbuilders);
							pro.setDescription(desc, null);
						}
					}
				}
			}catch (Exception e) {
				Activator.showException(e);
			}
		}
		
		System.out.println("push");
		IProject[] projects = root.getProjects();
		
		Map<String, IProject> projectsMaps = new HashMap<String, IProject>();
		for (IProject iterProyect : projects) {
			projectsMaps.put(iterProyect.getName().toLowerCase(), iterProyect);
		}
		StringBuffer converted = new StringBuffer();
		try {
			for (IProject iterProyect : projects) {	
				if (iterProyect.isOpen()) {
					if (iterProyect.hasNature(JavaCore.NATURE_ID)) {
						// IJavaProject referenciaba a un library commons tiene que referenciar al nuevo 
						converted.append(fixLibrariesIn(iterProyect, projectsMaps));
					}
				}
			}
			Activator.showMessage("Fix de classpath", "Se convirtieron los siguientes proyectos " + converted.toString());
		} catch (CoreException e) {
			Activator.showException(e);
		}
//		dialog.create();
//		dialog.open(); 
		
	}

	private String fixLibrariesIn(IProject iterProyect, Map<String, IProject> projectsMaps) throws JavaModelException {
			StringBuffer sb = new StringBuffer();
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IJavaProject javaProject = JavaCore.create(iterProyect);
			IClasspathEntry[] entries = javaProject.getRawClasspath(); 
			int index = 0;
			for (IClasspathEntry classpathEntry : entries) {
				if (IClasspathEntry.CPE_VARIABLE == classpathEntry.getEntryKind()) {
					String projectToReference = getProjectName(classpathEntry.getPath().lastSegment());
					System.out.println(projectToReference);
					IProject project = projectsMaps.get(projectToReference.toLowerCase());
					if (project != null) {
						System.out.println("project found " + project.getName());
						entries[index] = JavaCore.newProjectEntry(new Path("/" + project.getName()), true);
						sb.append("\n" + classpathEntry.getPath().lastSegment() + " > " + project.getName());
					} else {
						entries[index] =  JavaCore.newVariableEntry(classpathEntry.getPath(), classpathEntry.getSourceAttachmentPath(), classpathEntry.getSourceAttachmentRootPath(), true);
					}
				} 
				index++;
			}
			try {
				javaProject.setRawClasspath(entries, null);
			} catch (Exception e) {
				Activator.showException(e);
			}
			return sb.toString();
	}

	private String getProjectName(String lastSegment) {
		String test = lastSegment.replace(".jar", "");
		test = test.replace("-SNAPSHOT", "");
//		Regex patern = new Regex("-\\d+(\\.\\d)*(\\.\\d)*");
		test = test.replaceAll("-\\d+(\\.\\d*)*(\\.\\d*)*", "");
		return test;
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {

	}

	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub

	}
	
	public static void main(String[] args) {
		System.out.println(new JarsToProjects().getProjectName("log4j-1.2.17.jar"));
	}

}
