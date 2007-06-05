/*
 * NISAccountMapper.java
 *
 * Created on May 25, 2004, 2:25 PM
 */

package gov.bnl.gums.account;

import gov.bnl.gums.GUMS;
import gov.bnl.gums.NISClient;
import gov.bnl.gums.configuration.Configuration;

import java.util.*;

import org.apache.commons.logging.*;

/** 
 * Maps a user to a local account based on the CN of the certificate and the
 * gecos field in the NIS/YP database. The mapping can't be perfect, but contains
 * a series of heuristics that solve up to 90% of the cases, depending on how
 * the NIS database itself is kept.
 * <p>
 * It's suggested not to use this policy by itself, but to have it part of a 
 * CompositeAccountMapper in which a ManualAccountMapper comes first. This allows
 * to override those user mapping that are not satisfying.
 *
 * @deprecated
 * @author Gabriele Carcassi, Jay Packard
 */
public class NISAccountMapper extends AccountMapper {
    static Log log = LogFactory.getLog(NISAccountMapper.class);
    static Log adminLog = LogFactory.getLog(GUMS.resourceAdminLog);
    
    static public boolean checkSurname(String possibleSurname) {
        if (Character.isDigit(possibleSurname.charAt(0))) {
            return false;
        }
        if (possibleSurname.charAt(possibleSurname.length() - 1) == '.') {
            return false;
        }
        
        return true;
    }
    
    static public String getTypeStatic() {
		return "nis";
	}
    
	public static String[] parseNameAndSurname(String certificateSubject) {
        int begin = certificateSubject.indexOf("CN=") + 3;
        String CN = certificateSubject.substring(begin);
        
        StringTokenizer tokenizer = new StringTokenizer(CN);
        List tokens = new ArrayList();
        while (tokenizer.hasMoreTokens()) {
            tokens.add(tokenizer.nextToken());
        }
        
        String name = (String) tokens.get(0);

        int nSurname = 1;
        while (!checkSurname((String) tokens.get(tokens.size()-nSurname))) {
            nSurname++;
        }
        String surname = (String) tokens.get(tokens.size()-nSurname);
        
        log.trace("Certificate '" + certificateSubject + "' divided in name='" + name + "' and surname='" + surname + "'");
        return new String[] {name, surname};
    }
    
    private String jndiNisUrl = "";
    
    private Map nisClients = new Hashtable();

    public NISAccountMapper() {
    	super();
        adminLog.warn("The use of gov.bnl.gums.NISAccountMapper is unsupported.");
    }
    
    public NISAccountMapper(Configuration configuration) {
    	super(configuration);
        adminLog.warn("The use of gov.bnl.gums.NISAccountMapper is unsupported.");
    }
    
    public NISAccountMapper(Configuration configuration, String name) {
    	super(configuration, name);
    }
    
    public AccountMapper clone(Configuration configuration) {
    	NISAccountMapper accountMapper = new NISAccountMapper(configuration, getName());
    	accountMapper.setDescription(getDescription());
    	accountMapper.setJndiNisUrl(jndiNisUrl);
    	return accountMapper;
    }
    
    public String getJndiNisUrl() {
        return jndiNisUrl;
    }
    
    public String getType() {
		return "nis";
	}
    
    public String mapUser(String userDN, boolean createIfDoesNotExist) {
        String[] nameSurname = parseNameAndSurname(userDN);
        return nisClient(jndiNisUrl).findAccount(nameSurname[0], nameSurname[1]);
    }

    public void setJndiNisUrl(String jndiNisUrl) {
        this.jndiNisUrl = jndiNisUrl;
    }

    public String toString(String bgColor) {
    	return "<td bgcolor=\""+bgColor+"\"><a href=\"accountMappers.jsp?command=edit&name=" + getName() + "\">" + getName() + "</a></td><td bgcolor=\""+bgColor+"\">" + getType() + "</td><td bgcolor=\""+bgColor+"\">&nbsp;</td>";
    }

    public String toXML() {
    	return "\t\t<nisAccountMapper\n"+
			"\t\t\tname='"+getName()+"'\n"+
			"\t\t\tdescription='"+getDescription()+"'\n"+
			"\t\t\tjndiNisUrl='"+jndiNisUrl+"'/>\n\n";
    }          
    
    private NISClient nisClient(String jndiNisUrl) {
        NISClient client = (NISClient) nisClients.get(jndiNisUrl);
        if (client != null) {
            log.trace("Reusing NisClient for '" + jndiNisUrl +"'");
            return client;
        }
        log.debug("Creating new NisClient for '" + jndiNisUrl + "'");
        client = new NISClient(jndiNisUrl);
        nisClients.put(jndiNisUrl, client);
        return client;
    }
}
