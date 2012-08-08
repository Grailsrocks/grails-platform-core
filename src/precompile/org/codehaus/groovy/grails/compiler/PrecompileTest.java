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
package org.codehaus.groovy.grails.compiler;

import org.apache.log4j.Logger;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.grails.commons.ControllerArtefactHandler;
import org.codehaus.groovy.grails.compiler.injection.AstTransformer;
import org.codehaus.groovy.grails.compiler.injection.GrailsArtefactClassInjector;

import java.net.URL;

/**
 * @author Stephane Maldini <smaldini@vmware.com>
 * @version 1.0
 * @file
 * @date 02/02/12
 * @section DESCRIPTION
 * <p/>
 * [Does stuff]
 */
@AstTransformer
public class PrecompileTest implements GrailsArtefactClassInjector {
    static private final Logger log = Logger.getLogger(PrecompileTest.class);

    public String[] getArtefactTypes() {
        return new String[]{ControllerArtefactHandler.TYPE};
    }

    public void performInjection(SourceUnit source, GeneratorContext context, ClassNode classNode) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void performInjection(SourceUnit source, ClassNode classNode) {
        System.out.println("//To change body of implemented methods use File | Settings | File Templates.");
    }

    public boolean shouldInject(URL url) {
        return false;
    }
}
