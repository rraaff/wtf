package wtfplugin.actions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.launching.RuntimeClasspathEntry;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;

import wtfplugin.Activator;

public class LaunchTomcat extends ActionDelegate implements IWorkbenchWindowActionDelegate {
	
	private static String serverxml = null; 
	

	public LaunchTomcat() {
	}

	/**
	 * @see ActionDelegate#run(IAction)
	 */
	public void run(IAction actionP) {
		launchDefaultTomcat();
	}

	public void launchDefaultTomcat() {
		try {
			
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IWorkbench workbench = PlatformUI.getWorkbench();
			
			IProject[] projects = root.getProjects();
			for (IProject pro : projects) {
				if (pro.isOpen()) {
						if (pro.hasNature(JavaCore.NATURE_ID)) {
							IFile file = pro.getFile(".wtf");
							InputStream is = null;
							try {
								if (file != null && file.exists()) {
										Properties props = new Properties();
										is = file.getContents();
										props.load(is);
										if ("true".equals(props.getProperty("default", "true"))) {
											LaunchTomcat.launchTomcat(pro, props.getProperty("contextPath", "/" + pro.getName()), props.getProperty("port", "8080"), props.getProperty("serverport", "8208"), props.getProperty("contextcontent",""), props.getProperty("jvmargs",""));
											return;
										}
								}
							} finally {
								if (is!= null) {
									try {
										is.close();
									} catch (IOException e1) {
										Activator.showException(e1);
									}
								}
							}
						}
					}
			}
			
			String webappname = "/corporate";
			IProject project = root.getProject("corporate");
			File file = null;
			if (project == null || !project.exists() || !project.isAccessible() || !project.isOpen()) {
				project = root.getProject("marcablanca-service-web");
				webappname = "/marcablanca-service-web";
			}  
			if (project == null || !project.exists() || !project.isAccessible() || !project.isOpen()) {
				project = root.getProject("marcablanca");
				webappname = "/marcablanca";
			}
			String port ="8080";
			String serverPort = "8208";
			launchTomcat(project, webappname, port, serverPort, "", "");
		} catch (Exception e) {
			Activator.showException(e);
		}
	}

