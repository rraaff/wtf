package wtfplugin.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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
import wtfplugin.console.ConsoleWriter;
import wtfplugin.preferences.WTFPreferences;

public class LaunchTomcat6 {
	
	public static String HTTP = "<Connector port=\"@httpport@\" maxHttpHeaderSize=\"8192\" maxThreads=\"150\" minSpareThreads=\"1\" maxSpareThreads=\"10\" enableLookups=\"false\" redirectPort=\"8443\" acceptCount=\"100\" connectionTimeout=\"20000\" disableUploadTimeout=\"true\" maxPostSize=\"0\"/>";
	public static String HTTPS = "<Connector port=\"@httpsport@\" maxThreads=\"200\" scheme=\"https\" secure=\"true\" SSLEnabled=\"true\" keystoreFile=\"${user.home}/.keystoreWTF\" keystorePass=\"changeit\" clientAuth=\"false\" sslProtocol=\"TLS\"/>";
	

	public LaunchTomcat6() {
	}


	public static void launchTomcat(IProject project, String webappname, String port, String httpsPort, String serverPort, String contextContent, String jvmArgs) throws CoreException, IOException, JavaModelException {
		
		IProcess process= DebugUITools.getCurrentProcess();
		if (process != null && process.getLaunch().getLaunchConfiguration().getName().equals("Start-" + webappname.substring(1))) {
			process.terminate();
		}
			
		
		File file;
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		ILaunchConfiguration[] configurations = manager.getLaunchConfigurations(type);
		for (int i = 0; i < configurations.length; i++) {
			ILaunchConfiguration configuration = configurations[i];
			if (configuration.getName().equals("Start-" + webappname.substring(1))) {
				configuration.delete();
				break;
			}
		}
		IVMInstall jre = JavaRuntime.getDefaultVMInstall();
		File jdkHome = jre.getInstallLocation();
		ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, "Start-" + webappname.substring(1));
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, jre.getName());
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, jre.getVMInstallType().getId());
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "org.apache.catalina.startup.Bootstrap");
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "-config "+System.getProperty("java.io.tmpdir") + "/temp_server.xml"+" start");
		IPath toolsPath = new Path(jdkHome.getAbsolutePath()).append("lib").append("tools.jar");
		IRuntimeClasspathEntry toolsEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(toolsPath);
		toolsEntry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
		IPath bootstrapPath = new Path(WTFPreferences.getTomcatVariable()).append("bin").append("bootstrap.jar");
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
		
		// Borro el keystore viejo
		org.apache.commons.io.FileUtils.deleteQuietly(new File(System.getProperty("user.home") + "/.keystoreWTF"));
		if (LaunchTomcat.keystore == null) {
			InputStream is = LaunchTomcat6.class.getResourceAsStream("keystoreWTF");
			LaunchTomcat.keystore = IOUtils.toByteArray(is);
			is.close();
		}
		File keystoreFile = new File(System.getProperty("user.home") + "/.keystoreWTF");
		org.apache.commons.io.FileUtils.writeByteArrayToFile(keystoreFile, LaunchTomcat.keystore);
		
		// Excribo el context.xml
		File contextDir = JavaCore.getClasspathVariable(WTFPreferences.getTomcatVariable()).toFile();
		
		if (!WTFPreferences.homeOffice()) {
			if (WTFPreferences.clusterSessionMongo()) {
				org.apache.commons.io.FileUtils.deleteQuietly(new File(contextDir + "/conf/context.xml"));
				if (LaunchTomcat.contextxml== null) {
					InputStream is = LaunchTomcat6.class.getResourceAsStream("context.xml");
					String tempServer = IOUtils.toString(is);
					LaunchTomcat.contextxml = tempServer;
					is.close();
				}
				String newTempContext = LaunchTomcat.contextxml.replace("@mongostore@", WTFPreferences.getUsername());
				newTempContext = newTempContext.replace("@mongosessionhost@", WTFPreferences.getMongosessionhosts());
				File fileContext = new File(contextDir + "/conf/context.xml");
				org.apache.commons.io.FileUtils.writeStringToFile(fileContext, newTempContext);
			} else {
				restoreContext(contextDir);
			}
		} else {
			restoreContext(contextDir);
		}
		
		// Primero escribo el server xml
		org.apache.commons.io.FileUtils.deleteQuietly(new File(System.getProperty("java.io.tmpdir") + "/temp_server.xml"));
		if (LaunchTomcat.serverxml == null) {
			InputStream is = LaunchTomcat6.class.getResourceAsStream("server.xml");
			String tempServer = IOUtils.toString(is);
			LaunchTomcat.serverxml = tempServer;
			is.close();
		}
		String newTempServer = LaunchTomcat.serverxml.replace("@contextpath@", webappname);
		newTempServer = newTempServer.replace("@appbase@", appBase);
		newTempServer = newTempServer.replace("@serverport@", serverPort);
		if (!port.equals("")) {
			String repl = HTTP.replace("@httpport@", port);
			newTempServer = newTempServer.replace("@HTTPCONNECTOR@", repl);
		} else {
			newTempServer = newTempServer.replace("@HTTPCONNECTOR@", "");
		}
		if (!httpsPort.equals("")) {
			String repl = HTTPS.replace("@httpsport@", httpsPort);
			newTempServer = newTempServer.replace("@HTTPSCONNECTOR@", repl);
		} else {
			newTempServer = newTempServer.replace("@HTTPSCONNECTOR@", "");
		}
		newTempServer = newTempServer.replace("@docbase@", docbase);
		newTempServer = newTempServer.replace("@workdir@", workdir);
		newTempServer = newTempServer.replace("@contextcontent@", contextContent);	
		
		new wtfplugin.console.ConsoleWriter().write("Launching " + webappname);
		new wtfplugin.console.ConsoleWriter().write("http access :" + port + webappname);
		new wtfplugin.console.ConsoleWriter().write("https access :" + httpsPort + webappname);
		
		File f = new File(System.getProperty("java.io.tmpdir") + "/temp_server.xml");
		org.apache.commons.io.FileUtils.writeStringToFile(f, newTempServer);
		
		// copio las librerias de mongo si no estan
		File mongoStore = JavaCore.getClasspathVariable(WTFPreferences.getTomcatVariable()).append("lib").append("mongo-store-proxy-1.6.jar").toFile();
		if (!mongoStore.exists()) {
			InputStream is = LaunchTomcat6.class.getResourceAsStream("mongo-store-proxy-1.6.jar");
			FileOutputStream fout= new FileOutputStream(mongoStore);
			IOUtils.copy(is, fout);
			is.close();
			fout.close();
		}
		File mongoDriver = JavaCore.getClasspathVariable(WTFPreferences.getTomcatVariable()).append("lib").append("mongo-java-driver-2.10.1.jar").toFile();
		if (!mongoDriver.exists()) {
			InputStream is = LaunchTomcat6.class.getResourceAsStream("mongo-java-driver-2.10.1.jar");
			FileOutputStream fout= new FileOutputStream(mongoDriver);
			IOUtils.copy(is, fout);
			is.close();
			fout.close();
		}
		
		IJavaProject javaProject = JavaCore.create(project);
		IRuntimeClasspathEntry projectOutputPath = JavaRuntime.newProjectRuntimeClasspathEntry(javaProject);
		classpath.add(projectOutputPath.getMemento());
		
