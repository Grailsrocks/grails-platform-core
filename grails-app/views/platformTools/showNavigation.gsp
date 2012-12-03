
<html>
    <head>
        <title>Navigation</title>
        <meta name="layout" content="platform/dev"/>
        <style>
        .usernav { float: right; }
        </style>
    </head>
    <body>
        <div class="row">
            <div class="span12">
                <div class="usernav">
                    <p>This is our user navigation</p>
                    <nav:menu scope="user"/>
                </div>

                <h1>Navigation</h1>
                
                <p>The current active path is "${nav.activePath().encodeAsHTML()}", which means the active node is ${nav.activeNode()?.id.encodeAsHTML()}</p>

                <h2>Test navigation for the active path</h2>
                <g:form mapping="platformNormal" action="showNavigation">
                    <input type="text" name="activePath" value="${params.activePath}" size="80"/>
                    <input type="submit"/>
                </g:form>

                <p>First active node for the path [${params.activePath}] is: ${nav.firstActiveNode(path:params.activePath)?.id}</p>
                <p>Primary navigation for this path:</p>
                <nav:primary scope="${nav.scopeForActivationPath(path:params.activePath)}" path="${params.activePath}"/>

                <p>Secondary navigation for this path:</p>
                <nav:secondary path="${params.activePath}"/>

                <p>Two-deep menu for this path:</p>
                <nav:menu scope="${params.activePath}" path="${params.activePath}" class="" depth="2"/>

                <p>Two-deep menu for this path with custom render:</p>
                <nav:menu scope="${params.activePath}" path="${params.activePath}" class="" custom="true" depth="2">
                    Item: <p:callTag tag="p:smartLink" attrs="${linkArgs}"/>
                </nav:menu>

                <h2>Available navigation scopes</h2>
                <ul>
                <g:each in="${navScopes}" var="scope"> 
                    <li>${scope.name.encodeAsHTML()}
                    <nav:items scope="${scope}" var="item">
                        id: ${item.id.encodeAsHTML()}
                            <g:if test="${item.id == params.activePath}"><strong>ACTIVE</strong></g:if>
                            <br/>
                            name: ${item.name.encodeAsHTML()}<br/>
                            order: ${item.order.encodeAsHTML()}<br/>
                            link args: ${item.linkArgs.encodeAsHTML()} (<p:callTag tag="g:link" attrs="${new HashMap(item.linkArgs)}">Test</p:callTag>)<br/>
                            action aliases: ${item.actionAliases?.encodeAsHTML()}<br/>
                            title: ${item.titleMessageCode.encodeAsHTML()} (<g:message code="${item.titleMessageCode}" encodeAs="HTML"/>)<br/>
                            default title: ${item.titleDefault.encodeAsHTML()}<br/>
                            data: ${item.data.encodeAsHTML()}<br/>
                            visible: ${item.visibleClosure ? 'from Closure' : item.visible}<br/>
                            enabled: ${item.enabledClosure ? 'from Closure' : item.enabled}<br/>
                    </nav:items>
                    </li>
                </g:each>
                </ul>
            
                <h2>Nodes by id cache</h2>
                <ul>
                <g:each in="${navNodesById}" var="n"> 
                    <li>${n.key.encodeAsHTML()} &raquo; ${n.value.name.encodeAsHTML()} <g:if test="${n.value instanceof org.grails.plugin.platform.navigation.NavigationItem}"> (${n.value.linkArgs.encodeAsHTML()})</g:if></li>
                </g:each>
                </ul>

                <h2>Nodes by controller/action</h2>
                <ul>
                <g:each in="${navNodesByControllerAction}" var="n"> 
                    <li>${n.key.encodeAsHTML()} &raquo; ${n.value.name.encodeAsHTML()} (${n.value.linkArgs.encodeAsHTML()})</li>
                </g:each>
                </ul>

                <footer>
                    <p>This is our footer navigation</p>
                    <nav:menu scope="footer"/>
                </footer>
            </div>
        </div>
    </body>
</html>