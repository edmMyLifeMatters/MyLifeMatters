/*
    MyLifeMatters
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
package org.everydaymatters.samhsa.srv;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.everydaymatters.samhsa.msg.EntryMood;
import org.everydaymatters.samhsa.msg.HomeViewEntry;
import org.everydaymatters.samhsa.msg.HomeViewFeed;
import org.everydaymatters.venti.VentiServlet;

@Path( "/profile" )
public class ProfileResource
    extends BaseMongoResource
{
    public static final String VERSION = "1.0.5";
    
    protected static final String COLL_ACTIVITY = "activity";
    protected static final String COLL_INSPIRATION = "inspiration";
    protected static final String COLL_PLAN = "plan";
    
    protected static final String ATTR_ID = "_id";
    protected static final String ATTR_PROFILE = "profile";
    protected static final String ATTR_MEDIA = "media"; 
    protected static final String ATTR_CONTENT = "content";     
    protected static final String ATTR_NOTES = "notes"; 
    protected static final String ATTR_PHOTOS = "photos"; 
    protected static final String ATTR_QUOTES = "quotes"; 
    protected static final String ATTR_ARTICLES = "articles"; 
    protected static final String ATTR_LINK = "link"; 
    protected static final String ATTR_TITLE = "title"; 
    protected static final String ATTR_TEXT = "text"; 
    protected static final String ATTR_INSPIRATION = "inspiration"; 
    
    protected static final String ATTR_MOOD = "mood";
    protected static final String ATTR_LEVEL = "level";
    protected static final String ATTR_DESCRIPTION = "description";
    
    @Context
    protected UriInfo uriInfo;
    
    @Context
    protected Request request;
    
    @Context
    protected HttpServletRequest hsRequest;
    
    @Context
    protected ServletContext application;
    
    
    @GET
    @Path( "version" )
    @Produces( MediaType.TEXT_PLAIN )
    public String getVersion()
    {
        return VERSION;
    }
    
    protected static String getFieldContentsIfAvailable( DBObject dbo, String field )
    {
        String result = null;
        
        if ( dbo.containsField( field ) )
        {
            String contents = dbo.get( field ).toString();
            if ( contents!=null && contents.trim().length()>0 )
            {
                result = contents.trim();
            }
        }
        
        return result;
    }
    
    protected static HomeViewEntry conformHomeViewObject( DBObject dbo )
    {
        HomeViewEntry hve = new HomeViewEntry();
        
        hve.setRef( getFieldContentsIfAvailable( dbo, ATTR_ID ) );
        hve.setUrl( getFieldContentsIfAvailable( dbo, ATTR_MEDIA ) );
        hve.setText( getFieldContentsIfAvailable( dbo, ATTR_CONTENT ) );
        
        String notes = getFieldContentsIfAvailable( dbo, ATTR_NOTES );
        if ( notes!=null )
        {
            hve.setText( notes );
        }
        
        String link = getFieldContentsIfAvailable( dbo, ATTR_LINK );
        if ( hve.getUrl()==null && link!=null )
        {
            hve.setUrl( link );
        }
        
        String title = getFieldContentsIfAvailable( dbo, ATTR_TITLE );
        if ( title!=null )
        {
            hve.setTitle( title );
        }
        
        String text = getFieldContentsIfAvailable( dbo, ATTR_TEXT );
        if ( text!=null )
        {
            hve.setText( text );
        }
        
        Object date = dbo.get( VentiServlet.ATTR_CREATED );
        if ( date!=null )
        {
            if ( date instanceof Date )
            {
                Date dt = (Date) date;
                hve.setAddedDate( dt );
            }
            else
            {
                hve.setAdded( date.toString() );
            }
        }
        
        if ( hve.getTitle()==null && hve.getText()!=null )
        {
            int cap = 32;
            
            String etitle = hve.getText().trim();
            if ( etitle.length()>cap )
            {
                etitle = ( etitle.substring( 0, cap-2 ) + "..." );
            }
            
            hve.setTitle( etitle );
        }
        
        if ( dbo.containsField( ATTR_MOOD ) )
        {
            EntryMood em = new EntryMood();
            DBObject mdb = (DBObject) dbo.get( ATTR_MOOD );
            
            em.setDescription( getFieldContentsIfAvailable( mdb, ATTR_DESCRIPTION ) );
            
            try
            {
                em.setLevel( getFieldContentsIfAvailable( mdb, ATTR_LEVEL ) );
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
            
            if ( HomeViewEntry.isNotEmptyMood( em ) )
            {
                hve.setMood( em );
            }
        }
        
        return hve;
    }
    
    protected static void addIfExists( List list, DBObject source, String field )
    {
        if ( source!=null )
        {
            if ( source.containsField( field ) )
            {
                Object found = source.get( field );
                if ( found instanceof List ) 
                {
                    list.addAll( (List) found );
                }
                else if ( found instanceof DBObject )
                {
                    list.add( found );
                }
            }
        }
    }
    
    @GET
    @Path( "{profileId}/feed/home" )
    @Produces( MediaType.APPLICATION_JSON )
    public HomeViewFeed getHomeViewEntries( @PathParam( "profileId" ) String profileId )
    {
        List<HomeViewEntry> list = new LinkedList<HomeViewEntry>();
        DB db = null;
        
        try
        {
            DBObject query = getPositiveProfileQuery( profileId );
            
            db = getMongoClient( application );
            db.requestStart();
            db.requestEnsureConnection();
            
            DBCollection coll = db.getCollection( COLL_ACTIVITY );
            DBCursor found = (DBCursor) coll.find( query );
            List<DBObject> activity = found.sort( new BasicDBObject( "created", new Integer( -1 ) ) ).toArray( 64 );
            
            query = new BasicDBObject( ATTR_PROFILE, profileId );

            coll = db.getCollection( COLL_INSPIRATION );
            DBObject inspirationDoc = coll.findOne( query );
            List<DBObject> inspiration = new LinkedList<DBObject>();
            
            addIfExists( inspiration, inspirationDoc, ATTR_PHOTOS );
            addIfExists( inspiration, inspirationDoc, ATTR_QUOTES );
            addIfExists( inspiration, inspirationDoc, ATTR_ARTICLES );

//            coll = db.getCollection( COLL_PLAN );
//            DBObject planDoc = coll.findOne( query );
//            
//            addIfExists( inspiration, planDoc, ATTR_INSPIRATION );
            
            int actFactor = activity.size();
            
            if ( inspiration.size()>0 )
            {
                actFactor = actFactor / inspiration.size();
                
                if ( actFactor<1 )
                {
                    actFactor = 1;
                }
            }
            
            Iterator<DBObject> ait = activity.iterator();
            Iterator<DBObject> iit = inspiration.iterator();
            
            while ( ait.hasNext() || ( list.size()<64 && iit.hasNext() ) )
            {
                int i=0;
                for ( i=0; i<actFactor && ait.hasNext(); i++ )
                {
                    conformAndAddWithData( list, ait.next() );
                }
                
                if ( iit.hasNext() )
                {
                    conformAndAddWithData( list, iit.next() );
                }
            }
        }
        catch ( RsException wae )
        {
            wae.printStackTrace();
            throw wae;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            throw new RsException( Response.Status.INTERNAL_SERVER_ERROR, "Internal error", e );
        }
        finally
        {
            if ( db!=null )
            {
                db.requestDone();
            }
        }
        
        return new HomeViewFeed( (HomeViewEntry[]) list.toArray( new HomeViewEntry[ 0 ] ) );
    }
    
    protected static void conformAndAddWithData( List list, DBObject dbo )
    {
        HomeViewEntry hve = conformHomeViewObject( dbo );
        if ( hve.hasData() )
        {
            list.add( hve );
        }
    }
    
    protected DBObject getPositiveProfileQuery( String profileId )
    {
        BasicDBObject query = new BasicDBObject();
        query.append( "profile", profileId );
        
        BasicDBList details = new BasicDBList();
        query.append( "$and", details );
        
        BasicDBList content = new BasicDBList();
        content.add( new BasicDBObject( "content", new BasicDBObject( "$exists", Boolean.TRUE ) ) );
        content.add( new BasicDBObject( "media", new BasicDBObject( "$exists", Boolean.TRUE ) ) );
        details.add( new BasicDBObject( "$or", content ) );
        
        BasicDBList mood = new BasicDBList();
        mood.add( new BasicDBObject( "mood", new BasicDBObject( "$exists", Boolean.FALSE ) ) );
        mood.add( new BasicDBObject( "mood.level", new BasicDBObject( "$gt", new Integer( 3 ) ) ) );
        details.add( new BasicDBObject( "$or", mood ) );
        
        return query;
    }
    
    @GET
    @Path( "{profileId}/activity/positive" )
    @Produces( MediaType.APPLICATION_JSON )
    public String getProfileActivity( @PathParam( "profileId" ) String profileId )
    {
        String response = null;
        DB db = null;
        
        DBObject query = getPositiveProfileQuery( profileId );

        try
        {
            db = getMongoClient( application );
            db.requestStart();
            db.requestEnsureConnection();
            
            DBCollection activity = db.getCollection( COLL_ACTIVITY );
            DBCursor found = (DBCursor) activity.find( query );
            Iterator<DBObject> it = found.sort( new BasicDBObject( "created", new Integer( -1 ) ) ).limit( 64 );
            
            StringBuffer sb = new StringBuffer();
            sb.append( "[" );
            
            while ( it.hasNext() )
            {
                DBObject dbo = it.next();
                conformObjectStyle( application, dbo );
                sb.append( dbo.toString() );
                
                if ( it.hasNext() )
                {
                    sb.append( "," );
                }
            }
            
            sb.append( "]" );
            response = sb.toString();
        }
        catch ( RsException wae )
        {
            wae.printStackTrace();
            throw wae;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            throw new RsException( Response.Status.INTERNAL_SERVER_ERROR, "Internal error", e );
        }
        finally
        {
            if ( db!=null )
            {
                db.requestDone();
            }
        }
        
        return response;
    }
}
