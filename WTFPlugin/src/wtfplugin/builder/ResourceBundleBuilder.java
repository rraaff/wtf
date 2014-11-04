package wtfplugin.builder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.eclipse.jdt.internal.ui.compare.ResizableDialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;

import wtfplugin.Activator;
import wtfplugin.statusbar.WTFStatusBar;


/*
 *  Este builder no posee ui de agregado, se debe modificar el .project en forma manual agregando:
 * <buildSpec>
		<buildCommand>
			<name>WTFPlugin.rbBuilder</name>
			<arguments>
			</arguments>
		</buildCommand>
	</buildSpec>
 */
public class ResourceBundleBuilder extends IncrementalProjectBuilder {

	// este hash va a tener una entrada por archivo con todos los resource del
	// archivo
	private static Set<String> usedResourceBundles = new HashSet<String>();
	private static Map<String, LanguageBundles> resourceBundles = new HashMap<String, LanguageBundles>();
	
	private static Set<String> suspectedMisssing = new HashSet<String>();
	private static Set<String> suspectedExtras = new HashSet<String>();
	
	private boolean propertiesModified = false;
	
	private boolean fullBuild = false;
	private boolean alreadyShown = false;

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
		if (resource instanceof IFile) {
			IFile file = (IFile)resource;
			if (!isTarget(file)) {
				if (file.getName().endsWith(".java")) {
					extractRBsFromJava(file);
				} 
				if (file.getName().endsWith(".js")) {
					extractRBsFromJS(file);
				} 
				if (file.getName().endsWith(".ftl")) {
					extractRBsFromFTL(file);
				} 
				if (file.getParent().getName().equals("locale")) {
					if (file.getName().startsWith("messages")) {
						try {
							buildFile(file);
							propertiesModified = true;
						} catch (Exception e) {
							Activator.showException(e);
						}
					}
				}
			}
		}
	}

	/*
	private String getResourceBundleFor(IFile resource) {
		for (String st : resourceBundleFiles) {
			if (st.endsWith(resource.getName())) {
				return st;
			}
		}
		return null;
	}*/
	
	private void extractRBsFromJava(IFile file) {
		InputStream inputStream = null;
		try {
			inputStream = file.getContents();
			String fileContent = IOUtils.toString(inputStream);
			Pattern pattern = Pattern.compile("getMessage\\s*\\(\\s*\\\"([^\"]*)\"");
			Matcher matcher = pattern.matcher(fileContent);
			while (matcher.find()) {
				usedResourceBundles.add(matcher.group(1));
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			if (inputStream != null) {
				closeSilent(inputStream);
			}
		}
	}
	private void closeSilent(InputStream inputStream) {
		try {
			inputStream.close();
		} catch (Exception e) {
		}
		
	}

	private void extractRBsFromJS(IFile file) {
		InputStream inputStream = null;
		try {
			inputStream = file.getContents();
			String fileContent = IOUtils.toString(inputStream);
			Pattern pattern = Pattern.compile("getI18nMessage\\s*\\(\\s*\\\"([^\"]*)\"");
			Matcher matcher = pattern.matcher(fileContent);
			while (matcher.find()) {
				usedResourceBundles.add(matcher.group(1));
			}
			pattern = Pattern.compile("getI18nMessage\\s*\\(\\s*'([^\']*)'");
			matcher = pattern.matcher(fileContent);
			while (matcher.find()) {
				usedResourceBundles.add(matcher.group(1));
			}
			
			pattern = Pattern.compile("translate\\s*\\(\\s*\\\"([^\"]*)\"");
			matcher = pattern.matcher(fileContent);
			while (matcher.find()) {
				usedResourceBundles.add(matcher.group(1));
			}
			
			pattern = Pattern.compile("translate\\s*\\(\\s*'([^\']*)'");
			matcher = pattern.matcher(fileContent);
			while (matcher.find()) {
				usedResourceBundles.add(matcher.group(1));
			}
			
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			if (inputStream != null) {
				closeSilent(inputStream);
			}
		}
	}
	private void extractRBsFromFTL(IFile file) {
		InputStream inputStream = null;
		try {
			inputStream = file.getContents();
			String fileContent = IOUtils.toString(inputStream);
			Pattern pattern = Pattern.compile("message\\s+'([^']*)'");
			Matcher matcher = pattern.matcher(fileContent);
			while (matcher.find()) {
				usedResourceBundles.add(matcher.group(1));
			}
			pattern = Pattern.compile("message\\s+\\\"([^\\\"]*)\\\"");
			matcher = pattern.matcher(fileContent);
			while (matcher.find()) {
				usedResourceBundles.add(matcher.group(1));
			}
			
			pattern = Pattern.compile("'([^']*)'\\s*\\|\\s*translate");
			matcher = pattern.matcher(fileContent);
			while (matcher.find()) {
				usedResourceBundles.add(matcher.group(1));
			}
			
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			if (inputStream != null) {
				closeSilent(inputStream);
			}
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

	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		try {
			if (kind == FULL_BUILD) {
				fullBuild = true;
				usedResourceBundles = new HashSet<String>();
				resourceBundles.clear();
				getProject().accept(new SampleResourceVisitor(monitor));
				performChecks();
				alreadyShown = true;
			} else {
				fullBuild = false;
				propertiesModified = false;
				IResourceDelta delta = getDelta(getProject());
				incrementalBuild(delta, monitor);
				if (propertiesModified) {
					performChecks();
				}
				alreadyShown = true;
			}
		} catch (Exception e) {
			Activator.showException(e);
		}
		return null;
	}

	private void performChecks() {
		// genero las claves de todos los rbs
		Set<String> fullKeys = new HashSet<String>();
		for (LanguageBundles lb : resourceBundles.values()) {
			fullKeys.addAll(lb.getBundles().keySet());
			for (CountryBundles cb : lb.getCountries().values()) {
				fullKeys.addAll(cb.getBundles().keySet());
			}
		}
		Map<String, String> exampleValue =new HashMap<String, String>();
		for (String key : fullKeys) {
			String value = null;
			Iterator<LanguageBundles> lang = resourceBundles.values().iterator();
			while(StringUtils.isEmpty(value) && lang.hasNext()) {
				LanguageBundles bundle = lang.next();
				value = bundle.getRB(key);
				if (!StringUtils.isEmpty(value)) {
					exampleValue.put(key, value);
				}
			}
			if (StringUtils.isEmpty(value)) {
				exampleValue.put(key, "");
			}
		}
		List<String> errors = new ArrayList<String>();
		boolean missingBundles= false;
		// Comparo las keys de cada pais con las keys totales
		for (LanguageBundles lb : resourceBundles.values()) { // para cada lenguage
			for (CountryBundles cb : lb.getCountries().values()) { // para cada pais
				Set<String> countryKeys = lb.getCountryKeys(cb);
				Set<String> missing = new HashSet<String>();
				missing.addAll(fullKeys);
				missing.removeAll(countryKeys);
				Set<String> revisionPending = new HashSet<String>();
				revisionPending.addAll(lb.getCountryPendingKeys(cb));
				final String lbF = lb.getLanguage();
				final String cbF = cb.getCountry();
				if (!missing.isEmpty()) {
					missingBundles= true;
					errors.add("Los siguientes de rb faltan " + lbF + " " + cbF);
					for (String st : missing) {
						errors.add(st + "=" + exampleValue.get(st));
					}
					errors.add("------------------------------------------------------------------------------------------------------------------------------------------------------------");
				}
				if (!revisionPending.isEmpty()) {
					errors.add("Los siguientes de rb deben revisarse " + lbF + " " + cbF);
					for (String st : revisionPending) {
						errors.add(st + "=" + exampleValue.get(st));
					}
					errors.add("------------------------------------------------------------------------------------------------------------------------------------------------------------");
				}
			}
		}
		if (fullBuild || !alreadyShown || missingBundles) {
			if (!errors.isEmpty()) {
				final String PID = Activator.PLUGIN_ID;
				final MultiStatus info = new MultiStatus(PID, 1, "Se han encontrado errores de rb", null);
				for (String key : errors) {
					info.add(new Status(IStatus.ERROR, PID, 1, key, null));
				}
				final MultiStatus infoFinal = info;
				Display.getDefault().syncExec( new Runnable() {
					public void run() {
						// Aca setear en un label contribution al status bar el tipo de error, que on click lo vuelva a mostrar, con un icono de ok, error, fatal
						ErrorDialog.openError(Activator.getDefault().getWorkbench()
								.getWorkbenchWindows()[0].getShell(), "Errores de rb", null, infoFinal);
					}
				});
			}
		}
		// comparo full keys con lo que encontre, esto me da los que tengo de mas
		Set<String> fullToUsed = new HashSet<String>();
		for (String st : fullKeys) {
			if (!st.endsWith("_validator")) {
				if (!st.startsWith("TAXES.")) {
					if (!st.startsWith("countries.")) {
						if (!st.startsWith("corporate.taxCondition")) {
							if (!st.startsWith("dynamic")) {
								if (!st.contains(".dyn")) {
									fullToUsed.add(st);
								}
							}
						}
					}
				}
			}
		}
		fullToUsed.removeAll(usedResourceBundles);
		suspectedExtras = fullToUsed;
		
		// comparo lo que encontre con lo existente, esto me da los que me faltan
		Set<String> usedToFull= new HashSet<String>();
		for (String st : usedResourceBundles) {
			if (!st.contains("${")) {
				usedToFull.add(st);
			}
		}
		usedToFull.removeAll(fullKeys);
		suspectedMisssing = usedToFull;
		
		if (!suspectedExtras.isEmpty()) {
			 Display.getDefault().syncExec( new Runnable() {
				public void run() {
					// Aca setear en un label contribution al status bar el tipo de error, que on click lo vuelva a mostrar, con un icono de ok, error, fatal
				   WTFStatusBar.setRBCheck(WTFStatusBar.RB_WARNING);
				}
			});
		}
		if (!suspectedMisssing.isEmpty()) {
			Display.getDefault().syncExec( new Runnable() {
				public void run() {
					// Aca setear en un label contribution al status bar el tipo de error, que on click lo vuelva a mostrar, con un icono de ok, error, fatal
				   WTFStatusBar.setRBCheck(WTFStatusBar.RB_ERROR);
				}
			});
		}
		/*
		LanguageBundles lang = resourceBundles.get("es");
		Set<String> esSet = new HashSet<String>(); 
		esSet.addAll(lang.getBundles().keySet());
		esSet.addAll(lang.getCountryBundle("AR").getBundles().keySet());
		
		LanguageBundles ptlang = resourceBundles.get("pt");
		Set<String> ptSet = new HashSet<String>(); 
		ptSet.addAll(ptlang.getBundles().keySet());
		ptSet.addAll(ptlang.getCountryBundle("BR").getBundles().keySet());
		
		Set<String> ptMissing = new HashSet<String>();
		ptMissing.addAll(esSet);
		ptMissing.removeAll(ptSet);
		Map<String, String> errors = new HashMap<String, String>();
		for (String key : ptMissing) {
			errors.put(key, lang.getRB(key));
		}
		if (!errors.isEmpty()) {
			
			final String PID = Activator.PLUGIN_ID;
			   final MultiStatus info = new MultiStatus(PID, 1, "Se han encontrado errores de rb para pt_BR", null);
			   for (Map.Entry<String, String> e : errors.entrySet()) {
					 info.add(new Status(IStatus.ERROR, PID, 1, e.getKey() + "=" + e.getValue(), null));
				}
			   final MultiStatus infoFinal = info;
			   Display.getDefault().syncExec( new Runnable() {
					public void run() {
						// Aca setear en un label contribution al status bar el tipo de error, que on click lo vuelva a mostrar, con un icono de ok, error, fatal
					   ErrorDialog.openError(Activator.getDefault().getWorkbench()
								.getWorkbenchWindows()[0].getShell(), "Errores de rb para BR", null, infoFinal);
					}
			  });
		}
		System.out.println(errors);*/
	}
	
	public static void showSuspectedErrors() {
		if (!suspectedMisssing.isEmpty()) {
			final String PID = Activator.PLUGIN_ID;
			   final MultiStatus info = new MultiStatus(PID, 1, "Lo siguientes rb posiblemente falten", null);
			   for (String key : suspectedMisssing) {
					 info.add(new Status(IStatus.ERROR, PID, 1, key, null));
				}
			   final MultiStatus infoFinal = info;
			   Display.getDefault().syncExec( new Runnable() {
					public void run() {
						// Aca setear en un label contribution al status bar el tipo de error, que on click lo vuelva a mostrar, con un icono de ok, error, fatal
					   ErrorDialog.openError(Activator.getDefault().getWorkbench()
								.getWorkbenchWindows()[0].getShell(), "Lo siguientes rb posiblemente falten", null, infoFinal);
					}
			  });
		}
		if (!suspectedExtras.isEmpty()) {
			final String PID = Activator.PLUGIN_ID;
			   final MultiStatus info = new MultiStatus(PID, 1, "Lo siguientes rb posiblemente sobren", null);
			   for (String key : suspectedExtras) {
					 info.add(new Status(IStatus.ERROR, PID, 1, key, null));
				}
			   final MultiStatus infoFinal = info;
			   Display.getDefault().syncExec( new Runnable() {
					public void run() {
						// Aca setear en un label contribution al status bar el tipo de error, que on click lo vuelva a mostrar, con un icono de ok, error, fatal
					   ErrorDialog.openError(Activator.getDefault().getWorkbench()
								.getWorkbenchWindows()[0].getShell(), "Lo siguientes rb posiblemente sobren", null, infoFinal);
					}
			  });
		}
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
			if (!repeated.isEmpty()) {
				final String PID = Activator.PLUGIN_ID;
				final String fileName = file.getName(); 
				   final MultiStatus info = new MultiStatus(PID, 1, "Claves repetidas en el archivo" + fileName, null);
				   for (String key : repeated) {
						 info.add(new Status(IStatus.ERROR, PID, 1, key, null));
					}
				   final MultiStatus infoFinal = info;
				   Display.getDefault().syncExec( new Runnable() {
						public void run() {
							// Aca setear en un label contribution al status bar el tipo de error, que on click lo vuelva a mostrar, con un icono de ok, error, fatal
						   ErrorDialog.openError(Activator.getDefault().getWorkbench()
									.getWorkbenchWindows()[0].getShell(), "Errores de rb para " + fileName, null, infoFinal);
						}
				  });
			}
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
		if (file.getName().contains("messages_es_")) {
			String country = file.getName().substring(file.getName().indexOf("messages_es_") + 12, file.getName().indexOf("messages_es_") + 14);
			LanguageBundles lan = getLanguageBundle("es");
			CountryBundles cb = lan.getCountryBundle(country);
			cb.getBundles().clear();
			cb.getBundles().putAll(fileLines);
		}
		if (file.getName().contains("messages_pt_")) {
			String country = file.getName().substring(file.getName().indexOf("messages_pt_") + 12, file.getName().indexOf("messages_pt_") + 14);
			LanguageBundles lan = getLanguageBundle("pt");
			CountryBundles cb = lan.getCountryBundle(country);
			cb.getBundles().clear();
			cb.getBundles().putAll(fileLines);
		}
		if (file.getName().contains("es.")) {
			LanguageBundles lb = getLanguageBundle("es");
			lb.getBundles().clear();
			lb.getBundles().putAll(fileLines);
		}
		if (file.getName().contains("pt.")) {
			LanguageBundles lb = getLanguageBundle("pt");
			lb.getBundles().clear();
			lb.getBundles().putAll(fileLines);
		}
	}

	public LanguageBundles getLanguageBundle(String lang) {
		if (!resourceBundles.containsKey(lang)) {
			resourceBundles.put(lang, new LanguageBundles(lang));
		}
		return resourceBundles.get(lang);
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
