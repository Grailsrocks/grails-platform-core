//includeTargets << grailsScript("_GrailsInit")

//ant.property(environment: "env")


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