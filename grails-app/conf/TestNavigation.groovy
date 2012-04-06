
navigation = {
    app {
        home(controller:'test', action:'home', order:-1000, data:[icon:'house'])
        about(controller:'test', action:'about', order:90, data:[icon:'question'])
        contact(controller:'test', action:'contact', order:100, data:[icon:'mail'])
        messages(controller:'test', action:'messages', order:0, data:[icon:'inbox']) {
            inbox action:'inbox'
            archive action:'archive'
            trash action:'trash'
        }
    }
    
    user {
        login(controller:'auth', action:'login', data:[icon:'user'])
        logout(controller:'auth', action:'logout', data:[icon:'user-out']) // isVisible...
        signup(controller:'auth', action:'signup') // isVisible...
        profile(controller:'auth', action:'profile') // isVisible...
    }

    footer {
        terms(view:'terms')
        privacy(view:'privacypolicy')
        help(view:'help', data:[icon:'help'])
    }
    /*
    overrides {
        // Move all the platformCore plugin items to app#tools
        move 'plugin.platformCore', 'app#tools'
    }
    */
}