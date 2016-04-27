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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import eu.emi.security.authn.x509.proxy.ProxyUtils;
import org.italiangrid.voms.VOMSAttribute;
import org.italiangrid.voms.VOMSValidators;
import org.italiangrid.voms.ac.VOMSACValidator;
import org.italiangrid.voms.ac.impl.DefaultVOMSValidator;
import org.italiangrid.voms.util.CertificateValidatorBuilder;

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
	static private ThreadLocal<String> dn = new ThreadLocal();
	static private ThreadLocal<String> fqan = new ThreadLocal();

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
		return fqan.get();
	}

	/**
	 * Set the certificate chain of the currently connected client
	 * @param cert
	 */
	static public void setUserCertificateChain(X509Certificate[] chain) {
		certificateChain.set(chain);
		X509Certificate cert = ProxyUtils.getEndUserCertificate(chain);
		if (cert == null) {
			log.warn("No client certificate found in the supplied certificate chain");
		} else {
			certificate.set(cert);
		}
		VOMSACValidator validator = VOMSValidators.newValidator();
		List<VOMSAttribute> attrs = validator.validate(chain);
		for (VOMSAttribute attr : attrs) {
			for (String fqan_str : attr.getFQANs()) {
				fqan.set(fqan_str);
				break;
			}
			break;
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
		GUMSAPIImpl api = new GUMSAPIImpl();
		api.cleanAccounts();
	}

	public void doFilter(javax.servlet.ServletRequest servletRequest,
			javax.servlet.ServletResponse servletResponse,
			javax.servlet.FilterChain filterChain) throws java.io.IOException,
			javax.servlet.ServletException {
		reset();
		Object sslIdObj = servletRequest.getAttribute("javax.servlet.request.ssl_session");
		/**
		 * This merits some explaining.
		 *
		 * If you use the Http11AprProtocol Connector, then OpenSSL is used for the SSL layer.
		 * The OpenSSL server - unlike Java native support - will use SSL session reuse with
		 * browsers such as Chrome.  When the SSL session is reused, *then* the container doesn't
		 * have access to the client X509 certificate.  Hence, for browsers (as no known CLI
		 * performs reuse), we create a session with the saved X509 certificate chain.  Further,
		 * as we can't disable SSLv3 right now (which can expose session cookies), we record the
		 * SSL session ID and only reuse the X509 certificate chain if the SSL session ID was not
		 * reused.
		 *
		 * Note we work around an apparent bug in the native connector where ssl_session is not set
		 * for the initial session, but only on reuse.  As POODLE requires multiple requests (and
		 * we record the SSL ID on the second request), this should be acceptable.
		 *
		 * If we disable SSLv3, we can just use the session cookie again to persist the credentials
		 * (what we do right now is technically invalid as a browser might use a pool of SSL connections
		 * for the same session).
		 *
		 * The session cookie should create more overhead than "normal" certificate processing, which
		 * is why we limit its use to browsers.
		 *
		 * NOTE: This code is all specific to APR users; the default GUMS deploy (using EMI trustmanager)
		 * will never trigger any of this.
		 */
		if (servletRequest
				.getAttribute("javax.servlet.request.X509Certificate") != null) {
			X509Certificate[] chain = ((X509Certificate[]) servletRequest
					.getAttribute("javax.servlet.request.X509Certificate"));
			setUserCertificateChain(chain);
			if (servletRequest instanceof HttpServletRequest)
			{
				HttpServletRequest httpRequest = (HttpServletRequest)servletRequest;
				String agent = httpRequest.getHeader("User-Agent");
				String sslId = (String)sslIdObj;
				if ((agent != null) && agent.startsWith("Mozilla"))
				{
					HttpSession session = httpRequest.getSession();
						// Note that, in tomcat6 / APR, SSL ID is null!
					session.setAttribute("javax.servlet.request.ssl_session", sslId);
					session.setAttribute("javax.servlet.request.X509Certificate", chain);
				}
			}
		}
		else if (servletRequest instanceof HttpServletRequest)
		{
			HttpServletRequest httpRequest = (HttpServletRequest)servletRequest;
			HttpSession session = httpRequest.getSession(false);
			String sslId = (String)sslIdObj;
			if (session != null)
			{
				X509Certificate[] chain = ((X509Certificate[]) session.getAttribute("javax.servlet.request.X509Certificate"));
				String sslIdSaved = (String)session.getAttribute("javax.servlet.request.ssl_session");
				if (sslIdSaved == null)
				{
					sslIdSaved = sslId;
					session.setAttribute("javax.servlet.request.ssl_session", sslId);
				}
				if ((chain != null) && sslId.equals(sslIdSaved))
				{
					setUserCertificateChain(chain);
				}
				else if (!sslId.equals(sslIdSaved))
				{
					log.warn("Remote user attempted to reuse a session cookie for a different SSL ID!  Rejecting; this should not happen during regular non-malicious use.");
				}
			}
		}
		if (certificate.get() == null) {
			// TODO: Improve log to include client information.
			log.warn("Missing client certificate from request.");
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
