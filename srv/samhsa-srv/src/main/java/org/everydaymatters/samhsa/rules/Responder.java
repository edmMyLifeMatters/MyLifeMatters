/*
    MyLifeMatters
    Copyright (c) 2013 by Netsmart Technologies, Inc.

    This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
    To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.
 */   
  
package org.everydaymatters.samhsa.rules;

import com.mongodb.MongoClient;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;
import org.everydaymatters.samhsa.srv.ApplicationContext;
import org.everydaymatters.samhsa.srv.PushNotifier;

public class Responder
{
    public static final String ATTR_SMTP_HOST = "smtp-host";
    public static final String ATTR_SMTP_PORT = "smtp-port";
    public static final String ATTR_SMTP_AUTH = "smtp-auth";
    public static final String ATTR_SMTP_TLS = "smtp-tls";
    public static final String ATTR_SMTP_USER = "smtp-user";
    public static final String ATTR_SMTP_PASSWD = "smtp-passwd";
    public static final String ATTR_MAIL_NAME = "mail-name";
    public static final String ATTR_MAIL_FROM = "mail-from";
    
    
    protected ServletContext application;
    
    protected MongoClient mongo;
    protected String dbName;
    
    protected String smtpHost;
    protected Integer smtpPort;
    protected boolean smtpAuth;
    protected boolean smtpTls;
    protected String smtpUser;
    protected String smtpPasswd;
    
    protected String mailName;
    protected String mailFrom;
    
    protected PushNotifier notifier;
    
    public Responder()
    {
        initResponder();
    }
    
    public Responder( ServletContext application )
    {
        initResponder();
        setApplication( application );
    }
    
    private void initResponder()
    {
        mongo = null;
        dbName = null;
        
        mailName = null;
    }
    
    public void setApplication( ServletContext application )
    {
        this.application = application;
        
        smtpHost = application.getInitParameter( ATTR_SMTP_HOST );
        smtpPort = new Integer( application.getInitParameter( ATTR_SMTP_PORT ) );
        smtpAuth = Boolean.parseBoolean( application.getInitParameter( ATTR_SMTP_AUTH ) );
        smtpTls = Boolean.parseBoolean( application.getInitParameter( ATTR_SMTP_TLS ) );
        smtpUser = application.getInitParameter( ATTR_SMTP_USER );
        smtpPasswd = application.getInitParameter( ATTR_SMTP_PASSWD );
        
        mailName = application.getInitParameter( ATTR_MAIL_NAME );
        mailFrom = application.getInitParameter( ATTR_MAIL_FROM );
        
        this.notifier = ApplicationContext.getNotifier( application );
    }
    
    public void setMongoConfig( MongoClient mongo, String dbName )
    {
        this.mongo = mongo;
        this.dbName = dbName;
    }
    
    public PushNotifier getNotifier()
    {
        return notifier;
    }
    
    public void sendMessage( String addrTo, String subject, String body )
    {
        /*DEBUG*/System.out.println( "sendMessage() "+addrTo );
        Properties props = new Properties();
        props.put( "mail.smtp.auth", ""+smtpAuth );
        props.put( "mail.smtp.starttls.enable", ""+smtpTls );
        props.put( "mail.smtp.host", smtpHost );
        props.put( "mail.smtp.port", smtpPort.toString() );

        Session session = 
            Session.getInstance( 
                props,
                new javax.mail.Authenticator()
                {
                    protected PasswordAuthentication getPasswordAuthentication()
                    {
                        return new PasswordAuthentication( smtpUser, smtpPasswd );
                    }
                } );

        try
        {
            /*DEBUG*/System.out.println( "Preparing message for "+addrTo );
            Message message = new MimeMessage( session );
            message.setFrom( new InternetAddress( mailFrom ) );
            message.setRecipients( Message.RecipientType.TO,
                                   InternetAddress.parse( addrTo ) );
            message.setSubject( subject );
            message.setText( body );

            /*DEBUG*/System.out.println( "Sending message to "+addrTo );
            Transport.send( message );
            /*DEBUG*/System.out.println( "Done" );
        }
        catch ( MessagingException e )
        {
            e.printStackTrace();
            throw new RuntimeException( e );
        }       
    }
    
    public void log( String message )
    {
        if ( application!=null )
        {
            application.log( message );
        }
        else
        {
            System.out.println( message );
        }
    }
}
