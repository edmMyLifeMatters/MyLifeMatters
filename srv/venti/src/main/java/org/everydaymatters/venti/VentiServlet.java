/*
    Venti RESTful MongoDB 
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
package org.everydaymatters.venti;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;
import com.mongodb.jee.servlet.ServletHelper;
import com.mongodb.util.JSON;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.bson.types.ObjectId;

public class VentiServlet
    extends HttpServlet
{
    public static final String VERSION = "1.0.1";
    
    public static final String PARAM_MONGO_JNDI = "mongo-jndi";
    public static final String DEFAULT_MONGO_JNDI = "mongodb/venti";
    
    public static final String PARAM_MESSAGE_SECURITY = "venti-secure";
    public static final String DEFAULT_MESSAGE_SECURITY = "true";
    
    public static final String PARAM_MONGO_DBNAME = "mongo-db-name";
    public static final String DEFAULT_MONGO_DBNAME = "venti";
    
    public static final String PARAM_ID_STYLE = "mongo-id-style";
    public static final String ID_STYLE_P_NATIVE = "_id";
    public static final String ID_STYLE_P_SIMPLE = "id";
    public static final String ID_STYLE_P_GUID = "guid";
    public static final String DEFAULT_ID_STYLE = ID_STYLE_P_NATIVE;
    public enum IdStyle { NATIVE, SIMPLE, GUID }
    
    public static final String PARAM_VENTI_BLOB_RESOURCE = "venti-blobs";
    public static final String DEFAULT_VENTI_BLOB_RESOURCE = "blob";
    
    public static final String PARAM_MONGO_RESULT_SIZE = "mongo-result-size";
    public static final String DEFAULT_MONGO_RESULT_SIZE = "64";
    
    public static final String PARAM_QUERY = "query";
    public static final String PARAM_START_AT = "skip";
    public static final String PARAM_MAX_RESULTS = "limit";
    
    protected static final Pattern PTN_DOCUMENT = Pattern.compile( "/(\\w+)/(\\w+)" );
    protected static final Pattern PTN_COLLECTION = Pattern.compile( "/(\\w+)/{0,1}" );
    
    public static final String ATTR_DOCTYPE = "docType";
    public static final String ATTR_CREATED = "created";
    public static final String ATTR_MODIFIED = "updated";
    protected static final String ATTR_TYPE = "mimeType";
    protected static final String ATTR_DATA = "data";
    
    public static final DateFormat DATE_DTF = new SimpleDateFormat( "yyyy-MM-dd" );
    public static final DateFormat TIME_DTF = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSSZ" );
    
    
    protected ServletContext application;
    protected MongoClient mongo;
    protected String mongoDbName;
    
    protected boolean checkSecurity;
    protected Properties secrets;
    protected VentiSecurityHelper security;
    
    protected IdStyle idStyle;
    protected String blobCollName;
    protected String defMaxResults;
    
    
    public static IdStyle getParameterValueIdStyle( String pvalue )
    {
        IdStyle value = IdStyle.NATIVE;
        
        if ( pvalue.equalsIgnoreCase( ID_STYLE_P_SIMPLE ) )
        {
            value = IdStyle.SIMPLE;
        }
        else if ( pvalue.equalsIgnoreCase( ID_STYLE_P_GUID ) )
        {
            value = IdStyle.GUID;
        }
        
        return value;
    }
    
    public static String getIdStyleParameterValue( IdStyle style )
    {
        String value = null;
        
        switch ( style )
        {
            case SIMPLE:
                value = ID_STYLE_P_SIMPLE;
                break;
                
            case GUID:
                value = ID_STYLE_P_GUID;
                break;
                
            default:
            case NATIVE:
                value = ID_STYLE_P_NATIVE;
                break;
        }
        
        return value;
    }
    
    protected String getInitParamOrDefault( String paramName, String defaultValue )
    {
        String value = getInitParameter( paramName );
        
        if ( value==null && application!=null )
        {
            value = application.getInitParameter( paramName );
        }
        
        if ( value==null )
        {
            value = defaultValue;
        }
        
        return value;
    }
    
    protected static String getReqParamOrdefault( HttpServletRequest req, String paramName, String defaultValue )
    {
        String value = req.getParameter( paramName );
        
        if ( value==null )
        {
            value = defaultValue;
        }
        
        return value;
    }

    @Override
    public void init( ServletConfig config )
        throws ServletException
    {
        super.init( config );
        
        try
        {
            application = config.getServletContext();
            String jndiUrl = getInitParamOrDefault( PARAM_MONGO_JNDI, DEFAULT_MONGO_JNDI );
            log( "Initializing with MongoClient from JNDI: "+jndiUrl );
        
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            mongo = (MongoClient) envCtx.lookup( jndiUrl );
            mongoDbName = getInitParamOrDefault( PARAM_MONGO_DBNAME, DEFAULT_MONGO_DBNAME );
            blobCollName = getInitParamOrDefault( PARAM_VENTI_BLOB_RESOURCE, DEFAULT_VENTI_BLOB_RESOURCE );
            defMaxResults = getInitParamOrDefault( PARAM_MONGO_RESULT_SIZE, DEFAULT_MONGO_RESULT_SIZE );
            
            String pIdStyle = getInitParamOrDefault( PARAM_ID_STYLE, DEFAULT_ID_STYLE );
            idStyle = getParameterValueIdStyle( pIdStyle );
            
            checkSecurity = Boolean.parseBoolean( getInitParamOrDefault( PARAM_MESSAGE_SECURITY, DEFAULT_MESSAGE_SECURITY ) );
            
            if ( checkSecurity )
            {
                security = (VentiSecurityHelper) application.getAttribute( VentiSecurityHelper.ATTR_NAME );
                if ( security==null )
                {
                    security = new VentiSecurityHelper();
                    security.configureForServlet( application, this );
                }
            }
        }
        catch ( Throwable t )
        {
            throw new ServletException( "Exception on initialization.", t );
        }
    }
    
    protected void conformObjectStyle( DBObject dbo )
    {
        conformObjectStyle( dbo, null );
    }
    
    protected void conformObjectStyle( DBObject dbo, String docTypeName )
    {
        if ( docTypeName!=null )
        {
            dbo.put( ATTR_DOCTYPE, docTypeName );
        }
        
        if ( idStyle!=IdStyle.NATIVE )
        {
            String idAttr = getIdStyleParameterValue( idStyle );
            
            if ( dbo.containsField( ID_STYLE_P_NATIVE ) )
            {
                ObjectId oid = (ObjectId) dbo.get( ID_STYLE_P_NATIVE );
                dbo.put( idAttr, oid.toString() );
                dbo.removeField( ID_STYLE_P_NATIVE );
            }
            else if ( dbo.containsField( idAttr ) )
            {
                Object id = dbo.get( idAttr );
                ObjectId oid = new ObjectId( id.toString() );
                dbo.put( ID_STYLE_P_NATIVE, oid );
                dbo.removeField( idAttr );
            }
        }        
        
        if ( dbo.containsField( ATTR_CREATED ) )
        {
            Object cob = dbo.get( ATTR_CREATED );
            if ( cob!=null && cob instanceof Date )
            {
                Date created = (Date) cob;
                dbo.put( ATTR_CREATED, TIME_DTF.format( created ) );
            }
        }
        
        if ( dbo.containsField( ATTR_MODIFIED ) )
        {
            Object mob = dbo.get( ATTR_MODIFIED );
            if ( mob!=null && mob instanceof Date )
            {
                Date modified = (Date) mob;
                dbo.put( ATTR_MODIFIED, TIME_DTF.format( modified ) );
            }
        }

        // TODO: conform any other date/time formats
    }
    
    protected boolean processSecurityCheck( HttpServletRequest req, HttpServletResponse rep )
        throws IOException
    {
        boolean passed = true;
        
        if ( checkSecurity )
        {
            passed = security.isSecurityValid( req );
            
            if ( ! passed )
            {
                rep.sendError( HttpServletResponse.SC_FORBIDDEN );
            }
        }
        
        return passed;
    }
    

    @Override
    protected void doPost( HttpServletRequest req, HttpServletResponse rep )
        throws ServletException, IOException
    {
        if ( processSecurityCheck( req, rep ) )
        {
            String path = req.getPathInfo();
            Matcher list = PTN_COLLECTION.matcher( path );
            
            if ( list.matches() )
            {
                String collection = list.group( 1 );
                
                InputStream is = null;
                ByteArrayOutputStream baos = null;
                BufferedReader br = null;
                DB db = null;
                DBObject dbo = null;
                
                try
                {
                    db = mongo.getDB( mongoDbName );
                    db.requestStart();
                    db.requestEnsureConnection();
                    
                    DBCollection coll = db.getCollection( collection );
                    if ( collection.equalsIgnoreCase( blobCollName ) )
                    {
                        String mimeType = req.getContentType();
                        
                        is = new BufferedInputStream( req.getInputStream() );
                        baos = new ByteArrayOutputStream();
                        
                        int b = is.read();
                        while ( b > -1 )
                        {
                            baos.write( b );
                            b = is.read();
                        }

                        baos.flush();
                        byte[] data = baos.toByteArray();
                        
                        dbo = new BasicDBObject();
                        dbo.put( ATTR_TYPE, mimeType );
                        dbo.put( ATTR_DATA, data );
                    }
                    else
                    {
                        br = req.getReader();
                        StringBuffer sb = new StringBuffer();
                        String line = br.readLine();
                        while ( line!=null )
                        {
                            sb.append( line );
                            line = br.readLine();
                        }
                        String body = sb.toString();
                        
                        dbo = (DBObject) JSON.parse( body );
                        conformObjectStyle( dbo );
                    }
                    
                    dbo.put( ATTR_CREATED, new Date() );

                    coll.insert( dbo );

                    if ( dbo!=null )
                    {
                        if ( dbo.containsField( ATTR_DATA ) )
                        {
                            dbo.removeField( ATTR_DATA );
                        }
                        
                        conformObjectStyle( dbo, collection );
                        ServletHelper.writeJson( dbo, rep );
                    }
                    else
                    {
                        rep.sendError( HttpServletResponse.SC_NOT_FOUND );
                    }
                }
                catch ( Throwable t )
                {
                    throw new ServletException( "Exception while processing request.", t );
                }
                finally
                {
                    if ( db!=null )
                    {
                        db.requestDone();
                    }
                    
                    if ( br!=null )
                    {
                        br.close();
                    }
                    
                    if ( is!=null )
                    {
                        is.close();
                    }
                }
            }
            else
            {
                rep.sendError( HttpServletResponse.SC_BAD_REQUEST );
            }
        }
    }

    @Override
    protected void doDelete( HttpServletRequest req, HttpServletResponse rep )
        throws ServletException, IOException
    {
        if ( processSecurityCheck( req, rep ) )
        {
            String path = req.getPathInfo();
            Matcher single = PTN_DOCUMENT.matcher( path );
            
            if ( single.matches() )
            {
                String collection = single.group( 1 );
                String id = single.group( 2 );
                
                DB db = null;
                PrintWriter pw = null;
                
                try
                {
                    db = mongo.getDB( mongoDbName );
                    db.requestStart();
                    db.requestEnsureConnection();
                    
                    DBCollection coll = db.getCollection( collection );
                    
                    DBObject query = new BasicDBObject( "_id", new ObjectId( id ) );
                    WriteResult result = coll.remove( query );
                    
                    pw = rep.getWriter();
                    pw.print( result.getError() );
                }
                catch ( Throwable t )
                {
                    throw new ServletException( "Exception while processing request.", t );
                }
                finally
                {
                    if ( db!=null )
                    {
                        db.requestDone();
                    }
                    
                    if ( pw!=null )
                    {
                        pw.close();
                    }
                }
            }
            else
            {
                rep.sendError( HttpServletResponse.SC_BAD_REQUEST );
            }
        }
    }
    
    @Override
    protected void doPut( HttpServletRequest req, HttpServletResponse rep )
        throws ServletException, IOException
    {
        if ( processSecurityCheck( req, rep ) )
        {
            String path = req.getPathInfo();
            Matcher single = PTN_DOCUMENT.matcher( path );
            
            if ( single.matches() )
            {
                String collection = single.group( 1 );
                String id = single.group( 2 );
                
                if ( collection.equalsIgnoreCase( blobCollName ) )
                {
                    rep.sendError( HttpServletResponse.SC_METHOD_NOT_ALLOWED );
                }
                else
                {
                    BufferedReader br = null;
                    DB db = null;

                    try
                    {
                        br = req.getReader();
                        StringBuffer sb = new StringBuffer();
                        String line = br.readLine();
                        while ( line!=null )
                        {
                            sb.append( line );
                            line = br.readLine();
                        }
                        String body = sb.toString();
                        DBObject incoming = (DBObject) JSON.parse( body );
                        conformObjectStyle( incoming );

                        ObjectId oid  = new ObjectId( id );
                        DBObject query = new BasicDBObject( "_id", oid );

                        incoming.put( "_id", oid );
                        incoming.put( ATTR_MODIFIED, new Date() );

                        db = mongo.getDB( mongoDbName );
                        db.requestStart();
                        db.requestEnsureConnection();

                        DBCollection coll = db.getCollection( collection );
                        DBObject old = coll.findOne( new BasicDBObject( "_id", oid ) );

                        if ( old!=null )
                        {
                            incoming.put( ATTR_CREATED, old.get( ATTR_CREATED ) );
                            coll.findAndModify( query, incoming );
                            DBObject dbo = coll.findOne( query );
                            conformObjectStyle( dbo, collection );
                            ServletHelper.writeJson( dbo, rep );
                        }
                        else
                        {
                            rep.sendError( HttpServletResponse.SC_NOT_FOUND );
                        }
                    }
                    catch ( Throwable t )
                    {
                        throw new ServletException( "Exception while processing request.", t );
                    }
                    finally
                    {
                        if ( db!=null )
                        {
                            db.requestDone();
                        }

                        if ( br!=null )
                        {
                            br.close();
                        }
                    }
                }
            }
            else
            {
                rep.sendError( HttpServletResponse.SC_BAD_REQUEST );
            }
        }
    }
    
    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse rep )
        throws ServletException, IOException
    {
        if ( processSecurityCheck( req, rep ) )
        {
            String path = req.getPathInfo();
            Matcher single = PTN_DOCUMENT.matcher( path );
            Matcher list = PTN_COLLECTION.matcher( path );
            
            if ( single.matches() )
            {
                String collection = single.group( 1 );
                String id = single.group( 2 );
                
                OutputStream os = null;
                DB db = mongo.getDB( mongoDbName );
                
                try
                {
                    db.requestStart();
                    db.requestEnsureConnection();
                    
                    DBCollection coll = db.getCollection( collection );
                    DBObject dbo = coll.findOne( new BasicDBObject( "_id", new ObjectId( id ) ) );
                    
                    if ( dbo!=null )
                    {
                        if ( collection.equalsIgnoreCase( blobCollName ) )
                        {
                            byte[] data = (byte[]) dbo.get( ATTR_DATA );
                            rep.setContentType( (String) dbo.get( ATTR_TYPE ) );
                            rep.setContentLength( data.length );
                            
                            os = rep.getOutputStream();
                            os.write( data );
                        }
                        else
                        {
                            conformObjectStyle( dbo, collection );
                            ServletHelper.writeJson( dbo, rep );
                        }
                    }
                    else
                    {
                        rep.sendError( HttpServletResponse.SC_NOT_FOUND );
                    }
                }
                catch ( Throwable t )
                {
                    throw new ServletException( "Exception while processing request.", t );
                }
                finally
                {
                    db.requestDone();
                    
                    if ( os!=null )
                    {
                        os.close();
                    }
                }
            }
            else if ( list.matches() )
            {
                String collection = list.group( 1 );
                String query = req.getParameter( PARAM_QUERY );
                
                DB db = mongo.getDB( mongoDbName );
                PrintWriter pw = null;
                boolean blobList = collection.equalsIgnoreCase( blobCollName );
                
                try
                {
                    db.requestStart();
                    db.requestEnsureConnection();
                    
                    DBCollection coll = db.getCollection( collection );
                    DBCursor cursor = null;
                    
                    if ( query!=null )
                    {
                        cursor = coll.find( (DBObject) JSON.parse( query ) );
                    }
                    else
                    {
                        cursor = coll.find();
                    }
                    
                    // TODO: support pagination
                    
                    rep.setContentType( "application/json" );
                    pw = rep.getWriter();
                    pw.println( "[" );
                    
                    while ( cursor.hasNext() )
                    {
                        DBObject dbo = cursor.next();
                        conformObjectStyle( dbo, collection );
                        
                        if ( blobList )
                        {
                            byte[] data = (byte[]) dbo.get( ATTR_DATA );
                            dbo.removeField( ATTR_DATA );
                            dbo.put( "bytes", data.length ); 
                        }
                        
                        pw.print( JSON.serialize( dbo ) );
                        
                        if ( cursor.hasNext() )
                        {
                            pw.println( "," );
                        }
                    }
                    
                    pw.println( "]" );
                }
                catch ( Throwable t )
                {
                    throw new ServletException( "Exception while processing request.", t );
                }
                finally
                {
                    db.requestDone();
                    
                    if ( pw!=null )
                    {
                        pw.close();
                    }
                }
            }
            else
            {
                rep.sendError( HttpServletResponse.SC_BAD_REQUEST );
            }
        }   
    }
}
