<html>
    <head>
        <title>Security</title>
    </head>
    <body>
        <h1>Security</h1>
        
        <p>The security provider is: ${grailsApplication.mainContext.grailsSecurityBridge?.getClass()}.</p>
        <p>The current user is [${identity?.encodeAsHTML()}].</p>
        <p>The info from the security implementation is:
            <pre>
                ${info?.encodeAsHTML()}
            </pre>
        </p>
    </body>
</html>