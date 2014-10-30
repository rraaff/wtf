package wtfplugin.actions;

import java.util.HashMap;
import java.util.Map;

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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;

import wtfplugin.Activator;


public class JarsToProjects extends ActionDelegate implements IWorkbenchWindowActionDelegate {

	public JarsToProjects() {
	}
	
	private boolean hasConfBuilder(ICommand[] builders) {
		for (ICommand command : builders) {
			if (command.getBuilderName().equals("WTFPlugin.confBuilder")) {
				return true;
			}
		}
		return false;
	}
	
	private boolean hasRBBuilder(ICommand[] builders) {
		for (ICommand command : builders) {
			if (command.getBuilderName().equals("WTFPlugin.rbBuilder")) {
				return true;
			}
		}
		return false;
	}
	
	private boolean hasImportVerifier(ICommand[] builders) {
		for (ICommand command : builders) {
			if (command.getBuilderName().equals("WTFPlugin.classpathVerifier")) {
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
		
		boolean applybuilders = false;
		MessageDialog dg = new MessageDialog(
				shell,
				"Desea aplicar el builder de RC y clases Persistentes",
				null,
				"Desea aplicar el builder de RC y clases Persistentes? Si es Corporate presione YES",
				MessageDialog.QUESTION, 
				new String[]{
					IDialogConstants.YES_LABEL, 
					IDialogConstants.NO_LABEL},
				0
				);
		switch(dg.open()) {
		case 0: 
			applybuilders = true;
			break;
		case 1:
			applybuilders = false;
			break;
		}
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		if (applybuilders) {
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
							builders = pro.getDescription().getBuildSpec();
						}
						if (!hasConfBuilder(builders)) {
							ICommand newbuilders[] = new ICommand[builders.length + 1];
							System.arraycopy(builders, 0, newbuilders, 0, builders.length);
							IProjectDescription desc =pro.getDescription(); 
							ICommand buildCommand = desc.newCommand();
							//buildCommand.setBuilder(new ClasspathVerifier());
							buildCommand.setBuilderName("WTFPlugin.confBuilder");
							newbuilders[newbuilders.length - 1] = buildCommand;
							desc.setBuildSpec(newbuilders);
							pro.setDescription(desc, null);
							builders = pro.getDescription().getBuildSpec();
						}
						if (pro.hasNature(JavaCore.NATURE_ID) && !hasImportVerifier(builders)) {
							ICommand newbuilders[] = new ICommand[builders.length + 1];
							System.arraycopy(builders, 0, newbuilders, 0, builders.length);
							IProjectDescription desc =pro.getDescription(); 
							ICommand buildCommand = desc.newCommand();
							//buildCommand.setBuilder(new ClasspathVerifier());
							buildCommand.setBuilderName("WTFPlugin.classpathVerifier");
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
