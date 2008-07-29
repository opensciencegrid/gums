package gov.bnl.gums.db;

import java.util.Collection;
import java.util.TreeMap;
import java.util.Date;

public class MockConfigurationDB implements ConfigurationDB {
	TreeMap configurations = new TreeMap();
	String currentConf = null;
	
	public void deleteBackupConfiguration(Date date) {
		configurations.remove(date.toString());
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
	
	public String restoreConfiguration(Date date) {
		return (String)configurations.get(date.toString());
	}
	
	public String retrieveCurrentConfiguration() {
		return currentConf;
	}
	
	public void setConfiguration(String text, Date date, boolean backupCopy) {
		configurations.put(date.toString(), text);
		if (!backupCopy)
			currentConf = text;
	}

}
