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

public class ConfigurationTransform {
    private static Log log = LogFactory.getLog(FileConfigurationStore.class);
    private static Log gumsResourceAdminLog = LogFactory.getLog(GUMS.resourceAdminLog);
	
	public static void doTransform(String configFile, String transformFile) {
	    try {
	    	String configFileOld = configFile + "_oldversion";
        	moveFile(configFile, configFileOld);
        	
	        XMLReader reader = XMLReaderFactory.createXMLReader();
	        Source source = new SAXSource(reader, new InputSource(configFileOld));

	        StreamResult result = new StreamResult(configFile);
	        Source style = new StreamSource(transformFile);

	        TransformerFactory transFactory = TransformerFactory.newInstance();
	        Transformer trans = transFactory.newTransformer(style);
	        
	        trans.transform(source, result);
	     } catch (Exception e) {
	        gumsResourceAdminLog.fatal("Could not convert older version of gums.config: " + e.getMessage());
	        log.info("Could not convert older version of gums.config.", e);
	        throw new RuntimeException("The configuration wasn't read properly");	    	 
	     }  	
	}
	
    private static void moveFile(String source, String target) {
        try {
			FileInputStream fis  = new FileInputStream(source);
			FileOutputStream fos = new FileOutputStream(target);
			byte[] buf = new byte[1024];
			int i = 0;
			while((i=fis.read(buf))!=-1)
			  fos.write(buf, 0, i);
			fos.write('\n');
			fis.close();
			fos.close();
			new File(source).delete();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
