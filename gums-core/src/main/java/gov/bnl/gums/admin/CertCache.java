/*
 * CertCache.java
 *
 * Created on December 21, 2004, 12:54 PM
 */

package gov.bnl.gums.admin;

import gov.bnl.gums.CertToolkit;

import java.security.cert.X509Certificate;
import javax.servlet.Filter;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

/**
 * Implements the javax.servlet.Filter class and stores a user certificate
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class CertCache implements Filter {
	static private Logger log = Logger.getLogger(CertCache.class);
	static private ServletContext context;
	static private ThreadLocal certificate = new ThreadLocal();

	/**
	 * Get the directory path for the configuration files
	 * 
	 * @return configuration directory as String
	 */
	static public String getConfigDir() {
		String base = context.getRealPath("/");
		String dir = base + "/WEB-INF/config";
		log.trace("URL to config dir: '" + dir + "'");
		return dir;
	}
	
	/**
	 * Get the directory path for the resource files
	 * 
	 * @return resource directory as String
	 */
	static public String getResourceDir() {
		String base = context.getRealPath("/");
		String dir = base + "/WEB-INF";
		log.trace("URL to resource dir: '" + dir + "'");
		return dir;
	}
	
	/**
	 * Get the directory path for the resource files
	 * 
	 * @return resource directory as String
	 */
	static public String getMetaDir() {
		String base = context.getRealPath("/");
		String dir = base + "/META-INF";
		log.trace("URL to meta dir: '" + dir + "'");
		return dir;
	}

	/**
	 * @return X509Certificate object
	 */
	static public X509Certificate getUserCertificate() {
		return (X509Certificate) certificate.get();
	}

	/**
	 * @param cert
	 */
    static public String getUserDN() {
            return CertToolkit.getUserDN(getUserCertificate());
    }

  	/**
  	 * @param cert
  	 */
  	static public void setUserCertificate(X509Certificate cert) {
  		certificate.set(cert);
  	}

	public void destroy() {
	}

	public void doFilter(javax.servlet.ServletRequest servletRequest,
			javax.servlet.ServletResponse servletResponse,
			javax.servlet.FilterChain filterChain) throws java.io.IOException,
			javax.servlet.ServletException {
		setUserCertificate(null);
		if (servletRequest
				.getAttribute("javax.servlet.request.X509Certificate") != null) {
			X509Certificate cert = ((X509Certificate[]) servletRequest
					.getAttribute("javax.servlet.request.X509Certificate"))[0];
			setUserCertificate(cert);
		}
		filterChain.doFilter(servletRequest, servletResponse);
	}

	public void init(javax.servlet.FilterConfig filterConfig)
			throws javax.servlet.ServletException {
		context = filterConfig.getServletContext();
	}

}
