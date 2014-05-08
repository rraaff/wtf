package wtfplugin.builder;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class LanguageBundles {

	private String language;
	private Map<String, String> bundles = new HashMap<String, String>();
	
	private Map<String, CountryBundles> countries = new HashMap<String,CountryBundles>();
	
	public LanguageBundles(String language) {
		super();
		this.language = language;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public Map<String, String> getBundles() {
		return bundles;
	} 
	public void setBundles(Map<String, String> bundles) {
		this.bundles = bundles;
	}
	
	public CountryBundles getCountryBundle(String country) {
		if (!countries.containsKey(country)) {
			countries.put(country, new CountryBundles(country));
		}
		return countries.get(country);
	}
	public String getRB(String key) {
		for (CountryBundles cb: countries.values()) {
			if (cb.getBundles().containsKey(key)) {
				String value = cb.getBundles().get(key);
				if (!StringUtils.isEmpty(value)) {
					return value;
				}
			}
		}
		String result =bundles.get(key);
		if (result == null) {
			result = "";
		}
		return result;
	}
	public Map<String, CountryBundles> getCountries() {
		return countries;
	}
	public void setCountries(Map<String, CountryBundles> countries) {
		this.countries = countries;
	}
	public Set<String> getCountryKeys(CountryBundles cb) {
		Set<String> result = new HashSet<String>();
		result.addAll(this.bundles.keySet());
		result.addAll(cb.getBundles().keySet());
		return result;
	}
	public Collection<? extends String> getCountryPendingKeys(CountryBundles cb) {
		Set<String> st = new HashSet<String>();
		for (Map.Entry<String, String> entry : this.bundles.entrySet()) {
				if (entry.getValue().startsWith("_/_")) {
					st.add(entry.getKey());
				}
		}
		for (Map.Entry<String, String> entry : cb.getBundles().entrySet()) {
			if (entry.getValue().startsWith("_/_")) {
				st.add(entry.getKey());
			}
	}
		return st;
	}
}
