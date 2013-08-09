/*
    MyLifeMatters
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
/*
    MyLifeMatters
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
package org.everydaymatters.samhsa.srv;

import com.mockrunner.mock.web.MockServletContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletContextEvent;
import org.everydaymatters.venti.VentiServlet;
import org.mockejb.jndi.MockContextFactory;

abstract public class MongoResourceTest
    extends MongoClientTest
{
    protected MockServletContext application;

    public MongoResourceTest()
    {
        super();
        initMRT();
    }

    public MongoResourceTest( String name )
    {
        super( name );
        initMRT();
    }

    private void initMRT()
    {
    }
    

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        application = new MockServletContext();
        application.setInitParameter( VentiServlet.PARAM_MONGO_JNDI, VentiServlet.DEFAULT_MONGO_JNDI );
        application.setInitParameter( VentiServlet.PARAM_ID_STYLE, VentiServlet.DEFAULT_ID_STYLE );
        application.setInitParameter( VentiServlet.PARAM_MONGO_DBNAME, mongoDbName );       
        
        MockContextFactory.setAsInitial();
        Context initCtx = new InitialContext();
        initCtx.rebind( "java:comp/env/"+VentiServlet.DEFAULT_MONGO_JNDI, mongo );
        
        ApplicationContext acl = new ApplicationContext();
        acl.contextInitialized( new ServletContextEvent( application ) );
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        MockContextFactory.revertSetAsInitial();
        
        super.tearDown();
    }
}
