/*
    Venti RESTful MongoDB 
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
package org.everydaymatters.venti;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class VentiVersionServlet
    extends HttpServlet
{
    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp )
        throws ServletException, IOException
    {
        resp.setContentType( "text/plain" );
        PrintWriter pw = resp.getWriter();
        pw.print( VentiServlet.VERSION );
        pw.close();
    }
}
