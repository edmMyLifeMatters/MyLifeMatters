/*
    Venti RESTful MongoDB 
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
package org.everydaymatters.venti;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class VentiSecurityFilter
    implements Filter
{
    protected FilterConfig config;
    protected ServletContext application;
    protected VentiSecurityHelper security;

    public void init( FilterConfig filterConfig )
        throws ServletException
    {
        config = filterConfig;
        application = config.getServletContext();
        
        security = (VentiSecurityHelper) application.getAttribute( VentiSecurityHelper.ATTR_NAME );
        
        if ( security==null )
        {
            security = new VentiSecurityHelper();
            
            try
            {
                security.configureForFilter( application, config );
            }
            catch ( Throwable t )
            {
                throw new ServletException( "Exception on initialization.", t );
            }
        }
    }

    public void destroy()
    {
        security = null;
        application = null;
        config = null;
    }    

    public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain )
        throws IOException, ServletException
    {
        if ( security.isSecurityValid( (HttpServletRequest) request ) )
        {
            chain.doFilter( request, response );
        }
        else
        {
            HttpServletResponse hrep = (HttpServletResponse) response;
            hrep.sendError( HttpServletResponse.SC_FORBIDDEN );
        }
    }
}
