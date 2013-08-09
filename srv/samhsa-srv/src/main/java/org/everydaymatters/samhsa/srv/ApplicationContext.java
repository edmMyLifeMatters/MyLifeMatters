/*
    MyLifeMatters
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
package org.everydaymatters.samhsa.srv;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.MongoClient;
import com.mongodb.jee.util.org.apache.commons.io.IOUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.naming.InitialContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.conf.EventProcessingOption;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatelessKnowledgeSession;
import org.everydaymatters.samhsa.rules.DebugListener;
import org.everydaymatters.samhsa.rules.Responder;
import org.everydaymatters.venti.VentiLoader;
import org.everydaymatters.venti.VentiServlet;

public class ApplicationContext
    implements ServletContextListener
{
    public static final String ATTR_MONGO = "uplift.mongo";
    public static final String ATTR_MONGO_DB = "uplift.mongo.db";
    public static final String ATTR_RULES = "uplift.rulebase";
    public static final String ATTR_RESPONDER = "uplift.responder";
    
    public static final String PARAM_APP_KEY = "ua-key";
    public static final String PARAM_APP_SECRET = "ua-secret";
    
    public static final String DBM_COLL_PROFILE = "profile";
    public static final String DBM_COLL_PLAN = "plan";
    public static final String DBM_COLL_TEAM = "team";
    public static final String DBM_COLL_ACTIVITY = "activity";
    public static final String DBM_COLL_ASSESSMENT = "assessment";
    public static final String DBM_COLL_RESPONSE = "response";
    public static final String DBM_COLL_INSPIRATION = "inspiration";
    public static final String DBM_COLL_BLOB = "blob";
    public static final String DBM_COLL_MESSAGE = "messageBody";
    public static final String DBM_COLL_USER = "edmUser";
    
    
    public static final String GLOBAL_MATCHES = "matches";
    private static final String GLOBAL_RESPONDER = "responder";
    private static final String GLOBAL_NOW = "now";
    
    private static final String[] INDEX_FILES =
        {
            "indices/facility.json",
            "indices/profile.json",
            "indices/plan.json",
            "indices/team.json",
            "indices/activity.json",
            "indices/assessment.json",
            "indices/response.json",
            "indices/inspiration.json",
            "indices/messageBody.json",
            "indices/edmUser.json"
        };
    
    private static final String[] DATA_FILES = 
        {
            "data/assessment.json"
        };
    
    private static final String[] RULE_FILES = 
        { 
            "ProfileTest.rule", 
            "ActivityTest.rule", 
            "NextDay.rule", 
            "ProfileInactivity.rule",
//            "DroppingMood.rule"
//            "LowPhqScore.rule" 
        };
    
    private static final String[] ACTIVITY_RULE_FILES = 
        { 
            "ProfileTest.rule", 
            "ActivityTest.rule", 
            "DroppingMood.rule"
        };
    
    public static String getInitParamOrDefault( ServletContext application, String paramName, String defaultValue )
    {
        String value = null;
        
        if ( application!=null )
        {
            value = application.getInitParameter( paramName );
        }
        
        if ( value==null )
        {
            value = defaultValue;
        }
        
        return value;
    }    
    
    public static PushNotifier getNotifier( ServletContext application )
    {
        PushNotifier notifier = null;        
        
        String appKey = getInitParamOrDefault( application, PARAM_APP_KEY, null );
        String appSecret = getInitParamOrDefault( application, PARAM_APP_SECRET, null );
        /*DEBUG*/application.log( "Using UA credentials: "+appKey+" / "+appSecret );
        
        if ( appKey!=null && appSecret!=null )
        {
            MongoClient mongo = (MongoClient) application.getAttribute( ATTR_MONGO );
            String mongoDbName = (String) application.getAttribute( ATTR_MONGO_DB );
            
            notifier = new PushNotifier( mongo, mongoDbName, appKey, appSecret );
        }
        
        return notifier;
    }
    
    public static StatelessKnowledgeSession getKnowledgeSession( ServletContext application )
    {
        return getKnowledgeSession( application, ATTR_RULES );
    }
    
    public static StatelessKnowledgeSession getKnowledgeSession( ServletContext application, String ruleSet )
    {
        StatelessKnowledgeSession ksess = null;
        KnowledgeBase kbase = (KnowledgeBase) application.getAttribute( ruleSet );
        
        if ( kbase!=null )
        {
            ksess = kbase.newStatelessKnowledgeSession();
            /*DEBUG*/ksess.addEventListener( new DebugListener() );
            ksess.setGlobal( GLOBAL_RESPONDER, application.getAttribute( ATTR_RESPONDER ) );
            ksess.setGlobal( GLOBAL_NOW, new Date() );
        }
        
        return ksess;
    }
    
    
    protected void loadFacilityData( VentiLoader loader )
        throws IOException
    {
        /*DEBUG*/System.out.println( "loadFacilityData()" );
        BufferedReader br = null;
        JsonParser parser = new JsonParser();
        
        try
        {
            List<String> lines = IOUtils.readLines( getClass().getResourceAsStream( "data/facilities.json.txt" ) );
            /*DEBUG*/System.out.println( "loadFacilityData() read lines "+lines.size() );
            for ( String line : lines )
            {
                JsonObject jFac = parser.parse( line ).getAsJsonObject();
                jFac.addProperty( "ref", jFac.getAsJsonPrimitive( "SITE_ID" ).getAsString() );
                
                JsonObject loc = new JsonObject();
                loc.addProperty( "type", "Point" );
                
                JsonArray coords = new JsonArray();
                coords.add( jFac.get( "LOC_LONG" ) );
                coords.add( jFac.get( "LOC_LAT" ) );
                
                loc.add( "coordinates", coords );
                jFac.add( "loc", loc );
                
                String guid = loader.ensureDocumentLoaded( "facility", jFac, "ref" );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        finally
        {
            if ( br!=null )
            {
                br.close();
            }
        }
    }
    
    public void contextInitialized( ServletContextEvent sce )
    {
        ServletContext application = sce.getServletContext();
        
        Responder responder = new Responder( application );
        application.setAttribute( ATTR_RESPONDER, responder );
        
        try
        {
            String jndiUrl = getInitParamOrDefault( application, VentiServlet.PARAM_MONGO_JNDI, VentiServlet.DEFAULT_MONGO_JNDI );

            javax.naming.Context initCtx = new InitialContext();
            javax.naming.Context envCtx = (javax.naming.Context) initCtx.lookup("java:comp/env");
            MongoClient mongo = (MongoClient) envCtx.lookup( jndiUrl );
            application.setAttribute( ATTR_MONGO, mongo );            
            
            if ( mongo!=null )
            {
                String mongoDbName = getInitParamOrDefault( application, VentiServlet.PARAM_MONGO_DBNAME, VentiServlet.DEFAULT_MONGO_DBNAME );
                application.setAttribute( ATTR_MONGO_DB, mongoDbName );
                
                responder.setMongoConfig( mongo, mongoDbName );
                
                VentiLoader loader = new VentiLoader( mongo.getDB( mongoDbName ) );
                
                for ( String index : INDEX_FILES )
                {
                    application.log( "Processing MongoDB collection indices from: "+index );
                    loader.ensureIndices( getClass().getResourceAsStream( index ) );
                }
                
                for ( String data : DATA_FILES )
                {
                    application.log( "Processing MongoDB reference data from: "+data );
                    loader.ensureLoaded( getClass().getResourceAsStream( data ) );
                }               
                
                loadFacilityData( loader );
            }
        }
        catch ( Exception ne )
        {
            throw new RuntimeException( ne );
        }
        
        
        KnowledgeBase kbDefault = buildKnowledgeBase( application, responder, RULE_FILES );
        application.setAttribute( ATTR_RULES, kbDefault );
        application.setAttribute( "default", kbDefault );
        
        KnowledgeBase kbActivity = buildKnowledgeBase( application, responder, ACTIVITY_RULE_FILES );
        application.setAttribute( "activity", kbActivity );
    }
    
    protected KnowledgeBase buildKnowledgeBase( ServletContext application, Responder responder, String[] ruleFiles )
    {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        
        for ( String rule : ruleFiles )
        {
            kbuilder.add( 
                ResourceFactory.newClassPathResource( rule, responder.getClass() ),
                ResourceType.DRL );
        }

        if ( kbuilder.hasErrors() ) 
        {
            application.log( kbuilder.getErrors().toString() );
        }
        
        KnowledgeBaseConfiguration kbconfig = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
        kbconfig.setOption( EventProcessingOption.STREAM );

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase( kbconfig );
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );    
        
        return kbase;
    }

    public void contextDestroyed( ServletContextEvent sce )
    {
        ServletContext application = sce.getServletContext();
        
        MongoClient mongo = (MongoClient) application.getAttribute( ATTR_MONGO );
        application.removeAttribute( ATTR_MONGO );
        if ( mongo!=null )
        {
            mongo.close();
        }
        
        application.removeAttribute( ATTR_RULES );
        application.removeAttribute( ATTR_RESPONDER );
        
        // TODO: tear down any active resources
    }    
}
