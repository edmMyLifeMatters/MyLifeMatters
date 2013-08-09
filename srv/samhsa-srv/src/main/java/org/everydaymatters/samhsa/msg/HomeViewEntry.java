/*
    MyLifeMatters
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
package org.everydaymatters.samhsa.msg;

import java.io.Serializable;
import java.util.Date;
import javax.xml.bind.annotation.XmlRootElement;
import org.everydaymatters.venti.VentiServlet;

@XmlRootElement
public class HomeViewEntry
    implements Serializable
{
    String ref;
    String title;
    String text;
    String url;
    String added;
    EntryMood mood;
    
    public HomeViewEntry()
    {
        initHVE();
    }
    
    private void initHVE()
    {
        ref = title = text = url = added = null;
        mood = null;
    }
    
    protected static boolean isNotEmpty( String value )
    {
        boolean hasData = false;
        
        if ( value!=null )
        {
            hasData = ( value.trim().length()>0 );
        }
        
        return hasData;
    }
    
    public static boolean isNotEmptyMood( EntryMood m )
    {
        boolean hasData = false;
        
        if ( m!=null )
        {
            hasData = ( m.level!=null || ( m.description!=null && m.description.trim().length()>0 ) );
        }
        
        return hasData;
    }
    
    public boolean hasData()
    {
        return ( isNotEmpty( title ) || isNotEmpty( text ) || isNotEmpty( url ) || isNotEmptyMood( mood ) );
    }
    
    public void setAddedDate( Date adt )
    {
        if ( adt!=null )
        {
            setAdded( VentiServlet.TIME_DTF.format( adt ) );
        }
        else
        {
            setAdded( null );
        }
    }

    public String getRef()
    {
        return ref;
    }

    public void setRef( String ref )
    {
        this.ref = ref;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle( String title )
    {
        this.title = title;
    }

    public String getText()
    {
        return text;
    }

    public void setText( String text )
    {
        this.text = text;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl( String url )
    {
        this.url = url;
    }

    public String getAdded()
    {
        return added;
    }

    public void setAdded( String added )
    {
        this.added = added;
    }

    public EntryMood getMood()
    {
        return mood;
    }

    public void setMood( EntryMood mood )
    {
        this.mood = mood;
    }
}
