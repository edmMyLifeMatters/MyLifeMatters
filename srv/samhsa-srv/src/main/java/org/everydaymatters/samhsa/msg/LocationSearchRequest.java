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
public class LocationSearchRequest
    implements Serializable
{
    Double lat;
    Double lng;
    Integer zip;
    String address;
    
    public LocationSearchRequest()
    {
        initLSR();
    }
    
    private void initLSR()
    {
        lat = lng = null;
        zip = null;
        address = null;
    }

    public Double getLat()
    {
        return lat;
    }

    public void setLat( Double lat )
    {
        this.lat = lat;
    }

    public Double getLng()
    {
        return lng;
    }

    public void setLng( Double lng )
    {
        this.lng = lng;
    }

    public Integer getZip()
    {
        return zip;
    }

    public void setZip( Integer zip )
    {
        this.zip = zip;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress( String address )
    {
        this.address = address;
    }
}
