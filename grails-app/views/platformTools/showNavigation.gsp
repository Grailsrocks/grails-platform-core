
<html>
    <head>
        <title>Navigation</title>
        <meta name="layout" content="platform/dev"/>
    </head>
    <body>
        <h1>Navigation</h1>
        
        <p>The current active path is "${nav.activePath().encodeAsHTML()}", which means the active node is ${nav.activeNode()?.id.encodeAsHTML()}</p>

        <p>Set the current active path:</p>
        <g:form action="showNavigation">
            <input name="activePath" value="${params.activePath}"/>
            <input type="submit"/>
        </g:form>

        <p>Primary navigation for this path:</p>
        <nav:menu/>

        <p>The available navigation nodes are:
            <ul>
            <g:each in="${navScopes}" var="scope"> 
                <li>${scope.id.encodeAsHTML()}</li>
                <ul>
                    <g:each in="${scope.children}" var="item">
                        <li>id: ${item.id.encodeAsHTML()}<br/>
                            activation path: ${item.activationPath.encodeAsHTML()}<br/>
                            title message: ${item.titleMessageCode.encodeAsHTML()} (<g:message code="${item.titleMessageCode}" encodeAs="HTML"/>)<br/>
                            visible: ${item.visibleClosure ? 'from Closure' : item.visible}<br/>
                            enabled: ${item.enabledClosure ? 'from Closure' : item.enabled}<br/>
                            link: ${item.linkArgs.encodeAsHTML()}</li>
                    </g:each>
                </ul>
            </g:each>
            </ul>
        </p>
        
    </body>
</html>