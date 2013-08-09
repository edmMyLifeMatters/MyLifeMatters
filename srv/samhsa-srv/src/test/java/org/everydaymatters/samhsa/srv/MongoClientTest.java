/*
    MyLifeMatters
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
package org.everydaymatters.samhsa.srv;

import com.mongodb.MongoClient;
import junit.framework.TestCase;

abstract public class MongoClientTest
    extends TestCase
{
    protected static boolean USE_MONGO = false;
    protected static String MONGO_HOST = "localhost";
    protected static int MONGO_PORT = 27017;
    protected static String MONGO_DB_NAME = "samhsa";
    
    protected MongoClient mongo;
    
    protected String mongoHost;
    protected int mongoPort;
    protected String mongoDbName;

    public MongoClientTest()
    {
        super();
        initMCT();
    }

    public MongoClientTest( String name )
    {
        super( name );
        initMCT();
    }

    private void initMCT()
    {
        mongo = null;
        
        mongoHost = MONGO_HOST;
        mongoPort = MONGO_PORT;
        mongoDbName = MONGO_DB_NAME;
    }
    

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        if ( USE_MONGO )
        {
            mongo = new MongoClient( mongoHost, mongoPort );
        }
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        if ( mongo!=null )
        {
            mongo.close();
        }
        
        super.tearDown();
    }
}
