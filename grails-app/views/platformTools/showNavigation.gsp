
<html>
    <head>
        <title>Navigation</title>
        <meta name="layout" content="platform/dev"/>
    </head>
    <body>
        <h1>Navigation</h1>
        
        <p>The current active path is "${nav.activePath().encodeAsHTML()}", which means the active node is ${nav.activeNode()?.id.encodeAsHTML()}</p>

        <p>Show default navigation for the active path:</p>
        <g:form action="showNavigation">
            <input name="activePath" value="${params.activePath}" size="80"/>
            <input type="submit"/>
        </g:form>

        <p>First active node for this path is: ${nav.firstActiveNode(path:params.activePath)?.id}</p>
        <p>Primary navigation for this path:</p>
        <nav:primary path="${params.activePath}"/>

        <p>Secondary navigation for this path:</p>
        <nav:secondary path="${params.activePath}"/>

        <p>The available navigation nodes are:
            <ul>
            <g:each in="${navScopes}" var="scope"> 
                <li>${scope.name.encodeAsHTML()}
                <nav:items scope="${scope}" var="item">
                    id: ${item.id.encodeAsHTML()}
                        <g:if test="${item.id == params.activePath}"><strong>ACTIVE</strong></g:if>
                        <br/>
                        name: ${item.name.encodeAsHTML()}<br/>
                        title: ${item.titleMessageCode.encodeAsHTML()} (<g:message code="${item.titleMessageCode}" encodeAs="HTML"/>)<br/>
                        default title: ${item.titleDefault.encodeAsHTML()}<br/>
                        visible: ${item.visibleClosure ? 'from Closure' : item.visible}<br/>
                        enabled: ${item.enabledClosure ? 'from Closure' : item.enabled}<br/>
                        link: ${item.linkArgs.encodeAsHTML()}<br/>
                </nav:items>
                </li>
            </g:each>
            </ul>
        </p>
        
    </body>
</html>