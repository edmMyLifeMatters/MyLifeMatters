/*
    MyLifeMatters
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
package org.everydaymatters.samhsa.msg;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MatchedRule
    implements Serializable
{
    String name;
    String details;
    
    public MatchedRule()
    {
        initMR();
    }
    
    public MatchedRule( String name )
    {
        initMR();
        this.name = name;
    }
    
    public MatchedRule( String name, String details )
    {
        initMR();
        this.name = name;
        this.details = details;
    }
    
    private void initMR()
    {
        name = details = null;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getDetails()
    {
        return details;
    }

    public void setDetails( String details )
    {
        this.details = details;
    }
}
