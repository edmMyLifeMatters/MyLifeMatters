/*
    MyLifeMatters
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
package org.everydaymatters.samhsa.web;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.everydaymatters.samhsa.msg.ErrorMessage;

public class JsonErrorServlet
    extends HttpServlet
{
    protected void processRequest( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        ErrorMessage em = new ErrorMessage();
        
        em.setStatus( ( (Integer) request.getAttribute( "javax.servlet.error.status_code" ) ).intValue() );
        em.setDescription( (String) request.getAttribute( "javax.servlet.error.message" ) );
        
        Throwable t = (Throwable) request.getAttribute( "javax.servlet.error.exception" );
        if ( t!=null )
        {
            try
            {
                StringWriter sw = new StringWriter();
                t.printStackTrace( new PrintWriter( sw ) );
                em.setTrace( sw.toString() );
                
                if ( em.getDescription()==null )
                {
                    em.setDescription( t.getMessage() );
                }
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
        
        response.setContentType( "application/json;charset=UTF-8" );
        PrintWriter out = response.getWriter();
        try
        {
            Gson gson = new Gson();
            out.println( gson.toJson( em ) );
        }
        finally
        {            
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        processRequest( request, response );
    }

    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        processRequest( request, response );
    }

    @Override
    protected void doPut( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        processRequest( request, response );
    }

    @Override
    protected void doDelete( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        processRequest( request, response );
    }// </editor-fold>
}
