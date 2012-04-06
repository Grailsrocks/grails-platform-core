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
    </body>
</html>