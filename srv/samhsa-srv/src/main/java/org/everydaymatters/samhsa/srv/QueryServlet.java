/*
    MyLifeMatters
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
package org.everydaymatters.samhsa.srv;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.everydaymatters.venti.VentiSecurityHelper;
import org.everydaymatters.venti.VentiWrappedServletRequest;

public class QueryServlet
    extends HttpServlet
{
    public static final String VERSION = "1.0.4";
    
    public static final String QUERY_JOURNAL = "journal";
    public static final String QUERY_FB_PROFILE = "fbProfile";
    public static final String QUERY_ASSESS_REF = "assessRef";
    public static final String QUERY_FACILITY = "facility";
    
    public static final float DEFAULT_MAX_MILES = 30.0f;
    
    protected static final Pattern PTN_PROFILE = Pattern.compile( "/profile/(\\w+)/(\\w+)" );
    protected static final Pattern PTN_FB_PROFILE = Pattern.compile( "/fbProfile/(\\w+)" );
    protected static final Pattern PTN_ASSESS_REF = Pattern.compile( "/assess/(\\w+)" );
    protected static final Pattern PTN_FAC_ZIP = Pattern.compile( "/facilities/nearZip/(\\w+)" );
    protected static final Pattern PTN_FAC_LOC = Pattern.compile( "/facilities/nearLoc/([\\.\\-\\d]+),([\\.\\-\\d]+)" );
        
    protected static Properties QUERY_URI = new Properties();
    
    static
    {
        QUERY_URI.setProperty( QUERY_JOURNAL, "/rs/crud/activity" );
        QUERY_URI.setProperty( QUERY_FB_PROFILE, "/rs/crud/profile" );
        QUERY_URI.setProperty( QUERY_ASSESS_REF, "/rs/crud/assessment" );
        QUERY_URI.setProperty( QUERY_FACILITY, "/rs/crud/facility" );
    }
    
    protected ServletContext application;
    protected Properties secrets;
    protected VentiSecurityHelper security;
    
    @Override
    public void init( ServletConfig config )
        throws ServletException
    {
        super.init( config );
        
        try
        {
            application = config.getServletContext();
        
            security = (VentiSecurityHelper) application.getAttribute( VentiSecurityHelper.ATTR_NAME );
            if ( security==null )
            {
                security = new VentiSecurityHelper();
                security.configureForServlet( application, this );
            }
        }
        catch ( Throwable t )
        {
            throw new ServletException( "Exception on initialization.", t );
        }
    }
    
    protected static String mongoProfileQuery( String profileId )
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append( "{\"profile\":\"" );
        sb.append( profileId );
        sb.append( "\"} ");
        
        return sb.toString();
    }
    
    protected static String mongoFbProfileQuery( String fbProfileId )
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append( "{\"facebook.user\":\"" );
        sb.append( fbProfileId );
        sb.append( "\"} ");
        
        return sb.toString();
    }

    protected static String mongoAssessRefQuery( String assessRef )
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append( "{\"ref\":\"" );
        sb.append( assessRef );
        sb.append( "\"} ");
        
        return sb.toString();
    }
    
    protected static String mongoFacZipQuery( String zip )
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append( "{\"LOC_ZIP\":\"" );
        sb.append( zip );
        sb.append( "\"} ");
        
        return sb.toString();
    }
    
    protected static String mongoFacLocQuery( Double lat, Double lon )
    {
        return mongoFacLocQuery( lat, lon, null );
    }
    
    protected static String mongoFacLocQuery( Double lat, Double lon, Number maxMiles )
    {
        StringBuffer sb = new StringBuffer();
        
        double maxMi = DEFAULT_MAX_MILES;
        
        if ( maxMiles!=null )
        {
            maxMi = maxMiles.doubleValue();
        }
        
        sb.append( "{loc:{$nearSphere:{ $geometry:{ type:\"Point\", coordinates:[" );
        sb.append( lat.doubleValue() );
        sb.append( "," );
        sb.append( lon.doubleValue() );
        sb.append( "] }, $maxDistance: " );
        sb.append( maxMi * 1609.3 );
        sb.append( " }}}" );
        
        return sb.toString();
    }
    

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse rep )
        throws ServletException, IOException
    {
        if ( security.isSecurityValid( req ) )
        {
            String path = req.getPathInfo();
            
            Matcher profile = PTN_PROFILE.matcher( path );
            Matcher fbProfile = PTN_FB_PROFILE.matcher( path );
            Matcher assessRef = PTN_ASSESS_REF.matcher( path );
            Matcher facZip = PTN_FAC_ZIP.matcher( path );
            Matcher facLoc = PTN_FAC_LOC.matcher( path );
            
            String error = null;
            
            String mq = null;
            String quri = null;
            

            if ( profile.matches() )
            {
                String profileId = profile.group( 1 );
                String query = profile.group( 2 );
                
                mq = mongoProfileQuery( profileId );
                quri = QUERY_URI.getProperty( query );
            }
            else if ( fbProfile.matches() )
            {
                String fbProfileId = fbProfile.group( 1 );
                
                mq = mongoFbProfileQuery( fbProfileId );
                quri = QUERY_URI.getProperty( QUERY_FB_PROFILE );
            }
            else if ( assessRef.matches() )
            {
                String ref = fbProfile.group( 1 );
                
                mq = mongoFbProfileQuery( ref );
                quri = QUERY_URI.getProperty( QUERY_ASSESS_REF );
            }
            else if ( facLoc.matches() )
            {
                String lat = facLoc.group( 1 );
                String lon = facLoc.group( 2 );
                
                try
                {
                    Double dLat = Double.parseDouble( lat );
                    Double dLon = Double.parseDouble( lon );
                    
                    mq = mongoFacLocQuery( dLat, dLon );
                    quri = QUERY_URI.getProperty( QUERY_FACILITY );
                }
                catch ( NumberFormatException nfe )
                {
                    error = nfe.getMessage();
                }
            }
            else if ( facZip.matches() )
            {
                String zip = facZip.group( 1 );
                
                mq = mongoFacZipQuery( zip );
                quri = QUERY_URI.getProperty( QUERY_FACILITY );
            }
            else
            {
                error = "Bad request.";
            }
            
            if ( quri!=null && mq!=null )
            {
                try
                {
                    HttpServletRequest wreq = req;

                    String uri = quri + "?query=" + URLEncoder.encode( mq );
                    String client = req.getHeader( VentiSecurityHelper.HEAD_CLIENT_ID );

                    if ( client!=null )
                    {
                        String newHash = security.createMessageHash( uri, client );
                        wreq = new VentiWrappedServletRequest( req, client, newHash );
                    }

                    RequestDispatcher rd = req.getRequestDispatcher( uri );
                    rd.forward( wreq, rep);
                }
                catch ( Exception e )
                {
                    throw new ServletException( e );
                }
            }
            else
            {
                error = "Unknown query.";
            }

            if ( error!=null )
            {
                rep.sendError( HttpServletResponse.SC_BAD_REQUEST, error );
            }
        }
        else
        {
            rep.sendError( HttpServletResponse.SC_FORBIDDEN );
        }
    }
}
