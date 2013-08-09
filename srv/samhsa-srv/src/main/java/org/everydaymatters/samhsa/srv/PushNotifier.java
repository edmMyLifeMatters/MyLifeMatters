/*
    MyLifeMatters
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
package org.everydaymatters.samhsa.srv;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.bson.types.ObjectId;
import org.everydaymatters.venti.VentiServlet;

public class PushNotifier
{
    public static final String ACT_MOOD = "mood";
    public static final String ACT_ASSESS = "assess";
    public static final String ACT_COPING = "coping";
    public static final String ACT_CONNECT = "connect";
    public static final String ACT_URL = "url";
    
    public static final String REF_NONE = null;
    public static final String REF_ASSESS_PHQ2 = "phq2";
    public static final String REF_ASSESS_SLEEP = "sleep";
    
    public static final String COLL_PROFILES = "profile";
    public static final String COLL_MESSAGES = "messageBody";
    
    public static final String ATTR_ID = "_id";
    public static final String ATTR_DEVICES = "devices";
    public static final String ATTR_PUSH_ID = "pushId";
    
    protected DefaultHttpClient client;
    protected Gson gson;
    
    protected MongoClient mongo;
    protected String mongoDbName;
    
    protected JsonArray devTypes;
    
    
    public PushNotifier()
    {
        initPN();
    }
    
    public PushNotifier( MongoClient mongo, String mongoDbName, String appKey, String appSecret )
    {
        initPN();
        setMongo( mongo, mongoDbName );
        setUrbanAirshipConfig( appKey, appSecret );
    }
    
    private void initPN()
    {
        gson = new Gson();
        
        client = new DefaultHttpClient();
        mongo = null;
        mongoDbName = null;
        
        devTypes = new JsonArray();
        devTypes.add( new JsonPrimitive( "ios" ) );
    }
    
    public void setMongo( MongoClient mongo, String mongoDbName )
    {
        this.mongo = mongo;
        this.mongoDbName = mongoDbName;
    }
    
    public void setUrbanAirshipConfig( String appKey, String appSecret )
    {
        client.getCredentialsProvider()
              .setCredentials( 
                    new AuthScope( "go.urbanairship.com", 443 ), 
                    new UsernamePasswordCredentials( appKey, appSecret ) );
    }
    
    protected static void addDeviceId( DBObject ddbo, List<String> list )
    {
        if ( ddbo.containsField( ATTR_PUSH_ID ) )
        {
            list.add( ddbo.get( ATTR_PUSH_ID ).toString() );
        }
    }
    
    protected static void addIfNotNull( JsonObject map, String key, String value )
    {
        if ( value!=null )
        {
            map.addProperty( key, value );
        }
    }
    
    protected String storeMessageBody( String profileId, String messageBody )
    {
        String msgRef = null;
        
        if ( mongo!=null )
        {
            DB db = null;
            
            try
            {
                db = mongo.getDB( mongoDbName );
                db.requestStart();
                db.requestEnsureConnection();
                
                DBCollection messages = db.getCollection( COLL_MESSAGES );
                
                DBObject message = new BasicDBObject();
                message.put( "profile", profileId );
                message.put( "body", messageBody );
                message.put( VentiServlet.ATTR_CREATED, new Date() );
                messages.insert( message );
                
                msgRef = message.get( ATTR_ID ).toString();
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
            finally
            {
                if ( db!=null )
                {
                    db.requestDone();
                }
            }
        }
        
        return msgRef;
    }
    
    protected List<String> getDeviceIds( String profileId )
    {
        List<String> list = new LinkedList<String>();
        
        if ( mongo!=null )
        {
            DB db = null;
            
            try
            {
                db = mongo.getDB( mongoDbName );
                db.requestStart();
                db.requestEnsureConnection();
                
                DBCollection profiles = db.getCollection( COLL_PROFILES );
                DBObject profile = profiles.findOne( new BasicDBObject( ATTR_ID, new ObjectId( profileId ) ) );
                
                if ( profile!=null && profile.containsField( ATTR_DEVICES ) )
                {
                    DBObject devices = (DBObject) profile.get( ATTR_DEVICES );
                    if ( devices instanceof Iterable )
                    {
                        for ( Object device : (Iterable)devices )
                        {
                            if ( device instanceof DBObject )
                            {
                                addDeviceId( (DBObject)device, list );
                            }
                        }
                    }
                    else
                    {
                        addDeviceId( devices, list );
                    }
                }
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
            finally
            {
                if ( db!=null )
                {
                    db.requestDone();
                }
            }
        }
        
        return list;
    }
    
    protected String getUARequestJson( String profileId, String alert, String message, String action, String actionRef )
    {
        String req = null;
        List<String> deviceIds = getDeviceIds( profileId );
        
        if ( deviceIds.size()>0 )
        {
            JsonObject map = new JsonObject();
            map.add( "device_types", devTypes );
            
            if ( deviceIds.size()>1 )
            {
                JsonObject aud = new JsonObject();
                map.add( "audience", aud );

                JsonArray list = new JsonArray();
                aud.add( "OR", list );
                
                for ( String did : deviceIds )
                {
                    JsonObject dido = new JsonObject();
                    dido.addProperty( "device_token", did );
                    list.add( dido );
                }
            }
            else
            {
                JsonObject aud = new JsonObject();
                aud.addProperty( "device_token", deviceIds.get( 0 ) );
                map.add( "audience", aud );
            }
            
            JsonObject notice = new JsonObject();
            map.add( "notification", notice );
            
            JsonObject payload = new JsonObject();
            notice.add( "ios", payload );
            
            if ( alert!=null )
            {
                payload.addProperty( "alert", alert );
            }
            
            if ( message!=null || action!=null || actionRef!=null )
            {
                JsonObject extra = new JsonObject();
                payload.add( "extra", extra );
                
                addIfNotNull( extra, "msg", message );
                addIfNotNull( extra, "act", action );
                addIfNotNull( extra, "ref", actionRef );
            }
        
            req = gson.toJson( map );
        }
        
        return req;
    }
    
    public boolean sendNotification( String profileId, String alert, String message )
    {
        return this.sendNotification( profileId, alert, message, null, null );
    }
    
    public boolean sendNotification( String profileId, String alert, String message, String action, String actionRef )
    {
        boolean success = false;
        String messageRef = null;
        
        if ( message!=null && message.trim().length()>0 )
        {
            messageRef = storeMessageBody( profileId, message );
        }
        
        String payload = getUARequestJson( profileId, alert, messageRef, action, actionRef );
        
        if ( payload!=null )
        {
            try
            {
                HttpPost post = new HttpPost( "https://go.urbanairship.com/api/push/" );
                post.addHeader( "Content-Type", "application/json" );
                post.addHeader( "Accept", "application/vnd.urbanairship+json; version=3;" );
                post.setEntity( new StringEntity( payload ) );
                
                HttpResponse response = client.execute( post );
                StatusLine status = response.getStatusLine();
                /*DEBUG*/System.out.println( "Status: "+status.getStatusCode()+" "+status.getReasonPhrase() );
                
                success = ( 202 == status.getStatusCode() );
                
                if ( !success )
                {
                    HttpEntity he = response.getEntity();
                    if ( he!=null )
                    {
                        he.writeTo( System.out );
                    }
                }
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
        
        return success;
    }
}
