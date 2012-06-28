<html>
    <head>
        <title>Security</title>
        <meta name="layout" content="platform/dev"/>
    </head>
    <body>
        <h1>Security</h1>

        <g:if test="${grailsApplication.mainContext.grailsSecurityBridge}">
            <p>The security provider is: ${grailsApplication.mainContext.grailsSecurityBridge?.getClass()}.</p>
        </g:if>
        <g:else>
            <p>There's no security provider installed. You need to install a plugin that has one or declare a bean called grailsSecurityBridge that
                implements the <code>SecurityBridge</code> interface. See the documentation for details</p>
        </g:else>

        <p>The current user is: <s:identity/>.</p>

        <s:ifPermitted role="ADMIN">
            <p>The current user is an ADMIN</p>
        </s:ifPermitted>
        <s:ifNotPermitted role="ADMIN">
            <p>The current user is not an ADMIN</p>
        </s:ifNotPermitted>
        
        <p>The info from the security implementation is:
            <pre>
                <s:info/>
            </pre>
        </p>
        
        <g:if test="${grailsApplication.mainContext.grailsSecurityBridge}">
            <p>Log in button: <s:loginButton/>, log out button <s:logoutButton/> and sign up button <s:signupButton/>
            </p>
        </g:if>
        <g:else>
            <p>If you had a security provider installed, you would see the default log in/out/signup buttons here.</p>
        </g:else>
    </body>
</html>