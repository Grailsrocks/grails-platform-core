# Grails Plugin Platform

This plugin provides a suite of facilities to support the plugin ecosystem by
providing the mechanisms for plugins to play nicely with each other and the
developer's app.

This is intended to stay out of the Grails versioning cycle as much as
possible, to provide support for these features across Grails releases as much
as possible

## Features

### Config declaration

Plugins can declare the configuration options they support, default values, and validators.

These configuration settings are automatically namespaced by the plugin name to plugin.<pluginName>.x.y

    def doWithConfigOptions = { 
        // Optional prefix for purely legacy plugins, not recommended for use.
        // Defaults to plugin.<pluginName> if not specified
        // Do NOT set this to support legacy config. legacyPrefix will be used for that
        prefix = 'grails'
    
        // Declare a config setting. defaultValue optional. Validator closure optional, passed the value.
        'views.default.codec'( defaultValue:'none', validator: { 
            // Return null for success, string for error message.
            v -> v instanceof String ? null : 'Codec name required or "none"' 
        })
    }
    
### Config merging

Plugins can declare config that they want to merge into the main application config.

This allows plugins to configure other plugins, or to provide values for global Grails config settings.

    def doWithConfig = { config ->
        // Global configuration is under "application"
        application {
             grails.views.default.codec = "none"
        }

        // Per-plugin configuration is under blocks with the bean-name of the plugin
        // This is automatically namespaced as per doWithConfigOptions declarations
        myFancyPlugin {
             something.enabled = true
        }
    }

Configuration is automatically merged with the main application Config, with the following precedence:

1. Application Config.groovy supplied settings always win
2. If no setting supplied in Config.groovy, value from doWithConfig is merged in
3. If no setting supplied for a declared (doWithConfigOptions) setting, the defaultValue if any is applied

After all this, the validators supplied by the doWithConfigOptions of plugins
are applied, and error messages generated if necessary.

### System tags

These are some general purpose tags are provided to enable plugins and applications to respond to their environment.

Plugins, including theme plugins, can use these to customize their behaviour.
For example your plugin might have custom JavaScript UI code. Using these tags
you can provide multiple implementations of that code that will work with
jQuery or YUI depending on which JS plugin the user has installed.

#### g:config

This returns values from the application's configuration (defined via Config.groovy and merged configuration).

Attributes

* name - The full config property path of the value you require. For example "myapp.feature.x.enabled"

#### g:ifInstalled

This tag will execute its body only if a given plugin is installed. This is
useful to change page content or the resources included based on the presence of another Grails plugin.

Attributes

* plugin - The bean-name camelCase form of the plugin's name i.e. "jquery" or "springSecurityCore"

Example:

    <g:ifInstalled plugin="jquery">
       <r:require module="my-ui-jquery"/>
    </g:ifInstalled>

    <g:ifInstalled plugin="prototype">
       <r:require module="my-ui-prototype"/>
    </g:ifInstalled>

    <g:ifInstalled plugin="springSecurityCore">
       <r:require module="my-ui-springtools"/>
    </g:ifInstalled>

#### g:ifNotInstalled

This is simply the direct inverse of g:ifInstalled

### General purpose tags

There are some general purpose tags provided for convenience.

#### g:html

The g:html tag replaces <html> and renders the correct doctype for
you (<g:html doctype="html 4.01">...).

This saves you having to worry about the details of HTML doctypes, and also
allows you to easily parameterise them in the case of plugins that provide
views.

Attributes

* doctype - Optional. Defaults to "html 5". Also supports "html 4.01", "xhtml 1.0" and "xhtml 1.1"

### Themes

The ability to provide high level UI functionality separate from their styling
is vital for plugins that wish to add front- or back-end screens to apps.

Furthermore, application developers and users also want to be able to change
the visual theme of the application.

There are several challenges with such an API. You should be able to switch
themes with no application code changes. Ideally you would be able to choose
whichever JS library you prefer. You should be able to easily override or
modify some stylings, and core UI elements supplied by plugins should look
consistent with the theme your application is using.

To achieve this, the Themes API does the following:

1. Introduces the concept of a theme that comprises one or more page layouts and GSP templates and resource modules.
2. Introduces a simple API for getting and setting the current user's selected Theme, reverting to Config for defaults
3. Provides some tags for implementing themes
4. Abstracts key UI elements such as blocks, messages and tabs that themes often need to style
5. Uses Resources framework to pull in theme resources by convention
6. Supports indirection of theme layouts, allowing developers to override the layout from a given theme that is used for a given page type

