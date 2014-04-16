package wtfplugin.builder;

import java.util.HashMap;
import java.util.Map;

public class CountryBundles {

	private String country;
	private Map<String, String> bundles = new HashMap<String, String>();
	
	public CountryBundles(String country) {
		super();
		this.country = country;
	}
	public Map<String, String> getBundles() {
		return bundles;
	}
	public void setBundles(Map<String, String> bundles) {
		this.bundles = bundles;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
}
