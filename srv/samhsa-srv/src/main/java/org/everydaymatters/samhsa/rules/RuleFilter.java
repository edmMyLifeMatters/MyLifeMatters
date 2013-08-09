/*
    MyLifeMatters
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
package org.everydaymatters.samhsa.rules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.jee.util.org.apache.commons.io.IOUtils;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.bson.types.ObjectId;
import org.everydaymatters.samhsa.msg.MatchedRule;
import org.everydaymatters.samhsa.srv.BaseMongoResource;
import org.everydaymatters.samhsa.srv.CachedRequestWrapper;
import org.everydaymatters.samhsa.srv.EvaluatorResource;
import static org.everydaymatters.samhsa.srv.EvaluatorResource.COLL_PROFILES;

public class RuleFilter
    implements Filter
{
    public static final String PARAM_RULESET = "ruleset-attr";
    public static final String PARAM_METHDS = "methods";
    
    protected ServletContext application;
    protected String filterName;
    protected Set<String> methods;
    protected String rulesetAttr;
    protected JsonParser parser;
        
    
    public void init( FilterConfig fc )
        throws ServletException
    {
        filterName = fc.getFilterName();
        application = fc.getServletContext();
        rulesetAttr = fc.getInitParameter( PARAM_RULESET );
        
        methods = new TreeSet<String>();
        
        String mcsv = fc.getInitParameter( PARAM_METHDS );
        if ( mcsv!=null )
        {
            String[] msa = mcsv.split( "," );
            for ( String mn : msa )
            {
                methods.add( mn.toUpperCase() );
            }
        }
        
        parser = new JsonParser();
    }

    public void doFilter( ServletRequest sr, ServletResponse sr1, FilterChain fc )
        throws IOException, ServletException
    {
        HttpServletRequest hreq = (HttpServletRequest) sr;
        /*DEBUG*/application.log( "Firing RuleFilter "+filterName+" for "+hreq.getRequestURI() );
        
        String body = IOUtils.toString( hreq.getReader() );
        CachedRequestWrapper crw = new CachedRequestWrapper( hreq, hreq.getContentType(), body );
        fc.doFilter( crw, sr1 );
        
        String method = hreq.getMethod();
        
        if ( methods.contains( method.toUpperCase() ) )
        {
            String profileId = null;
            JsonElement bdoc = parser.parse( body );
            if ( bdoc.isJsonObject() )
            {
                JsonObject bdo = (JsonObject) bdoc;
                if ( bdo.has( "profile" ) )
                {
                    profileId = bdo.getAsJsonPrimitive( "profile" ).getAsString();
                }
                else if ( hreq.getMethod().equalsIgnoreCase( "PUT" ) ) 
                {
                    // TODO: look for profile id in uri
                }
                else if ( hreq.getMethod().equalsIgnoreCase( "POST" ) )
                {
                    // TODO: look for profile id in body
                    profileId = bdo.getAsJsonPrimitive( "id" ).getAsString();
                }
            }

            if ( profileId!=null )
            {
                /*DEBUG*/application.log( "Found profileId: "+profileId );
                DB db = null;

                try
                {
                    db = BaseMongoResource.getMongoClient( application );
                    db.requestStart();
                    db.requestEnsureConnection();

                    DBCollection profiles = db.getCollection( COLL_PROFILES );
                    DBObject profile = profiles.findOne( new BasicDBObject( "_id", new ObjectId( profileId ) ) );

                    List<MatchedRule> matches = EvaluatorResource.processProfile( application, rulesetAttr, db, profile, profileId );
                    /*DEBUG*/for( MatchedRule mr : matches ) { application.log( "Matched "+mr.getName() ); }
                }
                catch ( Exception e )
                {

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
    }

    public void destroy()
    {
        // NOTE: nothing to do
    }
}
