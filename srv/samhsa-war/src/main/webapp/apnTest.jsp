<%@page contentType="text/html" 
        import="org.everydaymatters.samhsa.srv.*"%>
<!DOCTYPE html>
<html>
    <head>
        <!--

            MyLifeMatters
            Copyright (c) 2013 by Netsmart Technologies, Inc.

            This work is licensed under the Creative Commons Attribution 3.0 Unported License. 
            To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/.

          -->
        <meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
        <title>MLM Push Notification Test</title>
        <%
            String pnType = request.getParameter( "pnType" );
            String pnProfile = request.getParameter( "pnProfile" );
            
            if ( pnProfile!=null && pnType!=null )
            {
                PushNotifier notifier = ApplicationContext.getNotifier( application );
                
                if ( pnType.equals( PushNotifier.ACT_ASSESS ) || pnType.equals( PushNotifier.ACT_CONNECT ) || pnType.equals( PushNotifier.ACT_URL ) || pnType.equals( PushNotifier.ACT_COPING ) )
                {
                    notifier.sendNotification(
                        request.getParameter( "pnProfile" ),
                        request.getParameter( "pnAlert" ),
                        request.getParameter( "pnMsg" ),
                        request.getParameter( "pnType" ),
                        request.getParameter( "pnRef" ) );
                }
                else if ( pnType.equals( PushNotifier.ACT_MOOD ) )
                {
                    notifier.sendNotification(
                        request.getParameter( "pnProfile" ),
                        request.getParameter( "pnAlert" ),
                        request.getParameter( "pnMsg" ),
                        request.getParameter( "pnType" ),
                        null );
                }
                else
                {
                    notifier.sendNotification(
                        request.getParameter( "pnProfile" ),
                        request.getParameter( "pnAlert" ),
                        request.getParameter( "pnMsg" ) );
                    
                }
            }
            
            if ( pnProfile==null )
            {
                pnProfile = "";
            }
        %>
    </head>
    <body>
        <h2>MLM Push Notification Test</h2>
        <form action="">
            <table>
                <tr>
                    <td>Notification profile:</td>
                    <td><input name="pnProfile" value="<%= pnProfile %>" /></td>
                </tr>
                <tr>
                    <td>Notification title:</td>
                    <td><input name="pnAlert"/></td>
                </tr>
                <tr>
                    <td>Notification type:</td>
                    <td>
                        <select name="pnType">
                            <option value="-msg-">-</option>
                            <option value="<%= PushNotifier.ACT_ASSESS %>">Assessment</option>
                            <option value="<%= PushNotifier.ACT_MOOD %>">Mood</option>
                            <option value="<%= PushNotifier.ACT_COPING %>">Coping</option>
                            <option value="<%= PushNotifier.ACT_CONNECT %>">Connect</option>
                            <option value="<%= PushNotifier.ACT_URL%>">URL</option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td>Notification ref:</td>
                    <td><input name="pnRef"/></td>
                </tr>
                <tr>
                    <td>Notification message:</td>
                    <td><textarea name="pnMsg" ></textarea></td>
                </tr>
            </table>
            <p>
                <button type="submit">Send</button>
            </p>            
        </form>

    </body>
</html>
