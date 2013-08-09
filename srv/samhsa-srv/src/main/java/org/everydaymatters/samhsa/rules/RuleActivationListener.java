/*
    MyLifeMatters
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
package org.everydaymatters.samhsa.rules;

import java.util.LinkedList;
import java.util.List;
import org.drools.event.rule.AfterActivationFiredEvent;
import org.drools.event.rule.DefaultAgendaEventListener;

public class RuleActivationListener
    extends DefaultAgendaEventListener
{
    private List<String> fired;
    
    public RuleActivationListener()
    {
        fired = new LinkedList<String>();
    }
    
    @Override
    public void afterActivationFired( AfterActivationFiredEvent event )
    {
        /*DEBUG*/System.out.println( "afterActivationFired( "+event.getActivation().getRule().getName()+" )" );
        fired.add( event.getActivation().getRule().getName() );
    }
    
    public List<String> getFired()
    {
        return fired;
    }
}
