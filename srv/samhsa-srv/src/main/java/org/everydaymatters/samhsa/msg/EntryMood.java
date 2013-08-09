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
public class EntryMood
    implements Serializable
{
    String level;
    String description;
    
    public EntryMood()
    {
        level = null;
        description = null;
    }
    
    public EntryMood( String level, String description )
    {
        this.level = level;
        this.description = description;
    }

    public EntryMood( int level, String description )
    {
        this.level = ""+level;
        this.description = description;
    }

    public String getLevel()
    {
        return level;
    }

    public void setLevel( String level )
    {
        this.level = level;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }
}
