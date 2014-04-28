package wtfplugin.views;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import swing2swt.layout.FlowLayout;
import wtfplugin.Activator;
import wtfplugin.statusbar.LaunchTomcatMouseListener;


/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class WTFLaunchView extends ViewPart {

	/**
	 * The constructor.
	 */
	public WTFLaunchView() {
		super();
	}

	@Override
	public void dispose() {
		super.dispose();
	}
	

	private void clearView() {}


	private void hookDoubleClickAction() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		parent.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
//		Button btnNewButton = new Button(parent, SWT.NONE);
//		btnNewButton.setText("New Button");
		Button b = null ;
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();
		for (IProject pro : projects) {
			if (pro.isOpen()) {
				try {
					if (pro.hasNature(JavaCore.NATURE_ID)) {
						IFile file = pro.getFile(".wtf");
						if (file != null && file.exists()) {
								b = new Button(parent, SWT.NONE);
								RowData gd_btnVersionComercial = new RowData();
								gd_btnVersionComercial.height = 24;
								b.setLayoutData(gd_btnVersionComercial);
								b.setText(pro.getName());
								b.addMouseListener(new LaunchTomcatMouseListener(pro));
							}
						}
				} catch (CoreException e1) {
					Activator.showException(e1);
				}
			}
		}
		
	}


	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
	}
}