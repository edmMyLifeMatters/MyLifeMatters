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
public class HomeViewFeed
    implements Serializable
{
    HomeViewEntry[] homeViewEntry;
    
    public HomeViewFeed()
    {
        homeViewEntry = new HomeViewEntry[0];
    }
    
    public HomeViewFeed( HomeViewEntry[] hve )
    {
        homeViewEntry = hve;
    }

    public HomeViewEntry[] getHomeViewEntry()
    {
        return homeViewEntry;
    }

    public void setHomeViewEntry( HomeViewEntry[] homeViewEntry )
    {
        this.homeViewEntry = homeViewEntry;
    }
}