	public static void launchTomcat(IProject project, String webappname, String port, String serverPort, String contextContent, String jvmArgs) throws CoreException, IOException, JavaModelException {
		
		IProcess process= DebugUITools.getCurrentProcess();
		if (process != null && process.getLaunch().getLaunchConfiguration().getName().equals("Start Tomcat")) {
			process.terminate();
		}
			
		
		File file;
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		ILaunchConfiguration[] configurations = manager.getLaunchConfigurations(type);
		for (int i = 0; i < configurations.length; i++) {
			ILaunchConfiguration configuration = configurations[i];
			if (configuration.getName().equals("Start Tomcat")) {
				configuration.delete();
				break;
			}
		}
		IVMInstall jre = JavaRuntime.getDefaultVMInstall();
		File jdkHome = jre.getInstallLocation();
		ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, "Start Tomcat");
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, jre.getName());
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, jre.getVMInstallType().getId());
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "org.apache.catalina.startup.Bootstrap");
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "-config "+System.getProperty("java.io.tmpdir") + "/temp_server.xml"+" start");
		IPath toolsPath = new Path(jdkHome.getAbsolutePath()).append("lib").append("tools.jar");
		IRuntimeClasspathEntry toolsEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(toolsPath);
		toolsEntry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
		IPath bootstrapPath = new Path("TOMCAT6_HOME").append("bin").append("bootstrap.jar");
		IRuntimeClasspathEntry bootstrapEntry = JavaRuntime.newVariableRuntimeClasspathEntry(bootstrapPath);
		bootstrapEntry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
		IPath systemLibsPath = new Path(JavaRuntime.JRE_CONTAINER);
		IRuntimeClasspathEntry systemLibsEntry = JavaRuntime.newRuntimeContainerClasspathEntry(systemLibsPath, IRuntimeClasspathEntry.STANDARD_CLASSES);
		List classpath = new ArrayList();
		classpath.add(toolsEntry.getMemento());
		classpath.add(bootstrapEntry.getMemento());
		classpath.add(systemLibsEntry.getMemento());

		addTomcatJarToClasspath(classpath, "jsp-api.jar");
		addTomcatJarToClasspath(classpath, "el-api.jar");
		
		if (project == null || !project.exists() || !project.isAccessible() || !project.isOpen()) {
			Activator.showErrorMessage("Tomcat", "No se ha encontrado ningun projecto web");
			return;
		}
		
		
		file = project.getFolder("src").getRawLocation().toFile().getParentFile();
		String appBase = file.toString();
		
		String docBase = file.toString();
		
		String docbase =docBase + "/src/main/webapp";
		String workdir =docBase + "/src/main/webapp/work";
		
		// Primero escribo el server xml
		org.apache.commons.io.FileUtils.deleteQuietly(new File(System.getProperty("java.io.tmpdir") + "/temp_server.xml"));
		serverxml = null;
		if (serverxml == null) {
			InputStream is = LaunchTomcat.class.getResourceAsStream("server.xml");
			String tempServer = IOUtils.toString(is);
			serverxml = tempServer;
			is.close();
		}
		String newTempServer = serverxml.replace("@contextpath@", webappname);
		newTempServer = newTempServer.replace("@appbase@", appBase);
		newTempServer = newTempServer.replace("@serverport@", serverPort);
		newTempServer = newTempServer.replace("@httpport@", port);
		newTempServer = newTempServer.replace("@docbase@", docbase);
		newTempServer = newTempServer.replace("@workdir@", workdir);
		newTempServer = newTempServer.replace("@contextcontent@", contextContent);
		
		
		File f = new File(System.getProperty("java.io.tmpdir") + "/temp_server.xml");
		org.apache.commons.io.FileUtils.writeStringToFile(f, newTempServer);
		
		IJavaProject javaProject = JavaCore.create(project);
		IRuntimeClasspathEntry projectOutputPath = JavaRuntime.newProjectRuntimeClasspathEntry(javaProject);
		classpath.add(projectOutputPath.getMemento());
		
//			classpath.add("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><runtimeClasspathEntry path=\"/home/mgodoy/gitorious/test_plugin/wtf/wtf-service-web/target/classes\" type=\"1\"/>");
		IClasspathEntry[] entries = javaProject.getRawClasspath(); 
		int index = 0;
		for (IClasspathEntry classpathEntry : entries) {
			if (classpathEntry.getEntryKind() != IClasspathEntry.CPE_SOURCE) {
				if (classpathEntry.getEntryKind() != IClasspathEntry.CPE_CONTAINER) {
					classpath.add(new RuntimeClasspathEntry(classpathEntry).getMemento());
				}
			}
		}
		
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpath);
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "-Djava.endorsed.dirs=\"..\\common\\endorsed\""
				+ "-Dcatalina.base=\"..\"" + "-Dcatalina.home=\"..\"" + "-Djava.io.tmpdir=\"..\\temp\" " + (jvmArgs == null ? "" : jvmArgs));
		
		
		File workingDir = JavaCore.getClasspathVariable("TOMCAT6_HOME").append("bin").toFile();
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, workingDir.getAbsolutePath());
		ILaunchConfiguration configuration = workingCopy.doSave();
		DebugUITools.launch(configuration, ILaunchManager.DEBUG_MODE);
	}

	public static void addTomcatJarToClasspath(List classpath, String jar) throws CoreException {
		IPath jspapijar = new Path("TOMCAT6_HOME").append("lib").append(jar);
		IRuntimeClasspathEntry jspapijarEntry = JavaRuntime.newVariableRuntimeClasspathEntry(jspapijar);
		classpath.add(jspapijarEntry.getMemento());
	}

	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub

	}

}