//			classpath.add("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><runtimeClasspathEntry path=\"/home/mgodoy/gitorious/test_plugin/wtf/wtf-service-web/target/classes\" type=\"1\"/>");
		addToClasspath(classpath, javaProject);
		
		String jvmArgsParams = jvmArgs;
		if (jvmArgsParams != null) {
			Map<String, String> replacements = WTFPreferences.getReplacementsJVM();
			replacements.put("corporate_dev", WTFPreferences.getUsername());
			replacements.put("[username]", WTFPreferences.getUsername());
			for (Map.Entry<String, String> entry : replacements.entrySet()) {
				jvmArgsParams = StringUtils.replace(jvmArgsParams, entry.getKey(), entry.getValue());
			}
		} else {
			jvmArgsParams = "";
		}
		
		if (WTFPreferences.homeOffice()) {
			jvmArgsParams = StringUtils.replace(jvmArgsParams, "active=localhost","active=rc");
			jvmArgsParams = StringUtils.replace(jvmArgsParams, "jdbc:mysql","DUMMY");
		}
		
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpath);
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "-Djava.endorsed.dirs=\"..\\common\\endorsed\""
				+ "-Dcatalina.base=\"..\"" + "-Dcatalina.home=\"..\"" + "-Djava.io.tmpdir=\"..\\temp\" " + (jvmArgsParams));
		
		
		File workingDir = JavaCore.getClasspathVariable(WTFPreferences.getTomcatVariable()).append("bin").toFile();
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, workingDir.getAbsolutePath());
		ILaunchConfiguration configuration = workingCopy.doSave();
		DebugUITools.launch(configuration, ILaunchManager.DEBUG_MODE);
	}

	private static void restoreContext(File contextDir) {
		try {
			org.apache.commons.io.FileUtils.deleteQuietly(new File(contextDir + "/conf/context.xml"));
			InputStream is = LaunchTomcat6.class.getResourceAsStream("contextHomeOffice.xml");
			String newTempContext = IOUtils.toString(is);
			is.close();
			File fileContext = new File(contextDir + "/conf/context.xml");
			org.apache.commons.io.FileUtils.writeStringToFile(fileContext, newTempContext);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	public static void addTomcatJarToClasspath(List classpath, String jar) throws CoreException {
		IPath jspapijar = new Path(WTFPreferences.getTomcatVariable()).append("lib").append(jar);
		IRuntimeClasspathEntry jspapijarEntry = JavaRuntime.newVariableRuntimeClasspathEntry(jspapijar);
		classpath.add(jspapijarEntry.getMemento());
	}

	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub

	}

}