Combined with Resources framework module dependencies and the Grails plugin
dependency mechanisms, it becomes trivial to include powerful CSS and JS
libraries and supplementary code to support the theme, without the application
requiring any extra code. 

Themes can even adapt their JS code so that they can support the JS library
that the application developer wants to use, by providing multiple
implementations of behaviours or UI libraries.

The intention is that you can rapidly develop applications with polished UI.
Then if need be you can fork/create your own themes to create your unique look
and feel, and that any plugins you use that expose UI will continue to fit in
with you custom look and feel.

#### Using Themes in Applications

There are just a few steps to get instant high quality UI in your application:

1. Install one or more theme plugins
2. Edit GSPs to indicate which layout they need
3. (optional) Edit GSPs to put content into correct "zones"
4. (optional) Tell it which theme you wish to use by default

##### Installing a theme

To use themes, you need to install a theme plugin. For example the "default" theme:

    grails install-plugin default-theme
 
##### Edit your GSPs 
   
Next you need to amend your GSPs to specify what kind of page layout they require. You do this using the theme:layout tag:

    <html>
        <head>
            <theme:layout name="main"/>
        </head>
        ...
    </html>
    
This will tell Grails to use a Theme Sitemesh layout instead of one from
your application. There is a set of standard layouts that all themes must
support:

* main - A "standard" web application view with main content area and side bar
* report - A report view, for listing data in a table with pagination
* data-entry - A data-entry screen e.g. for creating/updating data records
* form - A "full screen" form used e.g. to log in or sign up users, with no navigation or other page elements
* full - A layout with no side bar, usually like report but with no pagination

All of these layouts must include common "main" and user-navigation, except where specified explicitly.

##### Edit your GSPs to define content for zones

Themes define content "zones" in the output page that you can populate with blocks of content from your GSP.

A prime example is the site navigation or a sidebar. If you don't define any zones in your GSPs, which will be the case if you have not edited them,
it will simply render your GSP content's body as the "body" zone.

However if you use the "main" layout for example, you will have an empty sidebar.

To put content into the sidebar you need to wrap the parts of your GSP with *theme:zone*:

    <body>
        <theme:zone name="body">
            This is the main part of your page
        </theme:zone>
        <theme:zone name="sidebar">
            This is the main part of your page
        </theme:zone>
    </body>
    
If you don't supply content for a zone, the theme will try to load a GSP template at /_themes/_templates/_<name>.gsp to render the content.
    
If that is not found, it will log a warning.

This way themes can provide a range of places where you can inject content,
and you can customize these per-page in your GSPs or application-wide using GSP templates.

Zones are additive. You can call theme:zone multiple times with the same name,
and the content will be appended to the content already defined for that zone.
This means that plugins or GSP templates can append content to e.g. a sidebar.

##### Choose which theme to use

You can tell the Themes API which theme to use on a per-request basis.

However often you just have one theme installed and want to use that, or you have a few installed but want it to default to a specific one.

If you have just one or more than one theme installed, there is nothing to do - it will default to the first in alphabetical order

If you have multiple themes installed and wish to specify the default to use
when no explicit theme is set per request, use the grails.theme.default Config.groovy variable:

    grails.theme.default = 'fancy'

#### UI Element tags and templates

The ui:xxxx tags provide the mechanism for Themes to alter how core UI elements are rendered.

These are rendered using GSP templates for each tag. The templates can be defined by the application, the current active theme, 
or fall back to plugin platform defaults.

The tags all render a GSP template located by convention, passing in useful
values which typically include:

* classes - string list of CSS classes specified
by caller
* text - the body of the text or message, the part the application passed in
* attrs - the attributes passed in, minus those used by the ui:xxx tag

##### ui:attributes

This is a utility tag for writing out the attributes of the original ui:xxxx tag. GSP templates that provide theme-specific
renderings of UI elements can use this to copy application-supplied attributes to the output, for example the width and height of an image.

Attributes:

* 

##### ui:block

##### ui:message

##### ui:avatar

##### ui:image

##### ui:tabs

##### ui:tab

##### ui:message

