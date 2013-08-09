/*
    MyLifeMatters
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
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
import com.mongodb.DBObject;
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

@Path( "/message" )
public class MessageBodyResource
    extends BaseMongoResource
{
    public static final String VERSION = "1.0.3";
    
    protected static final String COLL_MESSAGES = "messageBody";
    
    
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
    
    @GET
    @Path( "{messageBodyRef}" )
    @Produces( MediaType.TEXT_PLAIN )
    public String getTextBody( @PathParam( "messageBodyRef" ) String messageBodyRef )
    {
        String body = null;
        DB db = null;
        
        try
        {
            db = getMongoClient( application );
            db.requestStart();
            db.requestEnsureConnection();
            
            DBCollection profiles = db.getCollection( COLL_MESSAGES );
            DBObject message = profiles.findOne( new BasicDBObject( "_id", new ObjectId( messageBodyRef ) ) );
            
            if ( message!=null && message.containsField( "body" ) )
            {
                body = message.get( "body" ).toString();
            }
            else
            {
                throw new RsException( Response.Status.NOT_FOUND, "Message not found" );
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
        
        return body;
    }    
}
