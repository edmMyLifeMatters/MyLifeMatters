/*
    Venti RESTful MongoDB 
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
package org.everydaymatters.venti;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class VentiWrappedServletRequest
    extends HttpServletRequestWrapper
{
    String clientId;
    String hash;
    
    public VentiWrappedServletRequest( HttpServletRequest req, String clientId, String hash )
    {
        super( req );
        this.clientId = clientId;
        this.hash = hash;
    }

    @Override
    public String getHeader( String name )
    {
        String value = super.getHeader( name );
        
        if ( value.equals( VentiSecurityHelper.HEAD_CLIENT_ID ) )
        {
            return clientId;
        }
        else if ( value.equals( VentiSecurityHelper.HEAD_HASH ) )
        {
            return hash;
        }
        
        return value;
    }
}
