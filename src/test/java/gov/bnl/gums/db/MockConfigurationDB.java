package gov.bnl.gums.db;

import java.util.Collection;
import java.util.TreeMap;
import java.util.Date;

public class MockConfigurationDB implements ConfigurationDB {
	TreeMap configurations = new TreeMap();
	String currentConf = null;
	
	public void deleteBackupConfiguration(String dateStr) {
		configurations.remove(dateStr);
	}
	
	public Collection getBackupConfigDates() {
		return configurations.keySet();
	}
	
	public Date getLastModification() {
		return new Date();
	}
	
	public boolean isActive() {
		return true;
	}
	
	public String restoreConfiguration(String dateStr) {
		return (String)configurations.get(dateStr);
	}
	
	public String retrieveCurrentConfiguration() {
		return currentConf;
	}
	
	public void setConfiguration(String text, String dateStr, boolean backupCopy) {
		configurations.put(dateStr, text);
		if (!backupCopy)
			currentConf = text;
	}

}
