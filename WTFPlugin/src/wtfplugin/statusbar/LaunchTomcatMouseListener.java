package wtfplugin.statusbar;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

import wtfplugin.Activator;
import wtfplugin.actions.LaunchTomcat;
import wtfplugin.preferences.WTFPreferences;

public class LaunchTomcatMouseListener extends MouseAdapter {
	private IProject project; 
	
	public LaunchTomcatMouseListener(IProject project) {
		super();
		this.project = project;
	}
	
	@Override
	public void mouseUp(MouseEvent e) {
		InputStream is = null;
		try {
			IFile file = project.getFile(".wtf");
			Properties props = new Properties();
			is = file.getContents();
			props.load(is);
			LaunchTomcat.launchTomcat(project, props.getProperty("contextPath", "/" + project.getName()), props.getProperty("port", "8080"), 
					props.getProperty("httpsPort", "8443"), props.getProperty("serverport", "8208"), 
					props.getProperty("docbase", "/scr/main/webapp"),props.getProperty("contextcontent",""), 
					props.getProperty("jvmargs",""), props.getProperty("tomcatVersion", WTFPreferences.getTomcatVersion()),
					LaunchTomcat.getExclusions(props));
		} catch (JavaModelException e1) {
			Activator.showException(e1);
		} catch (CoreException e1) {
			Activator.showException(e1);
		} catch (IOException e1) {
			Activator.showException(e1);
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