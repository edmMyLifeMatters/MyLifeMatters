/*
    MyLifeMatters
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
package org.everydaymatters.samhsa.srv;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

public class RsException
    extends WebApplicationException
{
    int status;
    String description;
    String trace;
    
    public RsException( int statusCode, String message )
    {
        super( statusCode );
        this.status = statusCode;
        this.description = message;
        this.trace = null;
    }
    
    public RsException( Status statusCode, String message )
    {
        super( statusCode );
        this.status = statusCode.getStatusCode();
        this.description = message;
        this.trace = null;
    }
    
    public RsException( int statusCode, String message, Throwable t )
    {
        super( t, statusCode );
        this.status = statusCode;
        this.description = message;
        setCause( t );
    }
    
    public RsException( Status statusCode, String message, Throwable t )
    {
        super( t, statusCode );
        this.status = statusCode.getStatusCode();
        this.description = message;
        setCause( t );
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String message )
    {
        this.description = message;
    }
    
    protected void setCause( Throwable t )
    {
        this.trace = null;
        
        try
        {
            StringWriter sw = new StringWriter();
            t.printStackTrace( new PrintWriter( sw ) );
            this.trace = sw.toString();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    public int getStatus()
    {
        return status;
    }

    public void setStatus( int status )
    {
        this.status = status;
    }

    public String getTrace()
    {
        return trace;
    }

    public void setTrace( String trace )
    {
        this.trace = trace;
    }
}