##### ui:navigation

##### ui:h1 .. h4

##### ui:baseHeading

#### The Theme previewer

Just browser to http://localhost:8080/<yourapp>/platform/themes/

For a sample page with all the UI elements in it:

Just browser to http://localhost:8080/<yourapp>/platform/themes/ui

#### Overriding Theme layouts

#### Overriding Theme UI element templates


#### Creating a theme

Themes are Sitemesh layouts located by convention - they must define the
markup that forms the page layout and pull in any and all resources they need.
As with all Sitemesh layouts they can also contain other GSP tags and logic.

There are a bunch of tags to make this easy, and a theme preview tool.

You can create a skeleton of a theme inside an existing Grails application or
plugin project using the Grails command:

grails create-theme <themename>
    
Then you can run-app your app/plugin and browse to:

http://localhost:8080/<yourapp>/platform/themes/

There you can select from the installed themes and layouts in each theme.
Using this you can edit your layouts and reload the page to see changes
instantly, using dummy text. You can even customize the dummy text that your
theme shows for each zone (see theme tags).

#### Themes and Page Layouts

A theme consists of one or more page layouts. The exact appearance of a page
is determined by the page layout you request from the theme.

Themes in plugins must use the Config API to define the mapping between their supported page layout names and their sitemesh themes:

    doWithConfig = { config ->
        application {
            // If you decide to use different layout names...
            grails.theme.mytheme.layout.mapping.fullscreen = 'full'
            grails.theme.mytheme.layout.mapping.'three-column' = 'threecol'
            grails.theme.mytheme.layout.mapping.'two-column' = 'main'
            grails.theme.mytheme.layout.mapping.'user-profile' = 'main'
        }
    }

If defining a theme in your app, just put this config information in Config.groovy

If you do not supply this information, the file "theme.gsp" will be used for all layouts.

This is fine for themes that have simple layout requirements that can switch on the current layout name passed (use theme:ifLayoutIs tag)

#### Defining layouts

Themes are located in grails-app/views/layouts/themes/[themename]/ - the
Sitemesh layout GSP file for the requested theme layout is loaded from there.

So for example a theme called "sunshine" that has a page layout called "main" would look like this:

Filename: grails-app/views/layouts/themes/sunshine/main.gsp
    
    <g:html>
        <theme:head/>
        <theme:body bodyAttrs="['id', 'onload']">
            <div class="span-6">
                <theme:layoutZone name="sidebar"/>
            </div>

            <div class="span-18 last">
                <theme:layoutZone name="body"/>
            </div>
        </theme:body>
    </g:html>
    
Here the g:html tag supplied as a convenience is used to render the DTD and body of the page.
You can pass a "doctype" attribute to this tag to set the HTML variant you wish to use - e.g. "html 5", "html 4.01", "xhtml 1.1"

The theme:head tag renders the regular head section and the required
resources. An optional body for this tag allows the theme to add more head
markup.

The theme:body tag renders the body, copying across any bodyAttrs properties
from the GSP page into attributes of the body tag. This tag automatically
locates and renders GSP templates for the header and footer by convention, in the above
example they would be loaded from:

    grails-app/views/layouts/themes/sunshine/fullscreen/_header.gsp
    grails-app/views/layouts/themes/sunshine/fullscreen/_footer.gsp

These are rendered either side of the body of the theme:body tag (the main
layout body), so you can avoid explicit g:render calls.

Inside the theme:body tag body, you see markup laying out the page, and
theme:layoutZone tags that set where the content zones defined in the GSP page
will be rendered.

The "body" zone is always available. Others will only be available if the
developer supplied content for them in their GSP.

#### Tags for theme implementors

##### theme:head

Renders a <head> section, with automatic <title> tag, automatic r:require tags
to pull in resource modules with names of the form theme.<themename> and
theme.<layoutname>, and invokes r:layoutResources.

Accepts an optional body that will render before the r:layoutResources in case
you need to add more resources or meta.

##### theme:body

Renders a <body> section with body attributes copied from the GSP page, if the attribute name is listed in bodyAttrs.
    
If it detects there is no zone defined by the GSP page with name "body", it will automatically render the GSP page body using g:layoutBody.
If there is a zone defined called "body", it will just call the body of this tag, and let it control layout of the zones.

Finally, it writes out the second r:layoutResources before the closing body tag.

