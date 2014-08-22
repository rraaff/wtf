package wtfplugin.builder.classpath;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.internal.resources.Marker;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;

public class ClasspathImportVerifier {

	public static void verifyImports(IProject project, IJavaProject javaProject) throws Exception {
		project.deleteMarkers(ClasspathVerifier.INVALID_MODEL_USE, false, IResource.DEPTH_INFINITE);
		IPackageFragment[] packages = javaProject.getPackageFragments();
		for (IPackageFragment mypackage : packages) { // para cada package
			if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) { 
				for (ICompilationUnit unit : mypackage.getCompilationUnits()) { // para cada file
					checkCompilationUnitImports(project, unit);
				}
			}
		}
	}

	protected static void checkCompilationUnitImports(IProject project, ICompilationUnit unit) throws JavaModelException, Exception, CoreException {
		for (IImportDeclaration importDeclaration : unit.getImports()) { // para cada import
			String importLine = importDeclaration.getElementName();
//			System.out.println(importLine);
			//Pattern pattern = Pattern.compile(".*\\.domain\\.[^\\.]*$");
			//Matcher m = pattern.matcher(importLine);
			//if (m.find()) {
			if (importLine.contains("com.marcablanca.corporate.bo.service.domain")) {
				// Si es una clase de dominio
				IPackageDeclaration p = unit.getPackageDeclarations()[0];
				String packageName = p.getElementName();
				if (packageName.endsWith(".domain")) {
					return;
				}
				if (packageName.endsWith(".dao")) {
					return;
				}
				if (packageName.endsWith(".test")) {
					return;
				}
				if (packageName.contains(".test")) {
					return;
				}
				if (packageName.endsWith(".dao.impl")) {
					return;
				}
				if (packageName.endsWith(".service")) {
					return;
				}
				if (packageName.endsWith(".service.impl")) {
					return;
				}
				ISourceRange sourceRange = importDeclaration.getSourceRange();
				int lineNumber = getLineNumberFor(sourceRange.getOffset(), readFileContents((IFile)unit.getResource()));
				ClasspathVerifier.addMarker(unit.getResource(), "Invalid import of " + importLine, lineNumber, Marker.SEVERITY_ERROR, ClasspathVerifier.INVALID_MODEL_USE);
			}
//			String referenced = WorkspaceMap.getClassLocation(project, importLine);
//			if (referenced != null) {
//				if (!Module.isValidModuleRelation(project.getName(), referenced)) {
//					ISourceRange sourceRange = importDeclaration.getSourceRange();
//					int lineNumber = getLineNumberFor(sourceRange.getOffset(), readFileContents((IFile)unit.getResource()));
//					ClasspathVerifier.addMarker(unit.getResource(), "Invalid import of " + importLine + " (" + referenced + ")", lineNumber, Marker.SEVERITY_ERROR, ClasspathVerifier.INVALID_MODULE_RELATION_MARKER_TYPE);
//				}
//			}
		}
	}

	public static String readFileContents(IFile file) throws Exception {
		InputStream inputStream = null;
		try {
			inputStream = file.getContents();
			int available = inputStream.available();
			byte arr[] = new byte[available];
			inputStream.read(arr);
			return new String(arr);
		} finally  {
			closeStreamQuitly(inputStream);
		}
	}
	
	private static void closeStreamQuitly(InputStream is) {
		try {
			if (is != null) {
				is.close();
			}
		} catch (Exception e) {
		}
	}
	
	public static int getLineNumberFor(int i, String content) {
		if (i == -1) {
			return 1;
		}
		return org.apache.commons.lang.StringUtils.countMatches(content.substring(0,i), "\n") + 1;
	}
}
