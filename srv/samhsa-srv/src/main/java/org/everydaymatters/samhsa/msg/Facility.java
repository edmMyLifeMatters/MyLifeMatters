/*
    MyLifeMatters
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
package org.everydaymatters.samhsa.msg;

import java.io.Serializable;
import java.util.LinkedList;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Facility
    implements Serializable
{
    String name;
    String phone;
    LinkedList<String> address;
    String url;
    
    public Facility()
    {
        initF();
    }
    
    private void initF()
    {
        name = phone = url = null;
        address = new LinkedList<String>();
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone( String phone )
    {
        this.phone = phone;
    }

    public LinkedList<String> getAddress()
    {
        return address;
    }

    public void setAddress(
                            LinkedList<String> address )
    {
        this.address = address;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl( String url )
    {
        this.url = url;
    }
}

