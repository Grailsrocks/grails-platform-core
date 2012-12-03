/* Copyright 2011-2012 the original author or authors:
 *
 *    Marc Palmer (marc@grailsrocks.com)
 *    Stéphane Maldini (smaldini@vmware.com)
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
import groovy.io.FileType
import java.util.regex.Pattern

includeTargets << grailsScript('_GrailsInit')
includeTargets << grailsScript('_GrailsArgParsing')

NO_HEADER_PATTERN = Pattern.compile("\\s*(package|import)(.*)", Pattern.DOTALL)
HEADER_PATTERN = Pattern.compile("\\s*(/\\*.*\\*/)\\s*(package|import)(.*)", Pattern.DOTALL)

APACHE_2_LICENSE = """
/* Copyright the original author or authors.
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
"""
srcFileTypes = ['.groovy', '.java']

target("updateHeader": "Makes sure all source files contain a license") {
    depends(checkVersion, parseArguments)

    def header = new File(basedir, 'HEADER.TXT')
    if (!header.exists()) {
        println "You have no HEADER.TXT file - creating one for you using ASL 2 License. Change this if you need to and run this script again."
        header.setText(APACHE_2_LICENSE, 'UTF-8')
    }
    
    def headerText = header.getText('UTF-8')
    
    ant.input(message:"""
This script will modify all of your source (${srcFileTypes.join(', ')}) files. The first comment block before package/import will be set to:

${headerText}

Make sure you have backups/checked them in first! Continue?""", 
        defaultValue:'n', 
        validArgs:'y,n',
        addProperty:'doContinue')
        
    if (ant.project.properties.doContinue != 'y') {
        println "You chickened out, that's OK."
        return
    }

    def baseFile = new File(basedir)
    baseFile.eachFileRecurse(FileType.FILES) { f ->
        if (srcFileTypes.any { ext -> f.name.endsWith(ext) } ) {
            def t = f.getText('UTF-8')
            def m = HEADER_PATTERN.matcher(t)
            if (m.matches()) {
                def newF = new StringBuilder()
                newF << headerText
                newF << m[0][2]
                newF << m[0][3]
                f.setText(newF.toString(), 'UTF-8')
                println "Replaced header in file: $f"
            } else {
                m = NO_HEADER_PATTERN.matcher(t)
                if (m.matches()) {
                    def newF = new StringBuilder()
                    newF << headerText
                    newF << m[0][1]
                    newF << m[0][2]
                    f.setText(newF.toString(), 'UTF-8')
                    println "Replaced header in file: $f"
                }
            }
        }
    }
}

setDefaultTarget("updateHeader")
