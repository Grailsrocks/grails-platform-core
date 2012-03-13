<html>
    <head>
        <title>JS Model</title>
        <meta name="layout" content="platform/dev"/>
        <g:set var="output">
            <ui:jsModel model="book" i18n="test.ui.title,test.ui.other.title" params="*"/>
        </g:set>
        ${output}
    </head>
    <body>
        <h1>JS Model Test</h1>
        
        <p>This has been written to head:
        <pre>
            ${output.encodeAsHTML()}
        </pre>    
        </p>
    </body>
</html>