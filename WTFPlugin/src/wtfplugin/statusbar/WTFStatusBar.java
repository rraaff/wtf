package wtfplugin.statusbar;


import java.io.IOException;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

import wtfplugin.Activator;
import wtfplugin.actions.LaunchTomcat;

public class WTFStatusBar extends WorkbenchWindowControlContribution {

	private static WTFStatusBar instance;
	private CLabel label;
	private static MultiStatus error;
	private Composite comp;

	public WTFStatusBar() {
		instance = this;
	}

	public WTFStatusBar(String id) {
		super(id);
		instance = this;
	}
	

	@Override
	protected Control createControl(Composite parent) {
		comp = new Composite(parent, SWT.NONE);
		comp.setSize(120, 16);
		// Give some room around the control
		
		RowLayout rowLayout = new RowLayout();
		rowLayout.wrap = false;
		rowLayout.pack = true;
		rowLayout.justify = true;
		rowLayout.type = SWT.HORIZONTAL;
		
		comp.setLayout(rowLayout);

		
		refresh();
		
		
		/*label.addMouseListener(new MouseListener() {
			public void mouseUp(MouseEvent e) {
				if (error != null) {
				 ErrorDialog.openError(Activator.getDefault().getWorkbench()
							.getWorkbenchWindows()[0].getShell(), "Errores de workspace de LTO", null, error);
				}
			}
			public void mouseDoubleClick(MouseEvent e) {}
			public void mouseDown(MouseEvent e) {}
		});*/
		/*label.addMouseTrackListener(new MouseTrackListener() {
			public void mouseEnter(MouseEvent e) {
				// TODO Auto-generated method stub
				label.setText(AbstractProyect.getCurrentProyect().basicGetModulesAndVersions());
				label.setToolTipText(AbstractProyect.getCurrentProyect().basicGetModulesAndVersions());
			}
			public void mouseHover(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			public void mouseExit(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});*/
		return comp;
	}

	public void refresh() {
		Button b = new Button(comp, SWT.NONE);
		b.setText("Refresh");
		b.addMouseListener(new MouseListener() {
			public void mouseUp(MouseEvent e) {
				// TODO WTFStatusBar.this.refresh();
			}
			public void mouseDoubleClick(MouseEvent e) {}
			public void mouseDown(MouseEvent e) {}
		});
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();
		for (IProject pro : projects) {
			if (pro.isOpen()) {
				try {
					if (pro.hasNature(JavaCore.NATURE_ID)) {
						IFile file = pro.getFile(".wtf");
						if (file != null && file.exists()) {
								b = new Button(comp, SWT.NONE);
								b.setText(pro.getName());
								b.addMouseListener(new LaunchTomcatMouseListener(pro));
							}
						}
				} catch (CoreException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}
	
	public class LaunchTomcatMouseListener extends MouseAdapter {
		private IProject project; 
		
		public LaunchTomcatMouseListener(IProject project) {
			super();
			this.project = project;
		}
		
		@Override
		public void mouseUp(MouseEvent e) {
			try {
				IFile file = project.getFile(".wtf");
				Properties props = new Properties();
				props.load(file.getContents());
				LaunchTomcat.launchTomcat(project, props.getProperty("contextPath", "/" + project.getName()), props.getProperty("port", "8080"), props.getProperty("serverport", "8208"));
			} catch (JavaModelException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (CoreException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
	}

	public static void setMessage(MultiStatus info) {
		// TODO Auto-generated method stub
		error = info;
	}

}
