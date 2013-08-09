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
import java.util.Date;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.everydaymatters.samhsa.msg.Credentials;
import org.everydaymatters.venti.VentiServlet;

@Path( "/user" )
public class UserResource
    extends BaseMongoResource
{
    public static final String VERSION = "1.0.1";
    
    public static final String PARAM_PWD_SECRET = "password-secret";
    
    protected static final String COLL_PROFILES = "profile";
    protected static final String COLL_USERS = "edmUser";
    
    
    @Context
    protected UriInfo uriInfo;
    
    @Context
    protected Request request;
    
    @Context
    protected HttpServletRequest hsRequest;
    
    @Context
    protected ServletContext application;
    
    
    protected BasicDBObject getUserQuery( String username )
    {
        return new BasicDBObject( "username", username );
    }
    
    protected String hashPassword( String password )
    {
        String result = password;
        String pwdSecret = ApplicationContext.getInitParamOrDefault( application, PARAM_PWD_SECRET, PARAM_PWD_SECRET );
        
        try
        {
            SecretKeySpec key = new SecretKeySpec( pwdSecret.getBytes( "UTF-8" ), "HmacSHA256" );
            Mac mac = Mac.getInstance( "HmacSHA256" );
            mac.init( key );
            byte[] bytes = mac.doFinal( password.getBytes( "ASCII" ) );

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

            result = sb.toString();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        
        return result;
    }
    
    protected boolean passwordMatch( Credentials cred, DBObject userObject )
    {
        String chash = hashPassword( cred.getPassword() );
        return chash.equals( userObject.get( "password" ) );
    }
    
    
    @GET
    @Path( "version" )
    @Produces( MediaType.TEXT_PLAIN )
    public String getVersion()
    {
        return VERSION;
    }
    
    @POST
    @Path( "create" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    public String registerUser( Credentials cred )
    {
        String profileId = null;
        DB db = null;
        
        try
        {
            db = getMongoClient( application );
            db.requestStart();
            db.requestEnsureConnection();
            
            DBCollection users = db.getCollection( COLL_USERS );
            DBObject unq = getUserQuery( cred.getUsername() );
            DBObject existing = users.findOne( unq );
            
            if ( existing!=null )
            {
                throw new RsException( Status.CONFLICT, "Username already exists." );
            }
            else
            {
                DBCollection profiles = db.getCollection( COLL_PROFILES );
                DBObject profile = new BasicDBObject( "username", cred.getUsername() );
                profile.put( VentiServlet.ATTR_CREATED, new Date() );
                profiles.insert( profile );
                profileId = profile.get( "_id" ).toString();
                
                unq.put( "password", hashPassword( cred.getPassword() ) );
                unq.put( "profile", profileId );
                unq.put( VentiServlet.ATTR_CREATED, new Date() );
                users.insert( unq );
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
        
        return profileId;
    }
    
    @POST
    @Path( "auth/{username}" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    public String authenticateUser( @PathParam( "username" ) String username, Credentials cred )
    {
        String profileId = null;
        DB db = null;
        
        String cUsername = cred.getUsername();
        if ( cUsername!=null && ! cUsername.equals( username ) )
        {
            throw new RsException( Status.BAD_REQUEST, "Usernames do not match." );
        }
        
        try
        {
            db = getMongoClient( application );
            db.requestStart();
            db.requestEnsureConnection();
            
            DBCollection users = db.getCollection( COLL_USERS );
            DBObject unq = getUserQuery( cred.getUsername() );
            DBObject existing = users.findOne( unq );
            
            if ( existing==null )
            {
                throw new RsException( Status.NOT_FOUND, "Existing user not found." );
            }
            else if ( passwordMatch( cred, existing ) )
            {
                profileId = (String) existing.get( "profile" );
            }
            else
            {
                throw new RsException( Status.FORBIDDEN, "Supplied credentials do not match." );
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
        
        return profileId;
    }
}
