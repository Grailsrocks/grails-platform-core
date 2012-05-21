/* Copyright 2011-2012 the original author or authors:
 *
 *    Marc Palmer (marc@grailsrocks.com)
 *    Stéphane Maldini (stephane.maldini@gmail.com)
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
import org.codehaus.gant.GantBinding

includeTargets << grailsScript("_GrailsInit")

ant.property(environment: "env")

/**
 * Hooks to the compile grails event
 */
/*
eventCompileStart = {GantBinding compileBinding ->
    setCompilerSettings()
    resolveDependencies()

    ant.taskdef(name: 'precompileGroovyc', classname: 'org.codehaus.groovy.ant.Groovyc')

    try {
        grailsConsole.updateStatus "[plugin-platform] Precompiling sources"

        pluginSettings.pluginInfosMap.each { name, pluginInfo ->
            def _path = getPluginDirForName(pluginInfo.name).path + '/src/precompile'
            if (new File(_path).directory) {
                ant.mkdir(dir: pluginClassesDirPath)
                ant.precompileGroovyc(destdir: pluginClassesDirPath,
                        classpathref: "grails.compile.classpath",
                        encoding: projectCompiler.encoding,
                        verbose: projectCompiler.verbose,
                        listfiles: projectCompiler.verbose) {

                    src(path: _path)
                    javac(projectCompiler.javaOptions) {
                        compilerarg value: "-Xlint:-options"
                    }
                }
            }
        }

        def _path = basedir + '/src/precompile'
        if (new File(_path).directory) {
            ant.mkdir(dir: classesDirPath)
            ant.precompileGroovyc(destdir: classesDirPath,
                    classpathref: "grails.compile.classpath",
                    encoding: projectCompiler.encoding,
                    verbose: projectCompiler.verbose,
                    listfiles: projectCompiler.verbose) {

                src(path: _path)

                javac(projectCompiler.javaOptions) {
                    compilerarg value: "-Xlint:-options"
                }
            }
        }

        grailsConsole.updateStatus "[plugin-platform] End precompiling"

    } catch (Exception e) {
        grailsConsole.error("Could not precompile sources: " + e.class.simpleName + ": " + e.message, e)
    }
}
*/