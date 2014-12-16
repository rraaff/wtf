package wtfplugin.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import wtfplugin.Activator;


/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>,
 * we can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class WTFPreferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static final String P_USERNAME = "username";

	private StringFieldEditor userName = null;

	public WTFPreferences() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Aqui usted podra ingresar el usuario de la base mongo-db");
		initializeDefaults();
	}

	public static String getUsername() {
		return Activator.getDefault().getPreferenceStore().getString(WTFPreferences.P_USERNAME);
	}

	/**
	 * Sets the default values of the preferences.
	 */
	private void initializeDefaults() {
		IPreferenceStore store = this.getPreferenceStore();
		if (store.getString(P_USERNAME) == null || store.getString(P_USERNAME).length() == 0) {
			store.setValue(P_USERNAME, "corp_" + System.getProperty("user.name"));
		}
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */

	public void createFieldEditors() {
		userName = new StringFieldEditor(P_USERNAME, "Usuario:", getFieldEditorParent());
		addField(userName);
	}


	public void init(IWorkbench workbench) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		userName.store();
		this.getPreferenceStore().setValue(P_USERNAME, userName.getStringValue());
		return true;
	}

}