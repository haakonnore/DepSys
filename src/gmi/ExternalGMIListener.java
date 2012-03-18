
package gmi;


import java.io.Serializable;

/**
 *  In GMI package, an <i>external interface</i> is an interface that
 *  declare a set of methods implemented by an object group that may be
 *  invoked from a remote Java virtual machine.  An external remote
 *  interface must satisfy the following conditions:
 *
 *  <UL>
 *  <LI>It must at least extend, either directly or indirectly, the
 *  interface <code>ExternalGMIListener</code>.  </LI>
 *  <LI> in addition to any application-specific
 *  exceptions  </LI>
 *  </UL>
 *
 *  The <code>ExternalGMIListener</code> is a marker interface that
 *  declares no methods.
 *  
 * @author Gurvinder Singh
 */

public interface ExternalGMIListener extends Serializable {

}
