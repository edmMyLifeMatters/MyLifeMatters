/*
    MyLifeMatters
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
package org.everydaymatters.samhsa.srv;

import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import java.util.Date;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import org.bson.types.ObjectId;
import org.everydaymatters.venti.VentiServlet;

public class BaseMongoResource
{
    protected VentiServlet.IdStyle idStyle;

    public BaseMongoResource()
    {
        idStyle = null;
    }
    
    protected void prepMongoPrefs( ServletContext app )
    {
        if ( idStyle==null )
        {
            String pIdStyle = 
                ApplicationContext.getInitParamOrDefault( app, VentiServlet.PARAM_ID_STYLE, VentiServlet.DEFAULT_ID_STYLE );
            idStyle = VentiServlet.getParameterValueIdStyle( pIdStyle );
        }        
    }   
    
    public static DB getMongoClient( ServletContext app )
        throws NamingException
    {
        MongoClient mongo = null;
        DB db = null;
        
        String jndiUrl = 
            ApplicationContext.getInitParamOrDefault( app, VentiServlet.PARAM_MONGO_JNDI, VentiServlet.DEFAULT_MONGO_JNDI );

        javax.naming.Context initCtx = new InitialContext();
        javax.naming.Context envCtx = (javax.naming.Context) initCtx.lookup("java:comp/env");
        mongo = (MongoClient) envCtx.lookup( jndiUrl );

        if ( mongo!=null )
        {
            String mongoDbName =
                ApplicationContext.getInitParamOrDefault( app, VentiServlet.PARAM_MONGO_DBNAME, VentiServlet.DEFAULT_MONGO_DBNAME );
            db = mongo.getDB( mongoDbName );
        }
        
        return db;
    }    
    
    protected void conformObjectStyle( ServletContext app, DBObject dbo )
    {
        prepMongoPrefs( app );
        
        if ( idStyle!=VentiServlet.IdStyle.NATIVE )
        {
            String idAttr = VentiServlet.getIdStyleParameterValue( idStyle );
            
            if ( dbo.containsField( VentiServlet.ID_STYLE_P_NATIVE ) )
            {
                ObjectId oid = (ObjectId) dbo.get( VentiServlet.ID_STYLE_P_NATIVE );
                dbo.put( idAttr, oid.toString() );
                dbo.removeField( VentiServlet.ID_STYLE_P_NATIVE );
            }
            else if ( dbo.containsField( idAttr ) )
            {
                Object id = dbo.get( idAttr );
                ObjectId oid = new ObjectId( id.toString() );
                dbo.put( VentiServlet.ID_STYLE_P_NATIVE, oid );
                dbo.removeField( idAttr );
            }
        }        
        
        if ( dbo.containsField( VentiServlet.ATTR_CREATED ) )
        {
            Object cob = dbo.get( VentiServlet.ATTR_CREATED );
            if ( cob!=null && cob instanceof Date )
            {
                Date created = (Date) cob;
                dbo.put( VentiServlet.ATTR_CREATED, VentiServlet.TIME_DTF.format( created ) );
            }
        }
        
        if ( dbo.containsField( VentiServlet.ATTR_MODIFIED ) )
        {
            Object mob = dbo.get( VentiServlet.ATTR_MODIFIED );
            if ( mob!=null && mob instanceof Date )
            {
                Date modified = (Date) mob;
                dbo.put( VentiServlet.ATTR_MODIFIED, VentiServlet.TIME_DTF.format( modified ) );
            }
        }

        // TODO: conform any other date/time formats
    }

}
