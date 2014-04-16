/*
 * Created on Feb 12, 2007
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package wtfplugin.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author mgodoy
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class FileUtils {
	
	private static final String SCRIPT_SUFIX = ".SQL";
	private static final String EXCLUDED_SCRIPTS[] = new String[] {"CEA_CHANGE_VERSION.SQL","CEA_CHECK_REQUIREMENTS.SQL","CEA_DEFINE_VARIABLES.SQL","CEA_DEFINE_VERSION.SQL"};
	
	public static final String PROCEDURES_DIR = "Procedure Creation Scripts";
	public static final String TABLES_DIR = "Table Creation Scripts";
	public static final String TRIGGER_DIR = "Trigger Creation Scripts";
	public static final String INDEX_DIR = "Index Creation Scripts";
	public static final String SEQUENCES_DIR = "Sequence Creation Scripts";
	public static final String VIEW_DIR = "View Creation Scripts";
	public static final String PACKAGES_DIR = "Package Creation Scripts";
	public static final String FUNCTIONS_DIR = "Functions Creation Scripts";

	private static final byte empty_file_bytes[] = "".getBytes();
	
	
	
	public static void main(String[] args) throws Exception {
		//insertBefore("C:/RUNME.SQL", "\n-- Scripts que actualiza la version, no debe sacarse", "@@MARCOS.SQL");
	}
	

	public static void insertBefore(
		IFile file,
		String textToSearch,
		String textToInsert,
		IProgressMonitor progressMonitor)
		throws Exception {

		String runmeContents = readFileContents(file);

		int insertPoint = runmeContents.indexOf(textToSearch);
		String firstPart = runmeContents.substring(0, insertPoint);
		String lastPart = runmeContents.substring(insertPoint);
		StringBuilder result = new StringBuilder();

		result.append(firstPart);
		result.append(textToInsert + "\n");
		result.append(lastPart);
		setFileContents(file, result.toString(), progressMonitor);
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
	
	public static List<String> readFileLines(IFile file) throws Exception {
		List<String> result = new ArrayList<String>();
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader bufferedReader = null;
		String line;
		try {
			is = file.getContents();
			isr = new InputStreamReader(is);
			bufferedReader = new BufferedReader(isr);
			line = null;
			while ((line = bufferedReader.readLine()) != null) {
				result.add(line);
			}
		} finally {
			closeStreamQuitly(bufferedReader);
			closeStreamQuitly(isr);
			closeStreamQuitly(is);
		}
		return result;
	}
	
	private static void closeStreamQuitly(Reader is) {
		try {
			if (is != null) {
				is.close();
			}
		} catch (Exception e) {
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


	public static void setFileContents(
		IFile file,
		String contents,
		IProgressMonitor progressMonitor)
		throws Exception {
		file.setContents(
			new ByteArrayInputStream(contents.getBytes()),
			true,
			true,
			progressMonitor);
	}
	

	/**
	 * @param b
	 */
	private static void deleteDir(File file) {
		File subFiles[] = file.listFiles();
		for (int i = 0; i < subFiles.length; i++) {
			subFiles[i].delete(); 
		}
		file.delete();
	}


	public static ArrayList<String> split(String value, String delimiter) {
		ArrayList<String> array = new ArrayList<String>();
		if (value != null) {
			String lines[] = value.split("\n");
			for (int i = 0; i < lines.length; i++) {
				array.add(lines[i]);
			}
		}
		return array;
	}
	
	private static String replace(
		String aStringToSearch,
		String oldString,
		String newString) {
		if (aStringToSearch == null) {
			return null;
		}
		int oldStringLength = oldString.length();
		StringBuilder sb = new StringBuilder(oldStringLength + 100);
		int occ = 0;
		int start = 0;
		while (occ != -1) {
			occ = aStringToSearch.indexOf(oldString, start);
			if (occ != -1) {
				sb.append(aStringToSearch.substring(start, occ));
				sb.append(newString);
				start = occ + oldStringLength;
			}
		}
		// append the rest
		sb.append(aStringToSearch.substring(start));
		return sb.toString();
	}

	public static String getNameWithoutExtension(IFile file) {
		String name = file.getName();
		String extension = file.getFileExtension();
		if (extension != null) {
			name = name.substring(0, name.length() - 1 - extension.length());
		}
		return name;
	}
	
}