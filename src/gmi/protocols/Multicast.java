/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gmi.protocols;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *Marker Annotation for Specifying Multicast Protocol
 *by application and used by ExternalGMIService to handle invocation. 
 * @author Gurvinder Singh
 */

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Multicast {

}
