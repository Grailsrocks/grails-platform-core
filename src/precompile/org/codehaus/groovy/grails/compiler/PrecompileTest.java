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
 * @author Stephane Maldini <smaldini@doc4web.com>
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
