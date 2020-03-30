package wtfplugin.statusbar;


import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

import com.swtdesigner.ResourceManager;

import wtfplugin.Activator;

public class WTFStatusBar extends WorkbenchWindowControlContribution {
	
	public static int RB_ERROR = 2;
	public static int RB_WARNING = 1;
	public static int RB_OK = 0;

	private static WTFStatusBar instance;
	private CLabel label;
	private static Integer rb_check;
	private static MultiStatus error;
	private Composite comp;

	public WTFStatusBar() {
		instance = this;
	}

	public WTFStatusBar(String id) {
		super(id);
		instance = this;
	}

	private static void setImage() {
		if (rb_check==null) {
			instance.label.setImage(ResourceManager.getPluginImage("WTFPlugin", "icons/circle_gray_16.png"));
			return;
		}
		if (rb_check==0) {
			instance.label.setImage(ResourceManager.getPluginImage("WTFPlugin", "icons/circle_green_16.png"));
		}
		if (rb_check==1) {
			instance.label.setImage(ResourceManager.getPluginImage("WTFPlugin", "icons/circle_yellow_16.png"));
		}
		if (rb_check==2) {
			instance.label.setImage(ResourceManager.getPluginImage("WTFPlugin", "icons/circle_red_16.png"));
		}
	}

	@Override
	protected Control createControl(Composite parent) {
		
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
	
	public static void setRBCheck(Integer value) {
		rb_check = value;
		if (instance != null) {
			if (instance.label != null) {
				setImage();
			}
		}
	}

	public void refresh() {
		Button b = null ; //= new Button(comp, SWT.NONE);
		/*b.setText("Refresh");
		b.addMouseListener(new MouseListener() {
			public void mouseUp(MouseEvent e) {
				// TODO WTFStatusBar.this.refresh();
			}
			public void mouseDoubleClick(MouseEvent e) {}
			public void mouseDown(MouseEvent e) {}
		});*/
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();
		for (IProject pro : projects) {
			if (pro.isOpen()) {
				try {
					if (pro.hasNature(JavaCore.NATURE_ID)) {
						IFile file = pro.getFile(".wtf");
						if (file != null && file.exists()) {
								b = new Button(comp, SWT.NONE);
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
	

	public static void setMessage(MultiStatus info) {
		// TODO Auto-generated method stub
		error = info;
	}

}
