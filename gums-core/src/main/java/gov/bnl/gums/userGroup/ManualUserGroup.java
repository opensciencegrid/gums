/*
 * ManualUserGroup.java
 *
 * Created on May 25, 2004, 4:48 PM
 */

package gov.bnl.gums.userGroup;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.Security;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.db.ManualUserGroupDB;
import gov.bnl.gums.FQAN;
import gov.bnl.gums.GridUser;
import gov.bnl.gums.RWLock;

/** 
 * A user group that is defined by a list of users stored in some way, allowing
 * to add and remove users. The persistance layer is implemented through an
 * interface, allowing different storage frameworks (i.e. database, LDAP, file)
 * <p>
 * This class will provide also configurable data caching.
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class ManualUserGroup extends UserGroup {
    static public String getTypeStatic() {
		return "manual";
	}
    
    private Logger log = Logger.getLogger(ManualUserGroup.class);
    private ManualUserGroupDB db;
	private String persistenceFactory = "";
	private String membersUri = "";
	private String nonMembersUri = "";
	private List<Pattern> patternList = new ArrayList();
	private List<Pattern> dnPatternList = new ArrayList();
	private boolean needsRefresh = true;
	private RWLock rWLock = new RWLock();
	protected Date patternListLastUpdated;
	protected int secondsBetweenPatternRefresh = 60;
	protected int adminSecondsBetweenPatternRefresh = 10;
    
    /**
     * Create a new manual user group. This empty constructor is needed by the XML Digestor.
     */
    public ManualUserGroup() {
    	super();
    	
    	Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
		Properties properties = System.getProperties();
		String handlers = System.getProperty("java.protocol.handler.pkgs");
		if (handlers == null) {
		    // nothing specified yet (expected case)
		    properties.put("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
		}
		else {
		    // something already there, put ourselves out front
		    properties.put("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol|".concat(handlers));		    
		}

    }
    
    /**
     * Create a new manual user group with a configuration.
     */
    public ManualUserGroup(Configuration configuration) {
    	super(configuration);
    }
    
    /**
     * Create a new manual user group with a configuration and a name.
     */
	public ManualUserGroup(Configuration configuration, String name) {
		super(configuration, name);
	}
    
    public void addMember(GridUser user) {
    	rWLock.getReadLock();
    	try {
	        getDB().addMember(user);
	        needsRefresh = true;
    	}
    	catch(Exception e) {
    		log.error(e);
    	}
    	finally {
    		rWLock.releaseLock();
    	}
    }
    
    public UserGroup clone(Configuration configuration) {
    	ManualUserGroup userGroup = new ManualUserGroup(configuration, new String(getName()));
    	userGroup.setDescription(new String(getDescription()));
    	userGroup.setAccess(new String(getAccess()));
    	userGroup.setMembersUri(new String(membersUri));
    	userGroup.setNonMembersUri(new String(nonMembersUri));
    	userGroup.setPersistenceFactory(new String(persistenceFactory));
    	return userGroup;
    }
    
    private ManualUserGroupDB getDB() {
    	if (db==null)
    		db = getConfiguration().getPersistenceFactory(persistenceFactory).retrieveManualUserGroupDB( getName() );
    	return db;
    }
    
    public java.util.List getMemberList() {
        return getDB().retrieveMembers();
    }

    public String getMembersUri() {
    	return membersUri;
    }

    public String getNonMembersUri() {
    	return nonMembersUri;
    }
    
    /**
     * Setter for property persistenceFactory.
     * 
     * @return persistence factory as string.
     */
    public String getPersistenceFactory() {
        return persistenceFactory;
    }
    
    public String getType() {
		return "manual";
	}

    @Override
    public boolean isDNInGroup(GridUser user) {
        return isInGroupImpl(user, true);
    }

    @Override
    public boolean isInGroup(GridUser user) {
        return isInGroupImpl(user, false);
    }

    public boolean isInGroupImpl(GridUser user, boolean dnOnly) {
    	boolean returnVal = false;
		
    	try {
    	    rWLock.getReadLock();
    	    Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, accessIndex==0 ? -adminSecondsBetweenPatternRefresh : -secondsBetweenPatternRefresh);
            if (needsRefresh || patternListLastUpdated.before(cal.getTime())) {
                rWLock.releaseLock();
                refreshPatternList();
            	rWLock.getReadLock();
            }

            List<Pattern> patterns = dnOnly ? dnPatternList : patternList;
            for (Pattern p : patterns) {
                String userString = dnOnly ? user.getCertificateDN() : concatDNFqan(user);
                Matcher m = p.matcher(userString);
                if (log.isTraceEnabled()) { log.trace("trying to match user "+userString+" against "+m.toString()); }
                if (m.matches()) {
                    returnVal = true;
                    break;
                }
	    }
        }
	catch(Exception e) {
	    log.error(e);
	}
	finally {
	    rWLock.releaseLock();
	}
    	
        return returnVal;
    }

    public boolean removeMember(GridUser user) {
    	rWLock.getReadLock();
    	boolean value = false;
    	try {
    		value = getDB().removeMember(user);
    		needsRefresh = true;
		}
		catch(Exception e) {
			log.error(e);
		}
		finally {
			rWLock.releaseLock();
		}
        return value;
    }
    
    /**
     * Setter for property persistenceFactory
     * 
     * @param persistenceFactory
     */
    public void setPersistenceFactory(String persistenceFactory) {
        this.persistenceFactory = persistenceFactory;
    }
    
    public void setMembersUri(String membersUri) {
    	this.membersUri = membersUri;
    }

    public void setNonMembersUri(String nonMembersUri) {
    	this.nonMembersUri = nonMembersUri;
    }

    public String toString() {
        if (persistenceFactory == null) {
            return "ManualUserGroup: persistenceFactory=null - group='" + getName() + "'";
        } else {
            return "ManualUserGroup: persistenceFactory='" + persistenceFactory + "' - group='" + getName() + "'";
        }
    }
 
    public String toString(String bgColor) {
    	return "<td bgcolor=\""+bgColor+"\"><a href=\"userGroups.jsp?command=edit&name=" + getName() + "\">" + getName() + "</a></td><td bgcolor=\""+bgColor+"\">" + getType() + "</td><td bgcolor=\""+bgColor+"\">&nbsp;</td><td bgcolor=\""+bgColor+"\">&nbsp;</td><td bgcolor=\""+bgColor+"\">&nbsp;</td><td bgcolor=\""+bgColor+"\">&nbsp;</td>";
    }
    
    public String toXML() {
    	return "\t\t<manualUserGroup\n"+
		"\t\t\tname='"+getName()+"'\n"+
		"\t\t\taccess='"+accessTypes[accessIndex]+"'\n" +
		"\t\t\tdescription='"+getDescription()+"'\n"+
		"\t\t\tpersistenceFactory='"+persistenceFactory+"'\n" +
		"\t\t\tmembersUri='"+membersUri+"'\n" +
		"\t\t\tnonMembersUri='"+nonMembersUri+"'/>\n\n";
    }    
    
    public void updateMembers() {
    	rWLock.getReadLock();
    	try {
	    	if (membersUri.length()>0) {
	    		BufferedReader in = null;
	    		if (membersUri.startsWith("http")) {
	    			URL url = new URL(membersUri);
	    			URLConnection connection = url.openConnection();
		    		in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	    		}
	    		else if (membersUri.startsWith("file://")) {
		    		in = new BufferedReader(new FileReader(membersUri.substring(7)));	    			
	    		}
	    		else {
	    			String message = "Unsupported members URI: " + membersUri;
	    			log.error(message);
	    			throw new RuntimeException(message);
	    		}
	    		List members = getMemberList();
	    		String line;
	    		while ((line = in.readLine()) != null) {
	    			line = line.trim();
	    			if (line.length()==0)
	    				continue;
	    			String[] elements = line.split(",");
	    			GridUser user = new GridUser();
	    			if (elements.length >= 1)
	    				user.setCertificateDN(elements[0]);
	    			if (elements.length >= 2)
	    				user.setVoFQAN(new FQAN(elements[1], false));
	    			if (!members.contains(user)) {
	    				log.info("Added user "+user+" to "+getName()+" user group");
	    				getDB().addMember(user);
	    			}
	    		}
	    		if (in!=null)
	    			in.close();
	    	}
	    } catch (Throwable e) {
	    	String message = "Couldn't retrieve users from URI " + membersUri + ": ";
	        log.error(message, e);
	        throw new RuntimeException(message + ": " + e.getMessage());
	    } finally {
	    	rWLock.releaseLock();
	    }
	    		
	    rWLock.getReadLock();
	    try {
	    	if (nonMembersUri.length()>0) {
	    		BufferedReader in;
	    		if (nonMembersUri.startsWith("http")) {
	    			URL url = new URL(nonMembersUri);
	    			URLConnection connection = url.openConnection();
		    		in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	    		}
	    		else if (nonMembersUri.startsWith("file://")) {
		    		in = new BufferedReader(new FileReader(nonMembersUri.substring(7)));	    			
	    		}
	    		else {
	    			String message = "Unsupported non-members URI: " + membersUri;
	    			log.error(message);
	    			throw new RuntimeException(message);
	    		}
	    		List members = getMemberList();
	    		String line;
	    		while ((line = in.readLine()) != null) {
	    			line = line.trim();
	    			if (line.length()==0)
	    				continue;
	    			String[] elements = line.split(",");
	    			GridUser user = new GridUser();
	    			if (elements.length >= 1)
	    				user.setCertificateDN(elements[0]);
	    			if (elements.length >= 2)
	    				user.setVoFQAN(new FQAN(elements[1], false));
	    			if (members.contains(user)) {
	    				log.info("Removed user "+user+" from "+getName()+" user group");
	    				getDB().removeMember(user);
	    			}
	    		}
	    		if (in!=null)
	    			in.close();
	    	}
	    } catch (Throwable e) {
	    	String message = "Couldn't retrieve users from URI " + nonMembersUri + ": ";
	        log.error(message, e);
	        throw new RuntimeException(message + ": " + e.getMessage());
	    } finally {
	    	rWLock.releaseLock();
	    }
       	
       	needsRefresh = true;
    }
    
    private String concatDNFqan(GridUser user) {
    	return (user.getCertificateDN()!=null?user.getCertificateDN():"") + "," + (user.getVoFQAN()!=null?user.getVoFQAN().getFqan():"");
    }
    
    private void refreshPatternList() {
    	rWLock.getWriteLock();
    	try {
    		log.trace("refreshed pattern list for usergroup "+getName());
	 		patternList.clear();
			dnPatternList.clear();
	 		List members = getDB().retrieveMembers();
	 		Iterator it = members.iterator();
	 		while (it.hasNext()) 
	 		{
	 			GridUser itUser = (GridUser)it.next();
	 			Pattern p = Pattern.compile(Pattern.quote(concatDNFqan(itUser)));
				patternList.add(p);
				Pattern p2 = Pattern.compile(Pattern.quote(itUser.getCertificateDN()));
				dnPatternList.add(p2);
	 		}
			needsRefresh = false;
			patternListLastUpdated = Calendar.getInstance().getTime();
		}
		catch(Exception e) {
			log.error(e);
		}
		finally {
			rWLock.releaseLock();
		}
    }
}
