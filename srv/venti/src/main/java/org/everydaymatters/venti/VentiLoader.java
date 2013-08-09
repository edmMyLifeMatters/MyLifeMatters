/*
    Venti RESTful MongoDB 
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
package org.everydaymatters.venti;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.jee.util.JSON;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class VentiLoader
{
    public static final String ATTR_COLLECTION = "collection";
    
    public static final String ATTR_REF = "ref";
    public static final String ATTR_DATA = "data";
    
    public static final String ATTR_INDICES = "indices";
    public static final String ATTR_KEYS = "keys";
    public static final String ATTR_OPTIONS = "options";
    
    protected Gson gson;
    protected JsonParser parser;
    protected DB db;
    
    public VentiLoader()
    {
        initVI();
    }
    
    public VentiLoader( DB db )
    {
        initVI();
        setDb( db );
    }
    
    private void initVI()
    {
        gson = new Gson();
        parser = new JsonParser();
        db = null;
    }
    
    protected JsonObject getJsonObject( String json )
    {
        JsonObject jo = null;
        
        JsonElement je = parser.parse( json );
        if ( je.isJsonObject() )
        {
            jo = (JsonObject) je;
        }
        
        return jo;
    }
    
    protected JsonObject getJsonObject( Reader json )
    {
        JsonObject jo = null;
        
        JsonElement je = parser.parse( json );
        if ( je.isJsonObject() )
        {
            jo = (JsonObject) je;
        }
        
        return jo;
    }
    
    public void ensureIndex( DBCollection collection, JsonObject index )
    {
        if ( index.has( ATTR_KEYS ) )
        {
            JsonObject jKeys = index.getAsJsonObject( ATTR_KEYS );
            DBObject bKeys = (DBObject) JSON.parse( gson.toJson( jKeys ) );
            
            if ( index.has( ATTR_OPTIONS ) )
            {
                JsonObject jOpts = index.getAsJsonObject( ATTR_OPTIONS );
                DBObject bOpts = (DBObject) JSON.parse( gson.toJson( jOpts ) );
                collection.ensureIndex( bKeys, bOpts );
            }
            else
            {
                collection.ensureIndex( bKeys );
            }
        }
    }
    
    public String ensureDocumentLoaded( DBCollection collection, JsonObject document, String refAttr )
    {
        String persistedId = null;
        
        if ( document.has( refAttr ) )
        {
            String refVal = document.getAsJsonPrimitive( refAttr ).getAsString();
            DBObject bDoc = (DBObject) JSON.parse( gson.toJson( document ) );
            
            // TODO: adapt to more closely follow the logic in VentiServlet.doPut()
            DBObject persisted = (DBObject)
                collection.findAndModify( 
                    new BasicDBObject( refAttr, refVal ),                
                    null, null, false, bDoc, true, true );
            
            persistedId = persisted.get( "_id" ).toString();
        }
        
        return persistedId;
    }
    
    public void ensureIndices( JsonObject index )
    {
        if ( index.has( ATTR_COLLECTION ) && index.has( ATTR_INDICES ) )
        {
            try
            {
                db.requestStart();
                db.requestEnsureConnection();
                
                String cname = index.getAsJsonPrimitive( ATTR_COLLECTION ).getAsString();
                DBCollection collection = db.getCollection( cname );

                JsonArray indices = index.getAsJsonArray( ATTR_INDICES );

                for ( JsonElement jeIndex : indices )
                {
                    if ( jeIndex.isJsonObject() )
                    {
                        ensureIndex( collection, jeIndex.getAsJsonObject() );
                    }
                }
            }
            finally
            {
                db.requestDone();
            }
        }
    }
    
    public String ensureDocumentLoaded( String collectionName, JsonObject jsDoc, String refAttrName )
    {
        String persisted = null;
        
        try
        {
            db.requestStart();
            db.requestEnsureConnection();

            DBCollection collection = db.getCollection( collectionName );

            if ( jsDoc.isJsonObject() )
            {
                persisted = ensureDocumentLoaded( collection, jsDoc, refAttrName );
            }
        }
        finally
        {
            db.requestDone();
        }
        
        return persisted;
    }
    
    public Collection<String> ensureDocumentsLoaded( JsonObject data )
    {
        List<String> persisted = new LinkedList<String>();
        
        if ( data.has( ATTR_COLLECTION ) && data.has( ATTR_REF ) && data.has( ATTR_DATA ) )
        {
            try
            {
                db.requestStart();
                db.requestEnsureConnection();
                
                String cname = data.getAsJsonPrimitive( ATTR_COLLECTION ).getAsString();
                DBCollection collection = db.getCollection( cname );

                String refAttr = data.getAsJsonPrimitive( ATTR_REF ).getAsString();
                JsonArray documents = data.getAsJsonArray( ATTR_DATA );

                for ( JsonElement jeDoc : documents )
                {
                    if ( jeDoc.isJsonObject() )
                    {
                        ensureDocumentLoaded( collection, jeDoc.getAsJsonObject(), refAttr );
                    }
                }
            }
            finally
            {
                db.requestDone();
            }
        }
        
        return persisted;
    }
    
    public void ensureIndices( String json )
    {
        ensureIndices( getJsonObject( json ) );
    }
    
    public void ensureIndices( Reader json )
    {
        ensureIndices( getJsonObject( json ) );
    }
    
    public void ensureIndices( InputStream json )
        throws IOException
    {
        ensureIndices( getJsonObject( new InputStreamReader( json ) ) );
    }
    
    public void ensureLoaded( String json )
    {
        ensureDocumentsLoaded( getJsonObject( json ) );
    }
    
    public void ensureLoaded( Reader json )
    {
        ensureDocumentsLoaded( getJsonObject( json ) );
    }
    
    public void ensureLoaded( InputStream json )
        throws IOException
    {
        ensureDocumentsLoaded( getJsonObject( new InputStreamReader( json ) ) );
    }

    public void setDb( DB db )
    {
        this.db = db;
    }
}
