package wtfplugin.builder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.corext.util.CollectionsUtil;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;

import wtfplugin.Activator;
import wtfplugin.statusbar.WTFStatusBar;


/*
 *  Este builder no posee ui de agregado, se debe modificar el .project en forma manual agregando:
 * <buildSpec>
		<buildCommand>
			<name>WTFPlugin.confBuilder</name>
			<arguments>
			</arguments>
		</buildCommand>
	</buildSpec>
 */
public class ConfigurationBuilder extends IncrementalProjectBuilder {

	// este hash va a tener una entrada por archivo con todos los resource del
	// archivo
	private static Set<EnvironmentBundles> globalConf = new HashSet<EnvironmentBundles>();
	private static Map<String, Set<EnvironmentBundles>> envConf = new HashMap<String, Set<EnvironmentBundles>>();
	
	private boolean propertiesModified = false;

	class SampleDeltaVisitor implements IResourceDeltaVisitor {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse
		 * .core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				checkFile(resource, delta.getKind());
				break;
			case IResourceDelta.REMOVED:
				checkFile(resource, delta.getKind());
				break;
			case IResourceDelta.CHANGED:
				checkFile(resource, delta.getKind());
				break;
			}
			return true;
		}
	}
	
	class SampleResourceVisitor implements IResourceVisitor {
		
		private IProgressMonitor monitor;
	
		public SampleResourceVisitor(IProgressMonitor monitor) {
			super();
			this.monitor = monitor;
		}

		public boolean visit(IResource resource) {
			if (monitor.isCanceled()) {
				return false;
			}
			try {
				checkFile(resource, IResourceDelta.ADDED);
				return true;
		} catch (Exception e) {
			throw new RuntimeException("Error durante el build, cancelado");
		}
		}
	}

	void checkFile(IResource resource, int resourceDelta) {
		try {
			if (resource instanceof IFile) {
				IFile file = (IFile)resource;
				if (!isTarget(file) && !isBin(file) &&  file.getName().endsWith("properties") && !file.getName().startsWith("test")) {
					String segments[] = file.getFullPath().segments();
					if (segments.length > 4) {
						String fileName = segments[segments.length - 1];
						String env = segments[segments.length - 2];
						String app = segments[segments.length - 3];
						String conf = segments[segments.length - 4];
						
						if (env.equals("app")) {
//						esto es global
							globalConf.add(new EnvironmentBundles(file, "app", readFileLines(file)));
							System.out.println("global " + file.getFullPath().toString());
						}
						if (app.equals("app")) {
							// esto no es global
							Set<EnvironmentBundles> envs = envConf.get(env);
							if (envs == null) {
								envs = new HashSet<EnvironmentBundles>();
								envConf.put(env, envs);
							}
							envs.add(new EnvironmentBundles(file, env, readFileLines(file)));
							System.out.println("env " + env + " - " + file.getFullPath().toString());
						
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean isTarget(IFile file) {
		for (String s : file.getFullPath().segments()) {
			if (s.equals("target")) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isBin(IFile file) {
		for (String s : file.getFullPath().segments()) {
			if (s.equals("bin")) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		try {
			if (kind == FULL_BUILD) {
				globalConf = new HashSet<EnvironmentBundles>();
				envConf.clear();
				getProject().accept(new SampleResourceVisitor(monitor));
				performChecks();
			} else {
				propertiesModified = false;
				IResourceDelta delta = getDelta(getProject());
				incrementalBuild(delta, monitor);
				if (propertiesModified) {
					performChecks();
				}
			}
		} catch (Exception e) {
			Activator.showException(e);
		}
		return null;
	}

	private void performChecks() {
		List<String> fullToCompare = new ArrayList<String>();
		Set<String> globalConfKeys = new HashSet<String>();
		for (EnvironmentBundles env :  globalConf) {
			globalConfKeys.addAll(env.getProps().keySet());
			for (Map.Entry<String, String> entry : env.getProps().entrySet()) {
				if (hasEnvPrefix(entry.getKey())) {
					fullToCompare.add("La property " + entry.getKey() + " es por entorno " + entry.getValue());
				}
			}
		}
		Set<String> appConfKeys = new HashSet<String>();
		for (Map.Entry<String, Set<EnvironmentBundles>> entry : envConf.entrySet()) {
			for (EnvironmentBundles e : entry.getValue()) {
				appConfKeys.addAll(e.getProps().keySet());
				for (Map.Entry<String, String> entry1 : e.getProps().entrySet()) {
					if (hasEnvPrefix(entry1.getKey())) {
						fullToCompare.add("La property " + entry1.getKey() + " es por entorno " + entry1.getValue());
					}
				}
			}
		}
		for (Map.Entry<String, Set<EnvironmentBundles>> entry : envConf.entrySet()) {
			Set<String> currAppConfKeys = new HashSet<String>();
			for (EnvironmentBundles e : entry.getValue()) {
				currAppConfKeys.addAll(e.getProps().keySet());
			}
			if (!currAppConfKeys.containsAll(appConfKeys)) {
				Set<String> faltantes= new HashSet<String>(appConfKeys);
				faltantes.removeAll(currAppConfKeys);
				faltantes.removeAll(globalConfKeys);
				if (!faltantes.isEmpty()) {
					fullToCompare.add("Al ambiente " + entry.getKey() + " le faltan " + faltantes);
				}
			}
			Collection<String> inter = CollectionUtils.intersection(currAppConfKeys, globalConfKeys);
			if (!inter.isEmpty()) {
				fullToCompare.add("El ambiente " + entry.getKey() + " contiene claves que colisionan con las generales " + inter);
			}
		}
		Set<EnvironmentBundles> prod = getProductionProperties();
		Set<EnvironmentBundles> prodOnly = getProductionOnlyProperties();
		for (EnvironmentBundles env : prod) {
			for (Map.Entry<String, String> entry : env.getProps().entrySet()) {
				if (entry.getValue().startsWith("http://")) {
					char c = entry.getValue().charAt(7);
					if (!StringUtils.isNumeric(String.valueOf(c))) {
						if (!contains(prodOnly, entry.getKey())) {
							fullToCompare.add("Posible referencia incorrecta por nombre en prod: " + entry.getKey() + "=" + entry.getValue());
						}
					}
				}
			}
		}
		for (EnvironmentBundles env : prodOnly) {
			for (Map.Entry<String, String> entry : env.getProps().entrySet()) {
				if (entry.getValue().startsWith("http://")) {
					char c = entry.getValue().charAt(7);
					if (!StringUtils.isNumeric(String.valueOf(c))) {
						fullToCompare.add("Posible referencia incorrecta por nombre en prod: " + entry.getKey() + "=" + entry.getValue());
					}
				}
			}
		}
		
		if (!fullToCompare.isEmpty()) {
			final String PID = Activator.PLUGIN_ID;
			   final MultiStatus info = new MultiStatus(PID, 1, "Se han encontrado errores de configuracion", null);
			   for (String key : fullToCompare) {
					 info.add(new Status(IStatus.ERROR, PID, 1, key, null));
				}
			   final MultiStatus infoFinal = info;
			   Display.getDefault().syncExec( new Runnable() {
					public void run() {
						// Aca setear en un label contribution al status bar el tipo de error, que on click lo vuelva a mostrar, con un icono de ok, error, fatal
					   ErrorDialog.openError(Activator.getDefault().getWorkbench()
								.getWorkbenchWindows()[0].getShell(), "Errores de conf", null, infoFinal);
					}
			  });
		}
	}
	

	private boolean contains(Set<EnvironmentBundles> prodOnly, String key) {
		for (EnvironmentBundles e : prodOnly) {
			for (Map.Entry<String, String> entry : e.getProps().entrySet()) {
				if (entry.getKey().equals(key)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean hasEnvPrefix(String key) {
		if (key.startsWith("localhost.")) {
			return true;
		}
		if (key.startsWith("ic.")) {
			return true;
		}
		if (key.startsWith("rc.")) {
			return true;
		}
		if (key.startsWith("prod.")) {
			return true;
		}
		return false;
	}

	private Set<EnvironmentBundles> getProductionProperties() {
		Set<EnvironmentBundles> result = new HashSet<EnvironmentBundles>();
		result.addAll(globalConf);
		return result;
	}
	
	private Set<EnvironmentBundles> getProductionOnlyProperties() {
		Set<EnvironmentBundles> result = new HashSet<EnvironmentBundles>();
		result.addAll(envConf.get("prod"));
		return result;
	}

	public static Map<String, String> readFileLines(IFile file) throws Exception {
		Map<String, String> result = new HashMap<String, String>();
		Set<String> repeated = new HashSet<String>();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file.getContents(),"ISO-8859-1"));
		String line;
		try {
			line = null;
			while ((line = bufferedReader.readLine()) != null) {
				if (line.contains("=") && !line.startsWith("#")) {
					String rb[] = line.split("=");
					rb[0] = rb[0].trim();
					if (rb.length == 1)  {
						if (result.containsKey(rb[0])) {
							repeated.add(rb[0]);
						}
						result.put(rb[0], "");
					} else {
						if (result.containsKey(rb[0])) {
							repeated.add(rb[0]);
						}
						result.put(rb[0], rb[1]);
					}
				}
			}
//			if (!repeated.isEmpty()) {
//				final String PID = Activator.PLUGIN_ID;
//				final String fileName = file.getName(); 
//				   final MultiStatus info = new MultiStatus(PID, 1, "Claves repetidas en el archivo" + fileName, null);
//				   for (String key : repeated) {
//						 info.add(new Status(IStatus.ERROR, PID, 1, key, null));
//					}
//				   final MultiStatus infoFinal = info;
//				   Display.getDefault().syncExec( new Runnable() {
//						public void run() {
//							// Aca setear en un label contribution al status bar el tipo de error, que on click lo vuelva a mostrar, con un icono de ok, error, fatal
//						   ErrorDialog.openError(Activator.getDefault().getWorkbench()
//									.getWorkbenchWindows()[0].getShell(), "Errores de rb para " + fileName, null, infoFinal);
//						}
//				  });
//			}
		} finally {
			bufferedReader.close();
		}
		return result;
	}

	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.
		delta.accept(new SampleDeltaVisitor());
	}

	public static void addMarker(IFile file, String message, int lineNumber, int severity, String markerType) throws CoreException {
			if (file.exists()) {
				IMarker marker = file.createMarker(markerType);
				marker.setAttribute(IMarker.MESSAGE, message);
				marker.setAttribute(IMarker.SEVERITY, severity);
				if (lineNumber == -1) {
					lineNumber = 1;
				}
				marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			}
	}

//	public void buillAll() throws Exception {
//		for (String path : resourceBundleFiles) {
//			buildFile(path);
//		}
//	}

	private void buildFile(IFile file) throws CoreException, Exception {
		Map<String, String> fileLines = readFileLines(file);
		
	}


	/*
	private boolean isRepeated(String lan, String country, String context, String key) {
		for (String st : resourceBundleFiles) {
			if (resourceBundles.get(st).get(lan, country, context, key) != null) {
				return true;
			}
		}
		return false;
	}*/

}
