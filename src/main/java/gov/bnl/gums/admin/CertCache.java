/*
 * CertCache.java
 *
 * Created on December 21, 2004, 12:54 PM
 */

package gov.bnl.gums.admin;

import java.security.cert.X509Certificate;
import javax.servlet.Filter;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author carcassi
 */
public class CertCache implements Filter {
    private static Log log = LogFactory.getLog(CertCache.class);
    private static ServletContext context;
    private static ThreadLocal certificate = new ThreadLocal();
    
    public static String getConfPath() {
        String base = context.getRealPath("/");
        log.trace("Path to the web app: '" + base + "'");
        String fullpath = base + "/WEB-INF/classes/gums.config";
        log.trace("URL to config file: '" + fullpath + "'");
        return fullpath;
    }

    public static X509Certificate getUserCertificate() {
        return (X509Certificate) certificate.get();
    }

    public static String getUserDN() {
        return CertToolkit.getUserDN(getUserCertificate());
    }

    public static void setUserCertificate(X509Certificate cert) {
        certificate.set(cert);
    }
    
    public void destroy() {
    }
    
    public void doFilter(javax.servlet.ServletRequest servletRequest, javax.servlet.ServletResponse servletResponse, javax.servlet.FilterChain filterChain) throws java.io.IOException, javax.servlet.ServletException {
        setUserCertificate(null);
        if (servletRequest.getAttribute("javax.servlet.request.X509Certificate") != null){
            X509Certificate cert = ((X509Certificate[]) servletRequest.getAttribute("javax.servlet.request.X509Certificate"))[0];
            setUserCertificate(cert);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
    
    public void init(javax.servlet.FilterConfig filterConfig) throws javax.servlet.ServletException {
        context = filterConfig.getServletContext();
    }
    
}
