/*
    MyLifeMatters
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
package org.everydaymatters.samhsa.rules;

import com.google.gson.JsonObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import javax.servlet.http.HttpServletRequest;
import org.everydaymatters.samhsa.srv.EvaluatorResource;
import org.everydaymatters.samhsa.srv.PushNotifier;

public class ActivityRuleFilter
    extends BaseRuleFilter
{
    @Override
    protected void evaluate( HttpServletRequest req, DB db, String profileId, DBObject profile, 
                             String body, JsonObject bdoc, Responder responder, PushNotifier notifier )
    {
        BasicDBObject query = new BasicDBObject();
        query.append( "profile", profileId );
        query.append( "mood", new BasicDBObject( "$exists", Boolean.TRUE ) );
        
        DBCollection coll = db.getCollection( "activity" );
        DBCursor results = coll.find( query ).sort( EvaluatorResource.SORT_LATEST_FIRST );
        
        if ( results.count()>2 )
        {
            DBObject first = results.next();
            DBObject mood = (DBObject) first.get( "mood" );
            Number level = (Number) mood.get( "level" );
            /*DEBUG*/application.log( "Found "+level+" on activity "+first.get(  "_id" ).toString()+" for profile "+profileId+"." );
            
            if ( level.intValue()<3 && results.hasNext() )
            {
                DBObject previous = results.next();
                mood = (DBObject) previous.get( "mood" );
                level = (Number) mood.get( "level" );
                /*DEBUG*/application.log( "Found "+level+" on activity "+previous.get(  "_id" ).toString()+" for profile "+profileId+"." );
                
                if ( level.intValue()<3 )
                {
                    // NOTE: two negative moods in a row; fire notification
                    /*DEBUG*/application.log( "Negative rule matched "+level+" on activity "+previous.get(  "_id" ).toString()+" for profile "+profileId+"." );
                    notifier.sendNotification( profileId, "Don't forget MyLifeMatters!", 
                                               "How are you doing? Please take a moment for a quick assessment.", 
                                               PushNotifier.ACT_ASSESS, 
                                               PushNotifier.REF_ASSESS_PHQ2 );
                }
            }
        }
    }
}
