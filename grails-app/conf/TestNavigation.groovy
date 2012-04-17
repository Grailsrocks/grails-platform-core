
navigation = {
    app(global:true) {
        home(controller:'test', action:'home', order:-1000, data:[icon:'house'])
        about(controller:'test', action:'about', order:90, data:[icon:'question'])
        contact(controller:'test', action:'contact', order:100, data:[icon:'mail'])
        messages(controller:'test', order:0, data:[icon:'inbox']) {
            inbox()
            archive()
            trash()
        }
        
        // Add "extranet" controller and specific actions
        extranet {
            support()
            account()
        }
        
        // Add controller:'test' and all its actions
        test(action:'a, b, c')

        // Add controller:'testTwo' and all its actions, with "list" as the default item
        testTwo(action:['list', 'create'])
        
        // Add controller:'testThree' using default action, no children
        testThree()
        
        // Add controller:'testFour' using default action, explicit children
        testFour {
            list()
            create()
        }

        // Add controller:'testFive' using default action, no children
        testFive(controller:'testFive')

        // Test declaration of existing controllers
        sample()

        something(controller:'test')
        somethingTwo(controller:'test') {
            a()
            b()
            c()
        }
        somethingThree(controller:'test', action:'list') {
            d()
            e()
            f()
        }
        
        // Add "create" option with aliases that also activate it
        somethingFourWithAliases(controller:'test', action:'create', actionAliases:['save', 'update', 'edit'])
    }
    
    user(global:true) {
        login controller:'auth', data:[icon:'user']
        logout controller:'auth', data:[icon:'user-out'] // isVisible...
        signup controller:'auth' // isVisible...
        profile controller:'auth' // isVisible...
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