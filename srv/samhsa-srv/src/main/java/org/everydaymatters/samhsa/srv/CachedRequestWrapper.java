/*
    MyLifeMatters
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
package org.everydaymatters.samhsa.srv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class CachedRequestWrapper
    extends HttpServletRequestWrapper    
{
    String myContentType;
    int myContentLength;
    BufferedReader bdr;
    CachedServletInputStream myInputStream;
    
    public CachedRequestWrapper( HttpServletRequest origRequest, 
                                 String contentType, byte[] cache )
    {
        super( origRequest );
        myInputStream = new CachedServletInputStream( cache );        
        
        myContentType = contentType;
        myContentLength = cache.length;
        /*DEBUG*/System.err.println( "CRWb() -- "+myContentLength+" of "+myContentType );
    }
    
    public CachedRequestWrapper( HttpServletRequest origRequest, 
                                 String contentType, String cache )
    {
        super( origRequest );
        myInputStream = new CachedServletInputStream( cache );        
        bdr = new BufferedReader( new StringReader( cache ) );
        
        myContentType = contentType;
        myContentLength = cache.length();
        /*DEBUG*/System.err.println( "CRWs() -- "+myContentLength+" of "+myContentType );
    }    

    /*
    public String getMethod()
    {
        return "POST";
    }
     */
    
    public String getContentType()
    {
        return myContentType;
    }
    
    public int getContentLength()
    {
        return myContentLength;
    }

    public BufferedReader getReader()
        throws IOException
    {
        return new BufferedReader( new InputStreamReader( myInputStream ) );
    }
    
    public ServletInputStream getInputStream()
        throws IOException
    {
        /*DEBUG*/System.err.println( "CRW.gIS()" );
        return myInputStream;
    }        
}
