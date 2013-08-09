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
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import org.bson.types.ObjectId;

public class PushNotifierTest
    extends MongoClientTest
{
    protected static boolean USE_AIRSHIP = false;
    
    protected static String PROFILE_ID = "";
    protected static String APP_KEY = "";
    protected static String APP_SECRET = "";
    
    protected PushNotifier notifier;
    
    public PushNotifierTest( String testName )
    {
        super( testName );
    }
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        notifier = new PushNotifier( mongo, mongoDbName, APP_KEY, APP_SECRET );
    }
    
    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }

//    public void testSetMongo()
//    {
//        System.out.println( "setMongo" );
//        MongoClient mongo = null;
//        String mongoDbName = "";
//        PushNotifier instance = new PushNotifier();
//        instance.setMongo( mongo, mongoDbName );
//        fail( "The test case is a prototype." );
//    }

    public void testGetDeviceIds()
    {
        if ( USE_MONGO )
        {
            System.out.println( "getDeviceIds" );

            List result = notifier.getDeviceIds( PROFILE_ID );
            /*DEBUG*/System.out.println( "Found device IDs for profile "+PROFILE_ID+": "+result.size() );
            assertTrue( "Expected at least one device id.", result.size()>0 );
        }
    }
    
    public void testGetUARequestJson()
    {
        if ( USE_MONGO )
        {
            System.out.println( "getUARequestJson" );
        
            String result = 
                notifier.getUARequestJson( PROFILE_ID, "testMessage", null, 
                                           "action", "actionRef" );
            /*DEBUG*/System.out.println( "Request JSON:\n"+result );
            // TODO: implement better verification
            assertTrue( "Expected result value.", ( result!=null && result.length()>0 ) );
        }
    }
    
    public void testStoreMessageBody_String_String()
    {
        if ( USE_MONGO )
        {
            System.out.println( "storeMessageBody" );
            String msgBody = "Now is the time for all good men to come to the aid of their country.";
            
            String msgRef = notifier.storeMessageBody( PROFILE_ID, msgBody );
            assertNotNull( "Expected not-null message ref.", msgRef );
            
            DB db = null;
            
            try
            {
                db = mongo.getDB( mongoDbName );
                db.requestStart();
                db.requestEnsureConnection();

                DBCollection messages = db.getCollection( PushNotifier.COLL_MESSAGES );
                DBObject persisted = 
                    messages.findOne( 
                        new BasicDBObject( PushNotifier.ATTR_ID, new ObjectId( msgRef ) ) );

                assertNotNull( "Expected presisted message.", persisted );
                assertEquals( "Expected matching message.", persisted.get( "body" ).toString(), msgBody );
            }
            finally
            {
                if ( db!=null )
                {
                    db.requestDone();
                }
            }
        }
    }

    public void testSendNotification_String_String()
    {
        if ( USE_MONGO && USE_AIRSHIP )
        {
            System.out.println( "sendNotification" );

            StringBuffer message = new StringBuffer();
            message.append( "testMessage 2args " );
            message.append( 
                DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT )
                          .format( new Date() ) );

            boolean success = 
                notifier.sendNotification( PROFILE_ID, message.toString(), null );

            assertTrue( "Expected successful response.", success );
        }
    }

//    public void testSendNotification_3args()
//    {
//        System.out.println( "sendNotification" );
//        String profileId = "";
//        String message = "";
//        String url = "";
//        PushNotifier instance = new PushNotifier();
//        instance.sendNotification( profileId, message, url );
//        fail( "The test case is a prototype." );
//    }
//
//    public void testSendNotification_4args()
//    {
//        System.out.println( "sendNotification" );
//        String profileId = "";
//        String message = "";
//        String action = "";
//        String actionRef = "";
//        PushNotifier instance = new PushNotifier();
//        instance.sendNotification( profileId, message, action, actionRef );
//        fail( "The test case is a prototype." );
//    }

    public void testSendNotification_5args()
    {
        if ( USE_MONGO && USE_AIRSHIP )
        {
            System.out.println( "sendNotification" );

            StringBuffer message = new StringBuffer();
            message.append( "testMessage 5args " );
            message.append( 
                DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT )
                          .format( new Date() ) );

            boolean success = 
                notifier.sendNotification( PROFILE_ID, message.toString(), "test message details", "action", "actionRef" );

            assertTrue( "Expected statusful response.", success );
        }
    }
}
