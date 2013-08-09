/*
    MyLifeMatters
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
package org.everydaymatters.samhsa.rules;

import org.drools.event.rule.ActivationCancelledEvent;
import org.drools.event.rule.ActivationCreatedEvent;
import org.drools.event.rule.AfterActivationFiredEvent;
import org.drools.event.rule.AgendaEventListener;
import org.drools.event.rule.AgendaGroupPoppedEvent;
import org.drools.event.rule.AgendaGroupPushedEvent;
import org.drools.event.rule.BeforeActivationFiredEvent;
import org.drools.event.rule.RuleFlowGroupActivatedEvent;
import org.drools.event.rule.RuleFlowGroupDeactivatedEvent;

public class DebugListener
    implements AgendaEventListener
{
    public void activationCreated( ActivationCreatedEvent ace )
    {
        System.out.println( "activationCreated() "+ace.getActivation().getRule().getName() );
    }

    public void activationCancelled( ActivationCancelledEvent ace )
    {
        System.out.println( "activationCancelled() "+ace.getActivation().getRule().getName() );
    }

    public void beforeActivationFired( BeforeActivationFiredEvent bafe )
    {
        System.out.println( "beforeActivationFired() "+bafe.getActivation().getRule().getName() );
    }

    public void afterActivationFired( AfterActivationFiredEvent aafe )
    {
        System.out.println( "afterActivationFired() "+aafe.getActivation().getRule().getName() );
    }

    public void agendaGroupPopped( AgendaGroupPoppedEvent agpe )
    {
        System.out.println( "agendaGroupPopped() "+agpe.getAgendaGroup().getName() );
    }

    public void agendaGroupPushed( AgendaGroupPushedEvent agpe )
    {
        System.out.println( "agendaGroupPushed() "+agpe.getAgendaGroup().getName() );
    }

    public void beforeRuleFlowGroupActivated( RuleFlowGroupActivatedEvent rfgae )
    {
        System.out.println( "beforeRuleFlowGroupActivated() "+rfgae.getRuleFlowGroup().getName() );
    }

    public void afterRuleFlowGroupActivated( RuleFlowGroupActivatedEvent rfgae )
    {
        System.out.println( "afterRuleFlowGroupActivated() "+rfgae.getRuleFlowGroup().getName() );
    }

    public void beforeRuleFlowGroupDeactivated( RuleFlowGroupDeactivatedEvent rfgde )
    {
        System.out.println( "beforeRuleFlowGroupDeactivated() "+rfgde.getRuleFlowGroup().getName() );
    }

    public void afterRuleFlowGroupDeactivated( RuleFlowGroupDeactivatedEvent rfgde )
    {
        System.out.println( "afterRuleFlowGroupDeactivated() "+rfgde.getRuleFlowGroup().getName() );
    }
}
