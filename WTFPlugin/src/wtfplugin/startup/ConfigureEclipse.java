package wtfplugin.startup;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wtfplugin.Activator;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.internal.variables.ValueVariable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;

public class ConfigureEclipse implements IStartup {
	
	
	public static final String PERFORM_SAVE_ACTIONS_PREFERENCE = "editor_save_participant_"
		+ org.eclipse.jdt.internal.corext.fix.CleanUpPostSaveListener.POSTSAVELISTENER_ID;
	private static MultiStatus infoFinal;
	
	// codigos de error de chequeos
	private static final Integer LTO_OK = 0; // todo ok
	private static final Integer LTO_ERROR = 1; // se puede usar, pero puede andar mal, no tiene java_1.5, java_1.6, pd_location no existe, error de encoding.
	private static final Integer LTO_FATAL = 2; // no es posible usar el entorno, no hay jvm 1.5, tomcat home no esta
	
	private static Integer ltoCompliant = LTO_OK;

	public static void main(String[] args) {
		(new ConfigureEclipse()).earlyStartup();
	}
	
	public void earlyStartup() {
			configureEclipse();
	}

	private void configureEclipse() {
		
		new Thread() {
			@Override
			public void run() {
				performChecks();
			}
		}.start();
	}
	
	public void performChecks() {
		final NullProgressMonitor monitor = new NullProgressMonitor();
			List<String[]> errors = new ArrayList<String[]>();
				String tomcatHome = System.getProperty("tomcat6_home", "");
				try {
					
					/* final Map<String, String> cleanupPreferences = new HashMap<String, String>( JavaPlugin
			            .getDefault()
			            .getCleanUpRegistry()
			            .getDefaultOptions(
			            		ICleanUp.DEFAULT_SAVE_ACTION_OPTIONS)
			            .getMap());*/
					
				final Map<String, String> cleanupPreferences = new HashMap<String, String>();
			    cleanupPreferences.put(org.eclipse.jdt.internal.corext.fix.CleanUpConstants.ORGANIZE_IMPORTS,
			        CleanUpOptions.TRUE);
			    cleanupPreferences.put(org.eclipse.jdt.internal.corext.fix.CleanUpConstants.ADD_MISSING_ANNOTATIONS_OVERRIDE,
				        CleanUpOptions.TRUE);
			    cleanupPreferences.put(org.eclipse.jdt.internal.corext.fix.CleanUpConstants.REMOVE_UNNECESSARY_CASTS,
				        CleanUpOptions.TRUE);
//			    cleanupPreferences.put(org.eclipse.jdt.internal.corext.fix.CleanUpConstants.FORMAT_REMOVE_TRAILING_WHITESPACES,
//				        CleanUpOptions.TRUE);
			    cleanupPreferences.put(org.eclipse.jdt.internal.corext.fix.CleanUpConstants.CONTROL_STATEMENTS_USE_BLOCKS,
				        CleanUpOptions.TRUE);
			   // System.out.println(cleanupPreferences);
			    
//			    cleanupPreferences.put(org.eclipse.jdt.internal.corext.fix.CleanUpConstants.CLEANUP_ON_SAVE_ADDITIONAL_OPTIONS,
//			        CleanUpOptions.TRUE);
			    
			    new InstanceScope().getNode(JavaUI.ID_PLUGIN).putBoolean(PERFORM_SAVE_ACTIONS_PREFERENCE, true);
			    org.eclipse.jdt.internal.corext.fix.CleanUpPreferenceUtil.saveSaveParticipantOptions(new InstanceScope(),
			        cleanupPreferences);
			    
					boolean isTomcatHomeValid = false;
					// defino variable tomcat home
					if (!isValidTomcatHomeVariable()) {
						if (isValidTomcatHome(tomcatHome)) {
							JavaCore.setClasspathVariable("TOMCAT6_HOME", new Path(tomcatHome), monitor);
							isTomcatHomeValid = true;
						} else {
							if (ltoCompliant < LTO_ERROR) {
								ltoCompliant = LTO_ERROR;
							}
							errors.add(new String[] {"La variable TOMCAT6_HOME es invalida", "Defina en el eclipse.ini -Dtomcat6_home=XXX"});
						}
					}else {
						isTomcatHomeValid = true;
						IPath tomcatHomeVar = JavaCore.getClasspathVariable("TOMCAT6_HOME");
						tomcatHome = tomcatHomeVar.toString();
					}
					// defino el string subs tomcat home
					if (isTomcatHomeValid) {
						if (VariablesPlugin.getDefault().getStringVariableManager().getValueVariable("TOMCAT6_HOME") == null) {
							ValueVariable arr[] = new ValueVariable[1];
							arr[0] = new ValueVariable("TOMCAT6_HOME","variable tomcat home de lto", false, tomcatHome);
							VariablesPlugin.getDefault().getStringVariableManager().addVariables(arr);
						}
					}
					
					if (!errors.isEmpty()) { // si hay errores los muestro
							final String PID = Activator.PLUGIN_ID;
						   final MultiStatus info = new MultiStatus(PID, 1, "Se han encontrado errores de seteos del eclipse para desarrollo de Corporate", null);
						   for (int i = 0; i < errors.size(); i++) {
							   info.add(new Status(IStatus.ERROR, PID, 1, errors.get(i)[0] + " - " + errors.get(i)[1], null));
						   }
						   infoFinal = info;
						   Display.getDefault().syncExec( new Runnable() {
								public void run() {
									// Aca setear en un label contribution al status bar el tipo de error, que on click lo vuelva a mostrar, con un icono de ok, error, fatal
								   ErrorDialog.openError(Activator.getDefault().getWorkbench()
											.getWorkbenchWindows()[0].getShell(), "Errores de workspace de Corporate", null, infoFinal);
								}
						  });
					} 
				} catch (CoreException e) {
					Activator.showException(e);
				} finally {
					monitor.done();
				}
			}

private boolean isValidPdLocation(String pdLocationFile) {
	if (StringUtils.isEmpty(pdLocationFile)) {
		return false;
	}
	File f = new File(pdLocationFile);
	return f != null && f.exists();
}

private boolean isValidTomcatHomeVariable() {
	IPath tomcatHome = JavaCore.getClasspathVariable("TOMCAT6_HOME");
	if (tomcatHome == null) {
		return false;
	}
	File file = new File(tomcatHome.toString() + "/lib/servlet-api.jar");
	return file != null && file.exists();
}

private boolean isValidTomcatHome(String tomcatHome) {
	if (StringUtils.isEmpty(tomcatHome)) {
		return false;
	}
	File file = new File(tomcatHome + "/lib/servlet-api.jar");
	return file != null && file.exists();
}


}
