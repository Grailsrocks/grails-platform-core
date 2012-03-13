class PlatformCoreBootStrap {
    def grailsNavigation
    
    def init = {
        grailsNavigation.reloadAll()
    }
    
    def destroy = {
        
    }
}