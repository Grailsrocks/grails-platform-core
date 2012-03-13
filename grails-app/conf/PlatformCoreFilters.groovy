class PlatformCoreFilters {

    def grailsNavigation
    
    def filters = {
        navigationActivator(controller:'*', action:'*') {
            before = {
                grailsNavigation.setActivePathFromRequest(request, controllerName, actionName)
            }
        }
    }
}