This means you never have to worry about resources tags.

Attributes:

* bodyAttrs (Optional) - List of attribute names to copy from the GSP page's body tag

##### theme:resources

This tag is used by theme:head to invoke the r:require tags needed to include
the theme's resources. If for some reason you do not want to use theme:head,
you can call this to get the resource conventions included.

##### theme:layoutZone

When a GSP defines the content for the different zones in the page, they use the theme:zone tag. To render these in your layout, you call this theme:layoutZone tag.

Attributes:

* name - The name of the zone to render

##### theme:currentTheme

This tag simply outputs the name of the current theme. The current theme is the one being used to render the current HTTP request

##### theme:ifLayoutIs

Sometimes you may want to use the same theme Sitemesh layout GSP to render
multiple theme page layouts. To do this you may need to check what page layout
the GSP requested i.e. "report" or "dataentry" and adapt rendering based on this.

The body will only be executed if the application's GSP page is being rendered with the specified page layout name.

Attributes:

* name - The name of the page layout i.e. "report"

##### theme:ifLayoutIsNot

The inverse of theme:ifLayoutIs

##### theme:template

This renders shared GSP template fragments that are specific to your theme.

These GSPs live in grails-app/views/_themes/<themename>/_<templatename>.gsp and follow the normal underscore prefix convention of g:render.
    
Attributes:

* name - The name of the template e.g. "header"

#### Required page layouts

There is a minimum set of page layouts required for a theme to be valid. This means that plugins can by default use these layouts and application developers can avoid manually wiring theme layouts up, as much as possible.

* form - a "fullscreen" layout that allows a GSP to present a form to the user, with no other content or navigation, typically for login screens. Zones:'body' only
* report - a layout for rendering report/list screens with paging. Zones:'body' and 'pagination' only 
* dataentry - a layout for rendering scaffolded create/show/update screens. Zones:'body' only 
* main - a normal layout presenting navigation, content areas etc. Zones:'body' and 'sidebar' only

#### Telling the Theming API what theme to use for a request

You do not need to tell the API which theme to use for each user. If your
application does not support theme switching you can just set this value once
in config - or leave it if "default" works for you.

Setting the default theme requires simply setting the following Config variable:

    grails.theme.default.theme = "shiny"

However for more advanced applications you may wish to change the theme at runtime, application-wide or per user.

To achieve this you can just inject the grailsThemes bean and call one of the following methods:

    void setRequestTheme(request, String theme) 
    void setSessionTheme(request, String theme) 

These will set the theme for the current request or per session. 

#### Overriding page layout location

A GSP page from a plugin may request a page layout not yet supported by your
theme. To deal with this, you can provide custom paths for each page layout name on a per-theme basis.

This gives you a level of indirection to deal with any incompatibilities
between plugins and the themes in use, for example to supply the layout in
your application instead of in the theme.

Themes define the mappings between page names and their sitemesh layout names.
NOTE: In future this should not be necessary, but is for the moment

To override this you define the correct GSP path for the page layout name in the theme:

    grails.theme.<theme-name>.layout.mapping.<page-layout-name> = 'alternative'
    grails.theme.layout.mapping.<page-layout-name> = 'another'
    grails.theme.default.layout = 'fallback'

View paths based at grails-app/ are supported if prefixed with /, otherwise
the name is assumed to be relative to the theme's layout folder i.e:

    grails-app/views/layouts/themes/sunshine/<layout-name-here>.gsp

If no page layout mapping is supplied, the name defaults to "theme" and would load in this example:

    grails-app/views/layouts/themes/sunshine/theme.gsp
    
  
### Security API


### Navigation API


### Events API

Plugin platforms brings an event API to allow decoupled communication within your application and beyond.
If you want to use more features like routing mechanisms or external message brokers, you could also rely on specifics plugins which implement
the platform events API such as Spring Integration (si-events).

#### Declaring an event listener
A listener is an handler attached to a topic name, ie 'userLogged' or 'bookArchived'.
This handler can receive and return values as we will see in the 'Sending Events' paragraph.

There are 3 ways to declare listeners :

##### Services artefacts :

