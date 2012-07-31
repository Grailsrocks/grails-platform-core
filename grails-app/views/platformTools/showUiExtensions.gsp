<html>
    <head>
        <title>UI Extensions</title>
        <meta name="layout" content="platform/dev"/>
    </head>
    <body>
        <h1>UI Extensions</h1>
        
        <h2>Display messages</h2>
        <p>The controller message was:</p>
        <p:tagDemo tag="p:displayMessage"/>

        <h2>Buttons</h2>
        <p:tagDemo tag="p:button">Click me I'm a button</p:tagDemo>
        <p:tagDemo tag="p:button" kind="anchor">Click me I'm a link</p:tagDemo>
        <p:tagDemo tag="p:button" kind="submit">Click me I'm a submit</p:tagDemo>

        <h2>Labels</h2>
        <g:form>
            <p:tagDemo tag="p:label" class="form-data" text="this.is.a.field"/>
        </g:form>

        <h2>Smart Links</h2>
        <p:tagDemo tag="p:smartLink" controller="platformTools"/>
        <p:tagDemo tag="p:smartLink" controller="platformTools" action="showNavigation"/>
        <p:tagDemo tag="p:smartLink" action="showPluginConfig"/>

        <h2>Branding</h2>
        <p:tagDemo tag="p:organization"/>
        <p:tagDemo tag="p:siteName"/>
        <p:tagDemo tag="p:siteURL"/>
        <p:tagDemo tag="p:siteLink"/>
        <p:tagDemo tag="p:year"/>

        <h2>Namespaced session, flash and request attributes</h2>
        <p>Session: ${pluginSessionInfo.encodeAsHTML()}</p>
        <p>Flash: ${pluginFlashInfo.encodeAsHTML()}</p>
        <p>Request: ${pluginRequestInfo.encodeAsHTML()}</p>

        <h2>Namespaced i18n messages</h2>
        <p>There is a <code>p:text</code> tag that is like <code>g:message</code> but uses plugin namespaced i18n codes
            and uses the body as default text. The messages are namespaced as <code>plugin.[GSP plugin Name].[code]</code>:
            <blockquote>
                <p:tagDemo tag="p:text" code="test.message">This will not show</p:tagDemo>
            </blockquote>
            <blockquote>
                <p:tagDemo tag="p:text" code="test.missing.message">This will show because the code used does not exist</p:tagDemo>
            </blockquote>
        </p>
        
    </body>
</html>