/*
    Venti RESTful MongoDB 
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
package org.everydaymatters.venti;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

public class VentiSecurityHelper
{
    public static final String HEAD_CLIENT_ID = "X-Venti-ClientID";
    public static final String HEAD_HASH = "X-Venti-Hash";
    public static final String PARAM_CLIENT_ID = "venti_client";
    public static final String PARAM_HASH = "venti_hash";
    
    public static final String ATTR_NAME = "VentiSecurityHelper";
    
    public static final String PARAM_MESSAGE_SECURITY = "venti-secure";
    public static final String DEFAULT_MESSAGE_SECURITY = "true";
    
    public static final String PARAM_MESSAGE_SECRETS = "venti-secure-secrets";
    public static final String DEFAULT_MESSAGE_SECRETS = "/WEB-INF/secrets.properties";
    
    protected boolean checkSecurity;
    protected Properties secrets;
    
    public VentiSecurityHelper()
    {
        checkSecurity = true;
        secrets = new Properties();
    }
    
    protected static String resolveParameter( String pname, String primary, ServletContext application, String dvalue )
    {
        String value = primary;
        
        if ( value==null )
        {
            value = application.getInitParameter( pname );
            
            if ( value==null )
            {
                value = dvalue;
            }
        }
        
        return value;
    }
    
    protected void finishConfig( String secretFilePath )
        throws IOException
    {
        finishConfig( secretFilePath, null );
    }
    
    protected void finishConfig( String secretFilePath, ServletContext application )
        throws IOException
    {
        if ( checkSecurity )
        {
            FileReader secretReader = null;
            
            try
            {
                File secretFile = new File( secretFilePath );
                if ( secretFile.exists() && secretFile.canRead() )
                {
                    secretReader = new FileReader( secretFile );
                    secrets.load( secretReader );
                }
                
                if ( secrets.size()<1 && application!=null )
                {
                    secretFilePath = application.getRealPath( secretFilePath );
                    secretFile = new File( secretFilePath );
                    if ( secretFile.exists() && secretFile.canRead() )
                    {
                        secretReader = new FileReader( secretFile );
                        secrets.load( secretReader );
                    }
                }
            }
            catch ( IOException ioe )
            {
                throw ioe;
            }
            finally
            {
                if ( secretReader!=null )
                {
                    try
                    {
                        secretReader.close();
                    }
                    catch ( IOException ioe )
                    {
                        ioe.printStackTrace();
                    }
                }
            }
        }
    }
    
    public void configureForFilter( ServletContext application, FilterConfig config )
        throws IOException
    {
        checkSecurity = 
            Boolean.parseBoolean( 
                resolveParameter( 
                    PARAM_MESSAGE_SECURITY, config.getInitParameter( PARAM_MESSAGE_SECURITY ),
                    application, DEFAULT_MESSAGE_SECURITY ) );
        
        finishConfig(
            resolveParameter( 
                PARAM_MESSAGE_SECRETS, config.getInitParameter( PARAM_MESSAGE_SECRETS ),
                application, DEFAULT_MESSAGE_SECRETS), application );
        
        application.setAttribute( ATTR_NAME, this );
    }
    
    public void configureForServlet( ServletContext application, HttpServlet servlet )
        throws IOException
    {
        checkSecurity = 
            Boolean.parseBoolean( 
                resolveParameter( 
                    PARAM_MESSAGE_SECURITY, servlet.getInitParameter( PARAM_MESSAGE_SECURITY ),
                    application, DEFAULT_MESSAGE_SECURITY ) );
        
        finishConfig(
            resolveParameter( 
                PARAM_MESSAGE_SECRETS, servlet.getInitParameter( PARAM_MESSAGE_SECRETS ),
                application, DEFAULT_MESSAGE_SECRETS ), application );
        
        application.setAttribute( ATTR_NAME, this );
    }
    
    protected String assembleMessageToHash( HttpServletRequest req )
    {
        StringBuffer sb = new StringBuffer();
        sb.append( req.getRequestURI() );
        
        String qs = req.getQueryString();
        if ( qs!=null )
        {
            sb.append( "?" );
            sb.append( qs );
        }
        
        return sb.toString();
    }
    
    public String createMessageHash( String msg, String clientId )
        throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException
    {
        String secret = secrets.getProperty( clientId );
        SecretKeySpec key = new SecretKeySpec( secret.getBytes( "UTF-8" ), "HmacSHA256" );
        Mac mac = Mac.getInstance( "HmacSHA256" );
        mac.init( key );
        byte[] bytes = mac.doFinal( msg.getBytes( "ASCII" ) );

        StringBuffer sb = new StringBuffer();

        for ( int i=0; i<bytes.length; i++ )
        {
            String hex = Integer.toHexString( 0xFF & bytes[i] );
            if ( hex.length()==1 )
            {
                sb.append( "0" );
            }

            sb.append( hex );
        }

        return sb.toString();
    }
    
    protected String getHeaderOrParameter( String hname, String pname, HttpServletRequest req )
    {
        String value = req.getHeader( hname );
        
        if ( value==null )
        {
            value = req.getParameter( pname );
        }
        
        return value;
    }
    
    public boolean isHashValid( String clientId, String message, String hash )
    {
        boolean valid = false;
        
        if ( checkSecurity )
        {
            try
            {
                String target = createMessageHash( message, clientId );
                valid = target.equals( hash );
            }
            catch ( Exception e )
            {
                valid = false;
                e.printStackTrace();
            }
        }
        else
        {
            valid = true;
        }
        
        return valid;
    }
    
    public boolean isSecurityValid( HttpServletRequest req )
    {
        String clientId = getHeaderOrParameter( HEAD_CLIENT_ID, PARAM_CLIENT_ID, req );
        String hash = getHeaderOrParameter( HEAD_HASH, PARAM_HASH, req );
        return isHashValid( clientId, assembleMessageToHash( req ), hash );
    }
}