You can surround candidate methods with @Listener(String topic. If you don't define explicitly any topic, then the method name will be used :

```groovy
class SomeService{
   @grails.events.Listener('userLogged')
   def myMethod(User user){
      //do something with user
   }

   @grails.events.Listener
   def mailSent(User user){ //use 'mailSent' as topic name
         //do something with user
  }
}
```

##### Inline closures :

Inside services, domains and controllers artefacts, you can call "String addListener(String topic, Closure closure)".
This method returns a listener Id which is under the following format "topic:ClassName#method@hashCode":

```groovy
class SomeController{

   def auth(){
      String listenerId = addListener('userLogged'){User user->
       //do something with user
      }
   }
}
```


##### Custom objects :

You can also declare runtime listeners with any object and method using "String addListener(String topic, Object bean, Method/* or String */ method)".
As previously mentionned, this method returns a listener Id which is under the following format "topic:ClassName#method@hashCode":

```groovy
class SomeController{

   def someService

   def auth(){
      addListener('userLogged', someService, 'myMethod')
   }
}
```

#### Sending events

You have 2 ways of sending events : asynchronously or syncronously. Both methods returns an EventReply object.
EventReply implements Future<Object> and provides 3 usefuls methods :
* List<Object> getValues()
Return as many values as listeners has replied.
* Object getValue()
Return the first element of getValues().
* int size()
Return the replies count.


##### Sync events
Syncronous events can be sent from domains, services and controllers artefacts by using "EventReply event(String topic, Object data)" :

```groovy
class SomeService{
    @Listener('logout')
    def method(User user){
       Date disconnectDate = new Date()

       //do something very long with user

       return disconnectDate
    }
}

class SomeController{

   def logout(){
      def reply = syncEvent('logout', session.user)
      render reply.value  //display disconnectDate
   }
}
```

##### Async events
Asyncronous events can be sent from domains, services and controllers artefacts by using "EventReply asyncEvent(String topic, Object data)" :

```groovy
class SomeService{
    @Listener('logout')
    def method(User user){
       Date disconnectDate = new Date()

       //do something with user

       return disconnectDate
    }
}

class SomeController{

   def logout(){
      def reply = asyncEvent('logout', session.user)
      render reply.value //block the thread until event response and display disconnectDate
   }
}
```

##### Waiting replies

In domains, services and controllers artefacts you can wait for events using "EventReply[] waitFor(EventReply... eventReplies)".
This method is rather useless in a sync scenario. It accepts as many events replies you want and returns the same array
for functiunal programming style. :

```groovy
class SomeService{
    @Listener('logout')
    def method(User user){
       Date disconnectDate = new Date()

       //do something with user

       return disconnectDate
    }
}

class SomeController{

   def logout(){
      def reply = asyncEvent('logout', session.user)
      def reply2 = asyncEvent('logout', session.user)
      def reply3 = asyncEvent('logout', session.user)

      waitFor(reply,reply2,reply3).each{EventReply reply->
        render reply.value +'</br>'
      }
   }
}
```


#### GORM events (Grails 2 only)

You can listen all the grails 2 gorm events using the same topic name than domain method handler described in grails documentation.
The listener argument has to be typed to specify the domain that the handler listens for. If the gorm event can be cancelled like with beforeInsert or beforeValidation,
the handler can return a false boolean to discard the event :

```groovy
class SomeService{
   @Listener('beforeInsert')
   def myMethod(User user){
      //do something with user
      false //cancel the current insert
   }
}
```

#### Removing listeners

To remove listeners, just use "int removeListeners(String listenerIdPattern)". The argument allows you to filter listeners by using the listener id pattern.
For instance you can use both "topic" and "topic:ClassName#method".
The method returns the number of deleted listeners :

```groovy
class SomeController{

   def logout(){
      println removeListeners('userLogged') //remove all listeners for topic 'userLogged'
      println removeListeners('statistics:org.sample.StatisticsService') // remove all listeners for topic 'statistics' and class 'StatisticsService'
   }
}
```

#### Counting listeners

To count listeners, just use "int countListeners(String listenerIdPattern)". The argument allows you to filter listeners by using the listener id pattern.
For instance you can use both "topic" and "topic:ClassName#method".
The method returns the number of listeners :


```groovy
class SomeController{

   def logout(){
      println countListeners('userLogged') //count all listeners for topic 'userLogged'
      println countListeners('statistics:org.sample.StatisticsService') // count all listeners for topic 'statistics' and class 'StatisticsService'
   }
}
```