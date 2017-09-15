package wtfplugin.preferences;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.preference.BooleanFieldEditor;
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
	private static final String P_TOMCAT_VERSION = "tomcatVersion";
	private static final String P_REPLACEMENTS_JVM = "replacementsJVM";
	private static final String P_CLUSTER_SESSION_MONGO = "clusterSessionMongo";
	private static final String P_HOME_OFFICE = "homeOffice";
	private static final String P_MONGO_SESSION_HOST = "mongosessionhost";

	private StringFieldEditor userName = null;
	private StringFieldEditor tomcatVersion = null;
	private StringFieldEditor replacementsJVM = null;
	private BooleanFieldEditor clusterSessionMongo = null;
	private BooleanFieldEditor homeOffice = null;
	private StringFieldEditor mongosessionhost = null;

	public WTFPreferences() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Aqui usted podra ingresar el usuario de la base mongo-db");
		initializeDefaults();
	}

	public static String getUsername() {
		return Activator.getDefault().getPreferenceStore().getString(WTFPreferences.P_USERNAME);
	}
	
	public static String getMongosessionhosts() {
		String result =  Activator.getDefault().getPreferenceStore().getString(WTFPreferences.P_MONGO_SESSION_HOST);
		if (result == null || result.trim().length() == 0) {
			return "corporate-db";
		} else {
			return result;
		}
	}
	
	public static String getTomcatVersion() {
		String result = Activator.getDefault().getPreferenceStore().getString(WTFPreferences.P_TOMCAT_VERSION);
		if (StringUtils.isEmpty(result) || !StringUtils.isNumeric(result)) {
			result = "6";			
		}
		return result;
	}
	
	public static Map<String, String> getReplacementsJVM() {
		String data = Activator.getDefault().getPreferenceStore().getString(WTFPreferences.P_REPLACEMENTS_JVM);
		Map<String, String> result = new HashMap<String, String>();
		if (data == null || StringUtils.isEmpty(data)) {
			return result;
		}
		for (String pair : data.split(",")) {
			if (!StringUtils.isEmpty(pair) && pair.contains("=")) {
	            String[] kv = pair.split("=");
	            result.put(kv[0], kv[1]);
			}
        }
		return result;
	}
	
	public static Boolean clusterSessionMongo() {
		Boolean result = Activator.getDefault().getPreferenceStore().getBoolean(P_CLUSTER_SESSION_MONGO);
		if (result == null) {
			return true;
		}
		return result;
	}
	
	public static Boolean homeOffice() {
		Boolean result = Activator.getDefault().getPreferenceStore().getBoolean(P_HOME_OFFICE);
		if (result == null) {
			return false;
		}
		return result;
	}
	
	public static String getTomcatVariable() {
		return "TOMCAT" + getTomcatVersion() + "_HOME";
	}
	
	public static String getTomcatVariable(String version) {
		return "TOMCAT" + version + "_HOME";
	}

	/**
	 * Sets the default values of the preferences.
	 */
	private void initializeDefaults() {
		IPreferenceStore store = this.getPreferenceStore();
		if (store.getString(P_USERNAME) == null || store.getString(P_USERNAME).length() == 0) {
			store.setValue(P_USERNAME, "corp_" + System.getProperty("user.name"));
		}
		if (store.getString(P_TOMCAT_VERSION) == null || store.getString(P_TOMCAT_VERSION).length() == 0) {
			store.setValue(P_TOMCAT_VERSION, "6");
		}

		if (store.getString(P_MONGO_SESSION_HOST) == null || store.getString(P_MONGO_SESSION_HOST).length() == 0) {
			store.setValue(P_MONGO_SESSION_HOST, "corporate-db");
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
		
		tomcatVersion = new StringFieldEditor(P_TOMCAT_VERSION, "Version de tomcat:", getFieldEditorParent());
		addField(tomcatVersion);
		
		replacementsJVM = new StringFieldEditor(P_REPLACEMENTS_JVM, "Replacements (key=value,...):", getFieldEditorParent());
		addField(replacementsJVM);
		
		clusterSessionMongo = new BooleanFieldEditor(P_CLUSTER_SESSION_MONGO, "Session en cluster mongo:", getFieldEditorParent());
		addField(clusterSessionMongo);
		
		mongosessionhost = new StringFieldEditor(P_MONGO_SESSION_HOST, "Host de mongo:", getFieldEditorParent());
		addField(mongosessionhost);
		
		homeOffice = new BooleanFieldEditor(P_HOME_OFFICE, "Home office:", getFieldEditorParent());
		addField(homeOffice);
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
		
		tomcatVersion.store();
		this.getPreferenceStore().setValue(P_TOMCAT_VERSION, tomcatVersion.getStringValue());
		
		replacementsJVM.store();
		this.getPreferenceStore().setValue(P_REPLACEMENTS_JVM, replacementsJVM.getStringValue());
		
		clusterSessionMongo.store();
		this.getPreferenceStore().setValue(P_CLUSTER_SESSION_MONGO, clusterSessionMongo.getBooleanValue());
		
		mongosessionhost.store();
		this.getPreferenceStore().setValue(P_MONGO_SESSION_HOST, mongosessionhost.getStringValue());
		
		homeOffice.store();
		this.getPreferenceStore().setValue(P_HOME_OFFICE, homeOffice.getBooleanValue());
		return true;
	}

}