import grails.util.Environment

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
        
        if (Environment.current == Environment.DEVELOPMENT) {
            'platformDev'(uri:'/platform/**'){
                before = {
                    def UA = request.getHeader('User-Agent')
                    // OK need a regex in future... check Lion
                    def OSX = UA.indexOf('OS X 10_7')
                    if (OSX == -1) {
                        // try mountain lion
                        OSX = UA.indexOf('OS X 10_8')
                    }
                    if (OSX > -1) {
                        redirect(mapping:'platformFancy', controller:'debug', action:actionName, id:params.id)
                    }
                }
            }
        }
    }
}
