<html>
    <head>
        <title>Events</title>
        <meta name="layout" content="platform/dev"/>
    </head>
    <body>
        <h1>Events</h1>
        
        <p>The following events are registered:</p>
        <ul>
        <g:each in="${events}" var="eventDef">
            <li>${eventDef.topic} in namespace ${eventDef.namespace} with attributes ${eventDef.othersAttributes}</li>
        </g:each>
        </ul>
    </body>
</html>