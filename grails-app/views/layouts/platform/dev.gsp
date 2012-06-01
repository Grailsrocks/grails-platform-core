<html>
<head>
    <g:layoutHead/>
    <plugin:isAvailable name="resources">
        <r:require module="plugin.platformCore.tools"/>
        <r:layoutResources/>
    </plugin:isAvailable>
</head>
<body>
    <div id="nav">
        <nav:primary class="nav nav-primary" scope="dev"/>
        <hr/>
        <nav:secondary class="nav nav-secondary"/>
    </div>
    <g:layoutBody/>

    <plugin:isAvailable name="resources">
        <r:layoutResources/>
    </plugin:isAvailable>
</body>
</html>