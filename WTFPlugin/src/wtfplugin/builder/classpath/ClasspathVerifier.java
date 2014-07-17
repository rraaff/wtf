package wtfplugin.builder.classpath;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/*
 *  Este builder no posee ui de agregado, se debe modificar el .project en forma manual agregando:
 * <buildSpec>
		<buildCommand>
			<name>WTFPlugin.classpathVerifier</name>
			<arguments>
			</arguments>
		</buildCommand>
	</buildSpec>
 */
public class ClasspathVerifier extends IncrementalProjectBuilder {

	public static final String INVALID_MODEL_USE = "WTFPlugin.invalidPersistentClassUse";

//	class ProjectMetadataDeltaVisitor implements IResourceDeltaVisitor {
//		/*
//		 * (non-Javadoc)
//		 * 
//		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse .core.resources.IResourceDelta)
//		 */
//		public boolean visit(IResourceDelta delta) throws CoreException {
//			IProject project = ClasspathVerifier.this.getProject();
//			verifyProjectRelations(project);
//			return false;
//		}
//	}
	
	class ClassDeltaVisitor implements IResourceDeltaVisitor {
		private boolean cancelled = false;
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			if (cancelled) {
				return false;
			}
			try {
				IResource resource = delta.getResource();
				switch (delta.getKind()) {
				case IResourceDelta.ADDED:
					checkClass(resource.getProject(), resource, delta.getKind());
					break;
				case IResourceDelta.REMOVED:
					break;
				case IResourceDelta.CHANGED:
					checkClass(resource.getProject(), resource,delta.getKind());
					break;
				}
				return true;
			} catch (Exception e) {
				wtfplugin.Activator.showException(e);
				return true;
			}
		}
	}

	private void checkClass(IProject project, IResource resource, int kind) throws JavaModelException, CoreException, Exception {
		if (resource.getName().endsWith(".java")) {
			resource.deleteMarkers(ClasspathVerifier.INVALID_MODEL_USE, false, IResource.DEPTH_INFINITE);
			IJavaElement javaElement = JavaCore.create(resource);
			ClasspathImportVerifier.checkCompilationUnitImports(project, (ICompilationUnit)javaElement);
		}
	}

	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		IProject project = ClasspathVerifier.this.getProject();
		IJavaProject javaProject = JavaCore.create(project);
		if (kind == FULL_BUILD) { // si es full build, solo hago el build del projecto actual
			// aca tengo que verificar las clases del projecto
			try {
				ClasspathImportVerifier.verifyImports(project, javaProject);
			} catch (Exception e) {
				wtfplugin.Activator.showException(e);
			}
		} else { // solo hago el build del delta
			IResourceDelta delta = getDelta(getProject());
			incrementalClassBuild(delta, monitor);
		}
		return new IProject[] { this.getProject() };
	}

	private void incrementalClassBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		delta.accept(new ClassDeltaVisitor());
	}

	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		delta.accept(new ClassDeltaVisitor());
	}

	public static void addMarker(IResource file, String message, int lineNumber, int severity, String markerType) throws CoreException {
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

}
