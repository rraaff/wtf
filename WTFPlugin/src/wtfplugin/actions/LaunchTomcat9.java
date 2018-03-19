package wtfplugin.actions;

import java.io.File;
import java.io.FileOutputStream;
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
import wtfplugin.preferences.WTFPreferences;

public class LaunchTomcat9 {
	

	public LaunchTomcat9() {
	}


	public static void launchTomcat(IProject project, String webappname, String port, String httpsPort, String serverPort, String contextContent, String jvmArgs) throws CoreException, IOException, JavaModelException {
		
		IProcess process= DebugUITools.getCurrentProcess();
		if (process != null && process.getLaunch().getLaunchConfiguration().getName().equals(getLaunchName(project))) {
			process.terminate();
		}
			
		
		File file;
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		ILaunchConfiguration[] configurations = manager.getLaunchConfigurations(type);
		for (int i = 0; i < configurations.length; i++) {
			ILaunchConfiguration configuration = configurations[i];
			if (configuration.getName().equals(getLaunchName(project))) {
				configuration.delete();
				break;
			}
		}
		IVMInstall jre = JavaRuntime.getDefaultVMInstall();
		File jdkHome = jre.getInstallLocation();
		ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, getLaunchName(project));
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, jre.getName());
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, jre.getVMInstallType().getId());
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "org.apache.catalina.startup.Bootstrap");
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "-config "+System.getProperty("java.io.tmpdir") + "/"+getLaunchName(project)+"_server.xml"+" start");
		IPath toolsPath = new Path(jdkHome.getAbsolutePath()).append("lib").append("tools.jar");
		IRuntimeClasspathEntry toolsEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(toolsPath);
		toolsEntry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
		IPath bootstrapPath = new Path(WTFPreferences.getTomcatVariable("9")).append("bin").append("bootstrap.jar");
		IRuntimeClasspathEntry bootstrapEntry = JavaRuntime.newVariableRuntimeClasspathEntry(bootstrapPath);
		bootstrapEntry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
		IPath systemLibsPath = new Path(JavaRuntime.JRE_CONTAINER);
		IRuntimeClasspathEntry systemLibsEntry = JavaRuntime.newRuntimeContainerClasspathEntry(systemLibsPath, IRuntimeClasspathEntry.STANDARD_CLASSES);
		List classpath = new ArrayList();
		classpath.add(toolsEntry.getMemento());
		classpath.add(bootstrapEntry.getMemento());
		classpath.add(systemLibsEntry.getMemento());

		addTomcatBinJarToClasspath(classpath, "tomcat-juli.jar");
		
		File tomcatLib = JavaCore.getClasspathVariable(WTFPreferences.getTomcatVariable("9")).append("lib").toFile();
		for (String jarFile : tomcatLib.list()) {
			if (jarFile.endsWith(".jar")) {
				addTomcatLibJarToClasspath(classpath, jarFile);
			}
		}
		
		if (project == null || !project.exists() || !project.isAccessible() || !project.isOpen()) {
			Activator.showErrorMessage("Tomcat", "No se ha encontrado ningun projecto web");
			return;
		}
		
		
		file = project.getFolder("src").getRawLocation().toFile().getParentFile();
		String appBase = file.toString();
		
		String docBase = file.toString();
		
		String docbase =docBase + "/src/main/webapp";
		String workdir =docBase + "/src/main/webapp/work";
		
		// Borro el keystore viejo
		org.apache.commons.io.FileUtils.deleteQuietly(new File(System.getProperty("user.home") + "/.keystoreWTF"));
		if (LaunchTomcat.keystore == null) {
			InputStream is = LaunchTomcat9.class.getResourceAsStream("keystoreWTF");
			LaunchTomcat.keystore = IOUtils.toByteArray(is);
			is.close();
		}
		File keystoreFile = new File(System.getProperty("user.home") + "/.keystoreWTF");
		org.apache.commons.io.FileUtils.writeByteArrayToFile(keystoreFile, LaunchTomcat.keystore);
		
		// Excribo el context.xml
		File contextDir = JavaCore.getClasspathVariable(WTFPreferences.getTomcatVariable("9")).toFile();
		// no esta soportado por el momento
		if (WTFPreferences.clusterSessionMongo() && false) {
			org.apache.commons.io.FileUtils.deleteQuietly(new File(contextDir + "/conf/context.xml"));
			if (LaunchTomcat.contextxml== null) {
				InputStream is = LaunchTomcat9.class.getResourceAsStream("context.xml");
				String tempServer = IOUtils.toString(is);
				LaunchTomcat.contextxml = tempServer;
				is.close();
			}
			
			String newTempContext = LaunchTomcat.contextxml.replace("@mongostore@", WTFPreferences.getUsername());
			newTempContext = newTempContext.replace("@mongosessionhost@", WTFPreferences.getMongosessionhosts());
			File fileContext = new File(contextDir + "/conf/context.xml");
			org.apache.commons.io.FileUtils.writeStringToFile(fileContext, newTempContext);
		}
		// Primero escribo el server xml
		org.apache.commons.io.FileUtils.deleteQuietly(new File(System.getProperty("java.io.tmpdir") + "/"+project.getName()+"_server.xml"));
//		if (serverxml == null) {
		{
			InputStream is = LaunchTomcat9.class.getResourceAsStream("server.xml");
			String tempServer = IOUtils.toString(is);
			LaunchTomcat.serverxml = tempServer;
			is.close();
		}
