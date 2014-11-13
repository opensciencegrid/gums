/*
 * CertCache.java
 *
 * Created on December 21, 2004, 12:54 PM
 */

package gov.bnl.gums.admin;

import gov.bnl.gums.CertToolkit;

import java.util.Vector;
import java.util.List;

import java.security.cert.X509Certificate;
import javax.servlet.Filter;
import javax.servlet.ServletContext;

import org.glite.security.util.CertUtil;
import org.glite.voms.VOMSValidator;
import org.glite.voms.VOMSAttribute;
import org.glite.voms.FQAN;

import org.apache.log4j.Logger;

/**
 * Implements the javax.servlet.Filter class and stores a user certificate
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class CertCache implements Filter {
	static private Logger log = Logger.getLogger(CertCache.class);
	static private ServletContext context;
	// If you add a new variable here, make sure to add it to
	// the reset method.
	static private ThreadLocal certificate = new ThreadLocal();
	static private ThreadLocal certificateChain = new ThreadLocal();
	static private ThreadLocal dn = new ThreadLocal();
	static private ThreadLocal fqan = new ThreadLocal();

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
	 * Return the DN associated with the current client
	 */
	static public String getUserDN() {
		return (String) dn.get();
	}

	/**
	 * Return the FQAN associated with the current client
	 */
	static public String getUserFQAN() {
		return (String) fqan.get();
	}

	/**
	 * Set the certificate chain of the currently connected client
	 * @param cert
	 */
	static public void setUserCertificateChain(X509Certificate[] chain) {
		certificateChain.set(chain);
		int i = CertUtil.findClientCert(chain);
		X509Certificate cert = null;
		if (i < 0) {
			log.warn("No client certificate found in the supplied certificate chain");
		} else {
			cert = chain[i];
			certificate.set(cert);
		}
		Vector voms_list = VOMSValidator.parse(chain);
		if ((voms_list != null) && (voms_list.size() > 0)) {
			VOMSAttribute attribute = (VOMSAttribute)voms_list.get(0);
			if (attribute != null) {
				List fqans = attribute.getListOfFQAN();
				if ((fqans != null) && (fqans.size() > 0)) {
					fqan.set(((FQAN)(fqans.get(0))).getFQAN());
				}
			}
		}
		if (cert != null) {
			dn.set(CertToolkit.getUserDN(getUserCertificate()));
		}
	}

	/**
	 * Returns the certificate chain of the currently connected client
	 * @return X509Certificate[] object
	 */
	static public X509Certificate[] getUserCertificateChain() {
		return (X509Certificate[]) certificateChain.get();
	}

	static public void reset() {
		certificate.set(null);
		certificateChain.set(null);
		dn.set(null);
		fqan.set(null);
	}

	public void destroy() {
	}

	public void doFilter(javax.servlet.ServletRequest servletRequest,
			javax.servlet.ServletResponse servletResponse,
			javax.servlet.FilterChain filterChain) throws java.io.IOException,
			javax.servlet.ServletException {
		reset();
		if (servletRequest
				.getAttribute("javax.servlet.request.X509Certificate") != null) {
			X509Certificate[] chain = ((X509Certificate[]) servletRequest
					.getAttribute("javax.servlet.request.X509Certificate"));
			setUserCertificateChain(chain);
		} else {
			log.info("Missing client certificate from request.");
		}
		try {
			filterChain.doFilter(servletRequest, servletResponse);
		} catch (Exception e) {
			log.error("Unhandled failure in servlet processing.", e);
			throw new javax.servlet.ServletException("Unhandled failure in servlet processing.", e);
		}
	}

	public void init(javax.servlet.FilterConfig filterConfig)
			throws javax.servlet.ServletException {
		context = filterConfig.getServletContext();
	}

}
