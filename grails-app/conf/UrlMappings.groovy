class UrlMappings {

	static mappings = {
		"/"(controller:"platformTools")
        "/event/$action?"(controller:'sample')
		"500"(view:'/error')
	}
}
