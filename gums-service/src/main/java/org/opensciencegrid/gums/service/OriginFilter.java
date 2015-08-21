
package org.opensciencegrid.gums.service;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Filter out any requests with the Origin filter
 * set.
 *
 * This should help prevent CSRF attacks againt the Axis
 * web service as client certificates are used there.
 */

public class OriginFilter implements Filter {

    private final static Logger log = Logger.getLogger("org.glite.security");

    @Override
    public void init(javax.servlet.FilterConfig _) throws ServletException
    {
    }


    @Override
    public void destroy()
    {
    }

    public void doFilter(javax.servlet.ServletRequest servletRequest,
                         javax.servlet.ServletResponse servletResponse,
                         javax.servlet.FilterChain filterChain) throws java.io.IOException,
                         javax.servlet.ServletException
    {
        if (servletRequest instanceof HttpServletRequest)
        {
            HttpServletRequest request = (HttpServletRequest)servletRequest;
            if (request.getHeader("origin") != null)
            {
                String rootPath = "/gums";
                rootPath = request.getContextPath();
                log.warn("Detected a redirect to origin-filtered resource: " + request.getRequestURL());
                if (servletResponse instanceof HttpServletResponse)
                {
                     ((HttpServletResponse)servletResponse).sendRedirect(rootPath + "/csrf.jsp");
                }
                else {throw new javax.servlet.ServletException("Unable to handle non-HTTP filters.");}
            }
        }
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Exception e) {
            throw new javax.servlet.ServletException("Unhandled failure in servlet processing.", e);
        }
    }

}
	
