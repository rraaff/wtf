package wtfplugin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "WTFPlugin"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		System.out.println("start!!!!");
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static void showMessage(String title, String message) {
		showMessage(title, message, null);
	}
	
	public static void showMessage(final String title, final String message, final Shell shell) {
		Display.getDefault().syncExec( new Runnable() {
			public void run() {
				MessageBox msg = new MessageBox(shell == null ? getDefault().getWorkbench().getWorkbenchWindows()[0].getShell() : shell);
				msg.setText(title);
				msg.setMessage(message);
				msg.open();
			}
		});
	}
	
	public static void showMessage(final String title, final String message, final Shell shell, final int style) {
		Display.getDefault().syncExec( new Runnable() {
			public void run() {
				MessageBox msg = new MessageBox(shell == null ? getDefault().getWorkbench().getWorkbenchWindows()[0].getShell() : shell, style);
				msg.setText(title);
				msg.setMessage(message);
				msg.open();
			}
		});
	}
	
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	
	public static void showErrorMessage(String title, String message) {
		showMessage(title, message, null, SWT.ICON_ERROR);
	}
	
	public static void showException(Throwable e) {
		showException(e, null);
	}
	
	public static void showException(final Throwable e, final Shell shell) {
		Display.getDefault().syncExec( new Runnable() {
			public void run() {
				MessageBox msg = new MessageBox(shell == null ? getDefault().getWorkbench().getActiveWorkbenchWindow().getShell() : shell);
				msg.setText("Exception!!!");
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				PrintWriter printWriter = new PrintWriter(byteArrayOutputStream);
				e.printStackTrace(printWriter);
				printWriter.close();
				try {
					byteArrayOutputStream.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				String message = new String(byteArrayOutputStream.toByteArray());
				Activator.getDefault().getLog().log(new Status(Status.ERROR, "SQLGeneratorPlugin", 1, "error", e));
				msg.setMessage("Problem:" + e.getMessage() + "\nStackTrace:" + message.substring(0, Math.min(1000, message.length())) + "\nComplete trace can be found in eclipse log file, please notify technical support.");
				msg.open();
			}
		});
	}
}
