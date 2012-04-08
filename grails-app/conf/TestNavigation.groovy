
navigation = {
    app(global:true) {
        home(controller:'test', action:'home', order:-1000, data:[icon:'house'])
        about(controller:'test', action:'about', order:90, data:[icon:'question'])
        contact(controller:'test', action:'contact', order:100, data:[icon:'mail'])
        messages(controller:'test', action:'messages', order:0, data:[icon:'inbox']) {
            inbox action:'inbox'
            archive action:'archive'
            trash action:'trash'
        }
    }
    
    user(global:true) {
        login(controller:'auth', action:'login', data:[icon:'user'])
        logout(controller:'auth', action:'logout', data:[icon:'user-out']) // isVisible...
        signup(controller:'auth', action:'signup') // isVisible...
        profile(controller:'auth', action:'profile') // isVisible...
    }

    footer(global:true) {
        terms(view:'terms')
        privacy(view:'privacypolicy')
        help(view:'help', data:[icon:'help'])
    }
    
    admin(global:false) {
        // All your scaffolding in here
    }
    
    /*
    overrides {
        // Move all the platformCore plugin items to app#tools
        // @todo work out char for path separator
        // @todo add custom body support just for the item rendering, always wrap with <ul>...</ul> because this is sane
        move 'plugin.platformCore', 'app#tools'ls -l 
        
        move 'plugin.weceem', 'admin#content'
    }
    */
}