class UrlMappings {

	static mappings = {
		"/"(controller:"platformTools")
        "/platform/event/$action?"(controller:'sample')
		"500"(view:'/error')
	}
}
