
navigation = {
    app {
        home(controller:'test', action:'home')
        about(controller:'test', action:'about')
        contact(controller:'test', action:'about')
        messages(controller:'test', action:'messages') {
            inbox action:'inbox'
            archive action:'archive'
            trash action:'trash'
        }
    }
    
    user {
        login(controller:'auth', action:'login')
        logout(controller:'auth', action:'logout') // isVisible...
        signup(controller:'auth', action:'signup') // isVisible...
        profile(controller:'auth', action:'profile') // isVisible...
    }

    footer {
        terms(view:'terms')
        privacy(view:'privacypolicy')
        help(view:'help')
    }
}