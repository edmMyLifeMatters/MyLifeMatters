/*
    MyLifeMatters
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
package org.everydaymatters.samhsa.srv;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import org.bson.types.ObjectId;
import org.drools.runtime.StatelessKnowledgeSession;
import org.everydaymatters.samhsa.msg.MatchedRule;
import org.everydaymatters.samhsa.rules.RuleActivationListener;
import static org.everydaymatters.samhsa.srv.BaseMongoResource.getMongoClient;

@Path( "/eval" )
public class EvaluatorResource
    extends BaseMongoResource
{
    public static final String VERSION = "1.0.2";
    
    public static final String COLL_PROFILES = "profile";
    public static final String COLL_ACTIVITY = "activity";
    public static final String COLL_RESPONSES = "response";
    
    public static final DBObject SORT_LATEST_FIRST = new BasicDBObject( "created", -1 );
    public static final DBObject SORT_OLDEST_FIRST = new BasicDBObject( "created", 1 );
    
    
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
    
    protected static List<Map> retrieveProfileDocuments( DB db, String profileId, String docType, int limit, DBObject sort )
    {
        List<Map> list = new LinkedList<Map>();
        
        DBCollection collection = db.getCollection( docType );
        DBCursor cursor = collection.find( new BasicDBObject( "profile", profileId ) );
        
        if ( sort!=null )
        {
            cursor = cursor.sort( sort );
        }
        
        for ( DBObject rec : cursor.limit( limit ) )
        {
            rec.put( "docType", docType );
            list.add( rec.toMap() );
        }
        
        /*DEBUG*/System.out.println( "Retreived "+list.size()+" "+docType+" documents for profile: "+profileId );
        return list;
    }
    
    protected List<MatchedRule> processProfile( DB db, DBObject profile, String profileId )
        throws RsException
    {
        return processProfile( application, "default", db, profile, profileId );
    }
    
    public static List<MatchedRule> processProfile( ServletContext application, String ruleSet, DB db, DBObject profile, String profileId )
        throws RsException
    {
        List<MatchedRule> responses = new LinkedList<MatchedRule>();
        String pid = profileId;
        
        if ( profile!=null )
        {
            profile.put( "docType", COLL_PROFILES );
            
            if ( pid==null )
            {
                pid = profile.get( "_id" ).toString();
            }
            
            List<Map> collection = new LinkedList<Map>();

            collection.add( profile.toMap() );

            collection.addAll( 
                retrieveProfileDocuments( db, pid, COLL_ACTIVITY, 24, SORT_OLDEST_FIRST ) );

            collection.addAll( 
                retrieveProfileDocuments( db, pid, COLL_RESPONSES, 24, SORT_OLDEST_FIRST ) );


            RuleActivationListener listener = new RuleActivationListener();
            StatelessKnowledgeSession ksess = ApplicationContext.getKnowledgeSession( application, ruleSet );
            ksess.addEventListener( listener );
            ksess.execute( collection );

            for ( String name : listener.getFired() )
            {
                responses.add( new MatchedRule( name ) );
            }                
        }
        else
        {
            throw new RsException( Response.Status.NOT_FOUND, "Profile not found" );
        }
            
        return responses;
    }

    @GET
    @Path( "profiles" )
    @Produces( MediaType.APPLICATION_JSON )
    public MatchedRule[] evaluateAllProfiles()
    {
        List<MatchedRule> responses = new LinkedList<MatchedRule>();
        DB db = null;
        
        try
        {
            db = getMongoClient( application );
            db.requestStart();
            db.requestEnsureConnection();
            
            DBCollection profiles = db.getCollection( COLL_PROFILES );
            
            for( DBObject dbo : profiles.find() )
            {
                /*DEBUG*/System.out.println( "Processing "+dbo.get( "_id" ).toString() );
                responses.addAll( processProfile( db, dbo, null  ) );
            }
        }
        catch ( RsException re ) 
        {
            re.printStackTrace();
            throw re;
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
        
        return (MatchedRule[]) responses.toArray( new MatchedRule[ 0 ] );        
    }
    
    @GET
    @Path( "profile/{profileId}" )
    @Produces( MediaType.APPLICATION_JSON )
    public MatchedRule[] evaluateProfile( @PathParam( "profileId" ) String profileId )
    {
        List<MatchedRule> responses = new LinkedList<MatchedRule>();
        DB db = null;
        
        try
        {
            db = getMongoClient( application );
            db.requestStart();
            db.requestEnsureConnection();
            
            DBCollection profiles = db.getCollection( COLL_PROFILES );
            DBObject profile = profiles.findOne( new BasicDBObject( "_id", new ObjectId( profileId ) ) );
            responses = processProfile( db, profile, profileId );
        }
        catch ( RsException re ) 
        {
            re.printStackTrace();
            throw re;
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
        
        return (MatchedRule[]) responses.toArray( new MatchedRule[ 0 ] );
    }
}
