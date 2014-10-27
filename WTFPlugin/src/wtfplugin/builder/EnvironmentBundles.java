package wtfplugin.builder;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;

public class EnvironmentBundles {

	private String env;
	private IFile file;
	private Map<String, String> props = new HashMap<String, String>();
	
	public EnvironmentBundles(IFile file, String env, Map<String, String> props) {
		super();
		this.file = file;
		this.env = env;
		this.props = props;
	}

	public Map<String, String> getProps() {
		return props;
	}

	public void setProps(Map<String, String> props) {
		this.props = props;
	}

}
