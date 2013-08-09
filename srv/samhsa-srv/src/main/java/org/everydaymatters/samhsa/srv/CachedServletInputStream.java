/*
    MyLifeMatters
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
package org.everydaymatters.samhsa.srv;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.servlet.ServletInputStream;

public class CachedServletInputStream
    extends ServletInputStream
{
    ByteArrayInputStream bais;
    
    public CachedServletInputStream( byte[] cache )
    {
        bais = new ByteArrayInputStream( cache );
    }
     
    public CachedServletInputStream( String cache )
    {
        bais = new ByteArrayInputStream( cache.getBytes() );
    }
    
    public int read() 
    { 
        /*DEBUG*/System.err.println( "CSIS.r()" );
        return bais.read(); 
    }
    
    public int read( byte[] b, int off, int len ) 
    { 
        /*DEBUG*/System.err.println( "CSIS.r(bol) "+off+"/"+len );
        return bais.read( b, off, len ); 
    }
    
    public int readLine( byte[] b, int off, int len )
    {
        /*DEBUG*/System.err.println( "CSIS.rL(bol) "+off+"/"+len );
        // TODO: implement this method to conform with spec
        
        int mine = read( b, off, len );
        /*DEBUG*/System.err.println( "CSIS.rL() -- "+mine );
        
        return mine;
    }
    
    public int available() { return bais.available(); }
    public void close() throws IOException { bais.close(); }
    public void mark( int limit ) { bais.mark( limit ); }
    public boolean markSupported() { return bais.markSupported(); }
    public void reset() { bais.reset(); }
    public long skip( long n ) { return bais.skip( n ); }
}
