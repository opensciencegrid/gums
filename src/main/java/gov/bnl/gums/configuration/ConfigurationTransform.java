package gov.bnl.gums.configuration;

import gov.bnl.gums.GUMS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;

/**
 *
 * @author Jay Packard
 */
public class ConfigurationTransform {
	static private Log log = LogFactory.getLog(FileConfigurationStore.class);
	static private Log gumsResourceAdminLog = LogFactory.getLog(GUMS.resourceAdminLog);
	
	static public void doTransform(String configFile, String transformFile) {
	    try {
	    	String configFileTemp = configFile + "~";
        	
	        XMLReader reader = XMLReaderFactory.createXMLReader();
	        Source source = new SAXSource(reader, new InputSource(configFile));

	        StreamResult result = new StreamResult(configFileTemp);
	        Source style = new StreamSource(transformFile);

	        TransformerFactory transFactory = TransformerFactory.newInstance();
	        Transformer trans = transFactory.newTransformer(style);
	        
	        trans.transform(source, result);

	        FileConfigurationStore.moveFile(configFile, configFile + "_1.1");

	        FileConfigurationStore.moveFile(configFileTemp, configFile);
	     } catch (Exception e) {
	        gumsResourceAdminLog.fatal("Could not convert older version of gums.config: " + e.getMessage());
	        log.info("Could not convert older version of gums.config.", e);
	        throw new RuntimeException("The configuration wasn't read properly");	    	 
	     }  	
	}

}