//		}
		String newTempServer = LaunchTomcat.serverxml.replace("@contextpath@", webappname);
		newTempServer = newTempServer.replace("@appbase@", appBase);
		newTempServer = newTempServer.replace("@serverport@", serverPort);
		if (!port.equals("")) {
			String repl = LaunchTomcat6.HTTP.replace("@httpport@", port);
			newTempServer = newTempServer.replace("@HTTPCONNECTOR@", repl);
		} else {
			newTempServer = newTempServer.replace("@HTTPCONNECTOR@", "");
		}
		if (!httpsPort.equals("")) {
			String repl = LaunchTomcat6.HTTPS.replace("@httpsport@", httpsPort);
			newTempServer = newTempServer.replace("@HTTPSCONNECTOR@", repl);
		} else {
			newTempServer = newTempServer.replace("@HTTPSCONNECTOR@", "");
		}
		newTempServer = newTempServer.replace("@docbase@", docbase);
		newTempServer = newTempServer.replace("@workdir@", workdir);
		newTempServer = newTempServer.replace("@contextcontent@", contextContent);	
		
		File f = new File(System.getProperty("java.io.tmpdir") + "/" + getLaunchName(project) + "_server.xml");
		org.apache.commons.io.FileUtils.writeStringToFile(f, newTempServer);
		
		// copio las librerias de mongo si no estan
		File mongoStore = JavaCore.getClasspathVariable(WTFPreferences.getTomcatVariable("9")).append("lib").append("mongo-store-proxy-1.9.jar").toFile();
		File mongoDriver = JavaCore.getClasspathVariable(WTFPreferences.getTomcatVariable("9")).append("lib").append("mongo-java-driver-3.2.1.jar").toFile();
		if (WTFPreferences.clusterSessionMongo() && false) {
			if (!mongoStore.exists()) {
				InputStream is = LaunchTomcat.class.getResourceAsStream("mongo-store-proxy-1.9.jar");
				FileOutputStream fout= new FileOutputStream(mongoStore);
				IOUtils.copy(is, fout);
				is.close();
				fout.close();
			}
			if (!mongoDriver.exists()) {
				InputStream is = LaunchTomcat9.class.getResourceAsStream("mongo-java-driver-3.2.1.jar");
				FileOutputStream fout= new FileOutputStream(mongoDriver);
				IOUtils.copy(is, fout);
				is.close();
				fout.close();
			}
		} else {
			if (mongoStore.exists()) {
				mongoStore.delete();
			}
			if (mongoDriver.exists()) {
				mongoDriver.delete();
			}
		}
		
		IJavaProject javaProject = JavaCore.create(project);
		IRuntimeClasspathEntry projectOutputPath = JavaRuntime.newProjectRuntimeClasspathEntry(javaProject);
		classpath.add(projectOutputPath.getMemento());
		
//			classpath.add("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><runtimeClasspathEntry path=\"/home/mgodoy/gitorious/test_plugin/wtf/wtf-service-web/target/classes\" type=\"1\"/>");
		addToClasspath(classpath, javaProject);
		
		String jvmArgsParams = jvmArgs;
		if (jvmArgsParams != null) {
			int beginIndex = jvmArgsParams.indexOf(":3306/");
			if (beginIndex != -1) {
				beginIndex = beginIndex  + 6;
				String first = jvmArgsParams.substring(0, beginIndex);
				String second = jvmArgsParams.substring(jvmArgsParams.indexOf("?", beginIndex));
				jvmArgsParams = first + WTFPreferences.getUsername() + second;
			}
		} else {
			jvmArgsParams = "";
		}
		
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpath);
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "-Djava.endorsed.dirs=\"..\\common\\endorsed\""
				+ "-Dcatalina.base=\"..\"" + "-Dcatalina.home=\"..\"" + "-Djava.io.tmpdir=\"..\\temp\" " + (jvmArgsParams));
		
		
		File workingDir = JavaCore.getClasspathVariable(WTFPreferences.getTomcatVariable("9")).append("bin").toFile();
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, workingDir.getAbsolutePath());
		ILaunchConfiguration configuration = workingCopy.doSave();
		DebugUITools.launch(configuration, ILaunchManager.DEBUG_MODE);
	}

	public static String getLaunchName(IProject project) {
		return "start_" + project.getName();
	}


	public static void addToClasspath( List classpath, IJavaProject javaProject) throws JavaModelException, CoreException {
		IClasspathEntry[] entries = javaProject.getRawClasspath(); 
		int index = 0;
		for (IClasspathEntry classpathEntry : entries) {
			if (classpathEntry.getEntryKind() != IClasspathEntry.CPE_SOURCE) {
				if (classpathEntry.getEntryKind() != IClasspathEntry.CPE_CONTAINER) {
					if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
						IPath path = classpathEntry.getPath();
						String projectName = path.lastSegment();
						IProject refProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
						IJavaProject refProjectJava = JavaCore.create(refProject);
						addToClasspath(classpath, refProjectJava);
					}
					classpath.add(new RuntimeClasspathEntry(classpathEntry).getMemento());
				}
			}
		}
	}
	
	public static void addTomcatBinJarToClasspath(List classpath, String jar) throws CoreException {
		IPath jspapijar = new Path(WTFPreferences.getTomcatVariable("9")).append("bin").append(jar);
		IRuntimeClasspathEntry jspapijarEntry = JavaRuntime.newVariableRuntimeClasspathEntry(jspapijar);
		classpath.add(jspapijarEntry.getMemento());
	}
	
	public static void addTomcatLibJarToClasspath(List classpath, String jar) throws CoreException {
		IPath jspapijar = new Path(WTFPreferences.getTomcatVariable("9")).append("lib").append(jar);
		IRuntimeClasspathEntry jspapijarEntry = JavaRuntime.newVariableRuntimeClasspathEntry(jspapijar);
		classpath.add(jspapijarEntry.getMemento());
	}

	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub

	}

}
