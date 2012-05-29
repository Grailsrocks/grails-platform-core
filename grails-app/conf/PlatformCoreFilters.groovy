class PlatformCoreFilters {

    def grailsNavigation
    def grailsApplication

    def filters = {
        if (grailsApplication.config.plugin.platformCore.navigation.enable) {
            navigationActivator(controller: '*', action: '*') {
                before = {
                    grailsNavigation.setActivePathFromRequest(request, controllerName, actionName)
                }
            }
        }
    }
}
