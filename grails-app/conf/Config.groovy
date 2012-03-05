// configuration for plugin testing - will not be included in the plugin zip

log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}
    debug "org.grails.plugins", 'org.grails.plugin', 'grails.app'

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
           'org.codehaus.groovy.grails.web.pages', //  GSP
           'org.codehaus.groovy.grails.web.sitemesh', //  layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping', // URL mapping
           'org.codehaus.groovy.grails.commons', // core / classloading
           'org.codehaus.groovy.grails.plugins', // plugins
           'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'

    warn   'org.mortbay.log'
}
//grails.views.gsp.sitemesh.preprocess = true
grails.views.gsp.encoding="UTF-8"
grails.views.default.codec="none" // none, html, base64

grails.doc.title = "Grails Plugin Platform"
grails.doc.subtitle = "APIs and Tags for advanced Plugin integration"
grails.doc.images = new File("resources/img")
grails.doc.css = new File('src/docs/css')
grails.doc.authors = "Marc Palmer (marc@grailsrocks.com), Stéphane Maldini (stephane.maldini@gmail.com)"
grails.doc.license = "ASL 2"
grails.doc.copyright = "2012 Marc Palmer & Stéphane Maldini"
grails.doc.footer = "Please contact the authors with any corrections or suggestions"