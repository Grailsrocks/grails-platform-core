class UrlMappings {

	static mappings = {
		"/test"(view:'test')
		"/"(controller:"platformTools")
        "/event/$action?"(controller:'sample')
		"500"(view:'/error')
	}
}
