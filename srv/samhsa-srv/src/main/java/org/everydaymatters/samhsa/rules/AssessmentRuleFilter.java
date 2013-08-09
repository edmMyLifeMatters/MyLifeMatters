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
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.everydaymatters.samhsa.srv.EvaluatorResource;
import org.everydaymatters.samhsa.srv.PushNotifier;

public class AssessmentRuleFilter
    extends BaseRuleFilter
{
    @Override
    protected void evaluate( HttpServletRequest req, DB db, String profileId, DBObject profile, 
                             String body, JsonObject bdoc, Responder responder, PushNotifier notifier )
    {
        BasicDBObject query = new BasicDBObject();
        query.append( "profile", profileId );
        query.append( "assessment", "PHQ2" );
        
        DBCollection coll = db.getCollection( "response" );
        DBCursor results = coll.find( query ).sort( EvaluatorResource.SORT_LATEST_FIRST );
        
        if ( results.count()>0 )
        {
            DBObject first = results.next();
            DBObject prompts = (DBObject) first.get( "prompts" );
            
            if ( prompts instanceof List )
            {
                List plist = (List) prompts;
                int score = 0;
                
                Iterator pit = plist.iterator();
                while ( pit.hasNext() )
                {
                    DBObject dbo = (DBObject) pit.next();
                    Number value = (Number) dbo.get( "responses" );
                    score += value.intValue();
                }
                
                /*DEBUG*/application.log( "Found PHQ2 for "+profileId+" with score of "+score );
                if ( score>1 )
                {
                    /*DEBUG*/application.log( "Activating for PHQ2: "+first.get( "_id" ) );
                    query = new BasicDBObject( "profile", profileId );
                    
                    coll = db.getCollection( "team" );
                    DBObject plan = coll.findOne( query );
                    
                    if ( plan.containsField( "members" ) )
                    {
                        DBObject pma = (DBObject) plan.get( "members" );
                        
                        if ( pma instanceof List )
                        {
                            DBObject pname = (DBObject) profile.get( "name" );
                            String name = pname.get( "first" ).toString()+" "+pname.get( "last" ).toString();
                            String subject = name;
                            
                            StringBuffer sb = new StringBuffer();
                            sb.append( name );
                            sb.append( " is using the MyLifeMatters app, and has asked us to contact you as part of their clinical support team. " );
                            sb.append( "Based on recent assessment responses, you may want to contact "+name+" for follow-up." );
                            String msg = sb.toString();
                            
                            Iterator mit = ((List)pma).iterator();
                            while ( mit.hasNext() )
                            {
                                DBObject member = (DBObject) mit.next();
                                if ( member.containsField( "email" ) && member.containsField( "canContact" ) && member.containsField( "relationship" ) )
                                {
                                    /*DEBUG*/application.log( member.get( "email" )+" "+member.get( "relationship" )+" "+member.get( "canContact" ) );
                                    if ( member.get( "canContact" ).toString().equalsIgnoreCase( "yes" ) &&
                                         member.get( "relationship" ).toString().equalsIgnoreCase( "Clinical" ) )
                                    {
                                        String addr = member.get( "email" ).toString();
                                        application.log( "Sending message to: "+addr );
                                        responder.sendMessage( addr, subject, msg );
                                    }
                                    else
                                    {
                                        application.log( "No contact permitted for team member: "+member.get(  "email" ) );
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
        }
        else
        {
            /*DEBUG*/application.log( "No PHQ-2 assessment responses found for profile: "+profileId );
        }
    }
}
