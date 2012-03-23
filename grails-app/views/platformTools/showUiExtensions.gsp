<html>
    <head>
        <title>UI Extensions</title>
        <meta name="layout" content="platform/dev"/>
    </head>
    <body>
        <h1>UI Extensions</h1>
        
        <h2>Display messages</h2>
        <p>The controller message was:</p>
        <p:tagDemo tag="g:displayMessage"/>

        <h2>Buttons</h2>
        <p:tagDemo tag="g:button">Click me I'm a button</p:tagDemo>
        <p:tagDemo tag="g:button" kind="anchor">Click me I'm a link</p:tagDemo>
        <p:tagDemo tag="g:button" kind="submit">Click me I'm a submit</p:tagDemo>

        <h2>Labels</h2>
        <g:form>
            <p:tagDemo tag="g:label" class="form-data" text="this.is.a.field"/>
        </g:form>

        <h2>Smart Links</h2>
        <p:tagDemo tag="g:smartLink" controller="platformTools"/>
        <p:tagDemo tag="g:smartLink" controller="platformTools" action="showNavigation"/>
        <p:tagDemo tag="g:smartLink" action="showPluginConfig"/>

        <h2>Branding</h2>
        <p:tagDemo tag="g:company"/>
        <p:tagDemo tag="g:siteName"/>
        <p:tagDemo tag="g:siteURL"/>
        <p:tagDemo tag="g:siteLink"/>
        <p:tagDemo tag="g:year"/>
    </body>
</html>