/*
    MyLifeMatters
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
package org.everydaymatters.samhsa.msg;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ErrorMessage
    implements Serializable
{
    int status;
    String description;
    String trace;
    
    public ErrorMessage()
    {
        status = 0;
        description = trace = null;
    }
    
    public String getDescription()
    {
        return description;
    }

    public void setDescription( String message )
    {
        this.description = message;
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
