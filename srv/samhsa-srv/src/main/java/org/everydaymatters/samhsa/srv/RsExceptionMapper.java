/*
    MyLifeMatters
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
package org.everydaymatters.samhsa.srv;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.everydaymatters.samhsa.msg.ErrorMessage;

@Provider
public class RsExceptionMapper
    implements ExceptionMapper<RsException>
{
    @Context
    private HttpHeaders headers;
    
    public Response toResponse( RsException ex )
    {
        ErrorMessage em = new ErrorMessage();
        em.setDescription( ex.getDescription() );
        em.setStatus( ex.getStatus() );
        em.setTrace( ex.getTrace() );
        
        return Response.status( ex.getStatus() ).entity( em ).type( headers.getMediaType() ).build();
    }
}
