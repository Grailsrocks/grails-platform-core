<html>
    <head>
        <title>UI Extensions</title>
        <meta name="layout" content="platform/dev"/>
    </head>
    <body>
        <div class="row>">
            <div class="span12">
                <h1>UI Extensions</h1>
                
                <h2>Display messages</h2>
                <p>The controller message was:</p>
                <p:tagDemo tag="p:displayMessage"/>

                <h2>Buttons</h2>
                <p:tagDemo tag="p:button">Click me I'm a button</p:tagDemo>
                <p:tagDemo tag="p:button" kind="anchor">Click me I'm a link</p:tagDemo>
                <p:tagDemo tag="p:button" kind="submit" action="update">Click me I'm a submit</p:tagDemo>
                <p:tagDemo tag="p:button" kind="button" text="text.from.i18n" textScope="my.i18n.scope">Click me I'm a button</p:tagDemo>
                <p:tagDemo tag="p:button" kind="submit" action="update" text="text.from.i18n" textPlugin="emailConfirmation">Click me I'm a submit</p:tagDemo>
                <p:tagDemo tag="p:button" kind="button" text="text.from.i18n" textScope="my.i18n.scope"/>
                <p:tagDemo tag="p:button" kind="submit" action="update" text="text.from.i18n" textPlugin="emailConfirmation"/>

                <h2>Labels</h2>
                <g:form>
                    <p:tagDemo tag="p:label" class="form-data" for="field0" text="this.is.field.zero">With default text</p:tagDemo>
                    <p:tagDemo tag="p:label" class="form-data" for="field1" text="this.is.a.field"/>
                    <p:tagDemo tag="p:label" class="form-data" for="field2" text="this.is.another.field" textScope="my.i18n.scope"/>
                    <p:tagDemo tag="p:label" class="form-data" for="field3" text="this.is.yet.another.field" textPlugin="emailConfirmation"/>
                </g:form>

                <h2>Smart Links</h2>
                <p:tagDemo tag="p:smartLink" controller="platformTools"/>
                <p:tagDemo tag="p:smartLink" controller="platformTools">With default link text</p:tagDemo>
                <p:tagDemo tag="p:smartLink" controller="platformTools" action="showNavigation"/>
                <p:tagDemo tag="p:smartLink" action="showPluginConfig"/>
                <p:tagDemo tag="p:smartLink" action="showPluginConfig" textScope="my.i18n.scope"/>
                <p:tagDemo tag="p:smartLink" action="showPluginConfig" textPlugin="emailConfirmation"/>

                <h2>Branding</h2>
                <p:tagDemo tag="p:organization"/>
                <p:tagDemo tag="p:siteName"/>
                <p:tagDemo tag="p:siteURL"/>
                <p:tagDemo tag="p:siteLink"/>
                <p:tagDemo tag="p:year"/>

                <h2>Namespaced session, flash and request attributes</h2>
                <p>Session: <p:prettyPrint value="${pluginSessionInfo}"/></p>
                <p>Flash: <p:prettyPrint value="${pluginFlashInfo}"/></p>
                <p>Request: <p:prettyPrint value="${pluginRequestInfo}"/></p>

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
                <p>Also supports multiple codes, resolving until it finds a match
                    <blockquote>
                        <p:tagDemo tag="p:text" codes="['nonsense.message', 'test.message']">This will not show</p:tagDemo>
                    </blockquote>
                </p>
            </div>
        </div>
    </body>
</html>