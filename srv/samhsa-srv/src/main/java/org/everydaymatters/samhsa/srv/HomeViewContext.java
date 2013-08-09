/*
    MyLifeMatters
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
package org.everydaymatters.samhsa.srv;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import org.everydaymatters.samhsa.msg.HomeViewEntry;
import org.everydaymatters.samhsa.msg.HomeViewFeed;

@Provider
public class HomeViewContext
    implements ContextResolver<JAXBContext>
{
    private JAXBContext context;
    private Class[] types =
    {
        HomeViewEntry.class,
        HomeViewFeed.class
    };

    public HomeViewContext()
        throws Exception
    {
        this.context = new JSONJAXBContext( JSONConfiguration.mapped().arrays( "homeViewEntry" ).build(),
                                            types );
    }

    public JAXBContext getContext( Class<?> objectType )
    {
        for ( Class type : types )
        {
            if ( type == objectType )
            {
                return context;
            }
        }

        return null;
    }
}
