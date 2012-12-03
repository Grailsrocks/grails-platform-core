<!DOCTYPE html>
<html lang="en">
<head>
    <g:layoutHead/>
    <plugin:isAvailable name="resources">
        <r:require module="plugin.platformCore.tools"/>
        <r:layoutResources/>
    </plugin:isAvailable>
</head>
<body>
    <div class="navbar navbar-fixed-top">
        <div class="navbar-inner">
            <div class="container">
                <g:link class="brand" controller="platformTools">Grails Platform Core</g:link>
                <nav:primary class="nav" scope="dev"/>
            </div>
        </div>
    </div>

    <div class="container">
        <g:layoutBody/>
    </div>

    <plugin:isAvailable name="resources">
        <r:layoutResources/>
    </plugin:isAvailable>
</body>
</html>