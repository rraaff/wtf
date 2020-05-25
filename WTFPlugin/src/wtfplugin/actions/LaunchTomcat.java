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

public class LaunchTomcat extends ActionDelegate implements IWorkbenchWindowActionDelegate {

	protected static String contextxml = null;

	protected static String serverxml = null;
	protected static byte keystore[];

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
									LaunchTomcat.launchTomcat(pro,
											props.getProperty("contextPath", "/" + pro.getName()),
											props.getProperty("port", "8080"), props.getProperty("httpsPort", "8443"),
											props.getProperty("serverport", "8208"),
											props.getProperty("docbase", "/scr/main/webapp"),
											props.getProperty("contextcontent", ""), props.getProperty("jvmargs", ""),
											props.getProperty("tomcatVersion", WTFPreferences.getTomcatVersion()),
											getExclusions(props));
									return;
								}
							}
						} finally {
							if (is != null) {
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

//			String webappname = "/corporate";
//			IProject project = root.getProject("corporate");
//			File file = null;
//			if (project == null || !project.exists() || !project.isAccessible() || !project.isOpen()) {
//				project = root.getProject("marcablanca-service-web");
//				webappname = "/marcablanca-service-web";
//			}  
//			if (project == null || !project.exists() || !project.isAccessible() || !project.isOpen()) {
//				project = root.getProject("marcablanca");
//				webappname = "/marcablanca";
//			}
//			String port ="8080";
//			String httpsPort ="8443";
//			String serverPort = "8208";
//			launchTomcat(project, webappname, port, httpsPort, serverPort, "", "");
		} catch (Exception e) {
			Activator.showException(e);
		}
	}

	public static String[] getExclusions(Properties props) {
		String[] result = props.getProperty("exclusions", "").split(",");
		if (result != null) {
			for (int i = 0; i< result.length; i++ ) {
				result[i] = result[i].replace(':', '/');
				result[i] = result[i].replace('.', '/');
			}
		}
		return result;
	}

	public static void launchTomcat(IProject project, String webappname, String port, String httpsPort,
			String serverPort, String docbase, String contextContent, String jvmArgs, String tomcatVersion, String[] exclusions)
			throws CoreException, IOException, JavaModelException {
		if ("9".equals(tomcatVersion)) {
			LaunchTomcat9.launchTomcat(project, webappname, port, httpsPort, serverPort, docbase, contextContent, jvmArgs);
		} else {
			if ("8".equals(tomcatVersion)) {
				LaunchTomcat8.launchTomcat(project, webappname, port, httpsPort, serverPort, docbase, contextContent, jvmArgs, exclusions);
			}
		}
	}

	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub

	}

}
