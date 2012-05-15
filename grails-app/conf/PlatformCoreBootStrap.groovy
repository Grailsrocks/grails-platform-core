import grails.util.Environment

class PlatformCoreBootStrap {
    def grailsNavigation
    def grailsApplication

    def init = {
        applicationStartupInfo()

        grailsNavigation.reloadAll()
    }
    
    def destroy = {
        
    }
    
    void applicationStartupInfo() {
        if (pluginConfig.show.startup.info) {
            def w = 70
            println '='*w
            def name = grailsApplication.metadata.'app.name'
            def ver = grailsApplication.metadata.'app.version'
            def welcome = "Application: "+(ver ? "$name $ver" : name)

            println ''
            println welcome.center(w)
            println(('-'*welcome.length()).center(w))
            println ''
            println "  Environment: ${Environment.current}"
            println '  Database configuration: '
            def ds = grailsApplication.config.dataSource
            println "    Hibernate DDL mode: ${ds.dbCreate}"
            if (ds.jndiName) {
                println "    JNDI: ${ds.jndiName}"
            } else {
                println "    URL: ${ds.url}"
                println "    Driver: ${ds.driverClassName}"
                println "    User: ${ds.username}"
            }

            println '='*w
        }
    }
}