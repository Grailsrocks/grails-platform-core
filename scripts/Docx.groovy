/*
* Copyright 2004-2005 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

/**
 * @author Graeme Rocher
 * @since 1.0
 *
 * Created: Sep 20, 2007
 */

includeTargets << grailsScript("_GrailsDocs")

target(addMacros: 'New Macros') {
    depends(setupDoc)

    config.grails.doc.css = config.grails?.doc?.css ?: new File("$platformCorePluginDir/src/docs/templates/css")
    config.grails.doc.style = config.grails?.doc?.style ?: new File("$platformCorePluginDir/src/docs/templates/html")

    org.radeox.macro.MacroLoader.newInstance().add(org.radeox.macro.MacroRepository.instance, new org.radeox.macro.Preserved() {
        @Override
        String getName() {
            'docx'
        }

        @Override
        void setInitialContext(org.radeox.api.engine.context.InitialRenderContext context) {
            super.setInitialContext(context)
            addSpecial('[' as char)
            addSpecial(']' as char)
            addSpecial('{' as char)
            addSpecial('}' as char)
            addSpecial('*' as char)
            addSpecial('-' as char)
            addSpecial('#' as char)
            addSpecial('@' as char)
            addSpecial('<' as char)
            addSpecial('>' as char)
            addSpecial('\\' as char)
            addSpecial('\n' as char)
            addSpecial('\r' as char)
        }

        @Override
        void execute(Writer writer, org.radeox.macro.parameter.MacroParameter params) {
            def content = replace(org.radeox.util.Encoder.unescape(params.content))
            if (params.length == 0) {
                writer << '<pre class="brush: groovy;">' << content << '</pre>'
            } else {
                switch (params.get(0)) {
                    case 'xml':
                        writer << '<pre class="brush: xml;">' << content << '</pre>'
                        break
                    default:
                        writer << content
                }

            }
        }
    })
}

target(extradocs: "Extra Docs") {
    depends(createConfig, addMacros, docs)
}


setDefaultTarget("extradocs")