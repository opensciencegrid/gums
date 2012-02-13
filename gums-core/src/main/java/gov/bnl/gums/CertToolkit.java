/*
 * CertToolkit.java
 *
 * Created on May 11, 2005, 12:00 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package gov.bnl.gums;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.security.cert.X509Certificate;

import org.apache.log4j.Logger;

/**
 * Toolkit for doing parsing operations on certificates
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class CertToolkit {
    static private Logger log = Logger.getLogger(CertToolkit.class);
	
    /**
     * @param commaDN
     * @return
     */
    public static String convertDN(String commaDN) {
        List pieces = new ArrayList();
        while (commaDN.indexOf(", ") != -1) {
            int pos = commaDN.indexOf(", ");
            pieces.add(commaDN.substring(0, pos));
            commaDN = commaDN.substring(pos+2);
        }
        pieces.add(commaDN);
        Collections.reverse(pieces);
        Iterator iter = pieces.iterator();
        StringBuffer DN = new StringBuffer();
        while (iter.hasNext()) {
            DN.append("/");
            DN.append((String) iter.next());
        }
        return DN.toString();
    }
    
    /**
     * @param cert
     * @return
     */
    public static String getUserDN(X509Certificate cert) {
        if (cert == null) return null;
        String commaDN = cert.getSubjectX500Principal().toString();
        // TODO Probably should check the issuer cert is also a proxy
        // Old style proxy are recognized by proxy in the DN
        if (commaDN.toLowerCase().indexOf("proxy") != -1) {
            commaDN = cert.getIssuerX500Principal().toString();
        }
        // New style proxy are recognized by presence of extension
        if (cert.getExtensionValue("1.3.6.1.5.5.7") != null) {
            commaDN = cert.getIssuerX500Principal().toString();
        }
        // New proxy implementation is bogus, and uses a different extension
        if (cert.getExtensionValue("1.3.6.1.4.1.3536.1.222") != null) {
            commaDN = cert.getIssuerX500Principal().toString();
        }
        return convertDN(commaDN);
    }
    
    /**
     * @param certificateSubject
     * @return
     */
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
    
    public static boolean checkSurname(String possibleSurname) {
        if (Character.isDigit(possibleSurname.charAt(0))) {
            return false;
        }
        if (possibleSurname.charAt(possibleSurname.length() - 1) == '.') {
            return false;
        }
        
        return true;
    }
}
