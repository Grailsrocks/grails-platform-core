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
                <a class="brand" href="#">
                  Grails Platform Core
                </a>
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