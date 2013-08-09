/*
    MyLifeMatters
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
package org.everydaymatters.samhsa.srv;

import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import org.everydaymatters.samhsa.msg.Facility;
import org.everydaymatters.samhsa.msg.LocationSearchRequest;

@Path( "/facilities" )
public class FacilityResource
{
    public static final String VERSION = "1.0";
    
    
    @Context
    protected UriInfo uriInfo;
    
    @Context
    protected Request request;
    
    @Context
    protected HttpServletRequest hsRequest;
    
    @Context
    protected ServletContext application;
    
    
    @GET
    @Path( "version" )
    @Produces( MediaType.TEXT_PLAIN )
    public String getVersion()
    {
        return VERSION;
    }
    
    @POST
    @Path( "search" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    public Facility[] searchFacilities( LocationSearchRequest lsr )
    {
        List<Facility> list = new LinkedList<Facility>();
        
        // TODO: search national suicide hotline
        
        // TODO: search samhsa database
        
        return (Facility[]) list.toArray( new Facility[0] );
    }
    
}
