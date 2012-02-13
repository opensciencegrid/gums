package gov.bnl.gums.db;

import java.util.Collection;
import java.util.TreeMap;
import java.util.Date;
import java.text.DateFormat;

public class MockConfigurationDB implements ConfigurationDB {
	TreeMap configurations = new TreeMap();
	String currentConf = null;
	
	public boolean deleteBackupConfiguration(Date date) {
		return (configurations.remove(date.toString()) != null);
	}

	public boolean deleteBackupConfiguration(String name) {
		return (configurations.remove(name) != null);
	}
	
	public Collection getBackupNames(DateFormat dateFormat) {
		return configurations.keySet();
	}
	
	public Date getLastModification() {
		return new Date();
	}
	
	public boolean isActive() {
		return true;
	}
	
	public String restoreConfiguration(Date date) {
		return (String)configurations.get(date.toString());
	}
	
	public String restoreConfiguration(String name) {
		return (String)configurations.get(name);
	}
	
	public String retrieveCurrentConfiguration() {
		return currentConf;
	}
	
	public void setConfiguration(String text, boolean backupCopy, String name, Date date) {
		configurations.put(date.toString(), text);
		if (!backupCopy)
			currentConf = text;
	}

}
