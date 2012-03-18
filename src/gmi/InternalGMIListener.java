
package gmi;

import java.io.Serializable;

/**In GMI package, an <i>internal interface</i> is an interface that
 *  declare a set of methods implemented by an object group that 
 *  invoked from a member of the group itself.  An internal 
 *  interface must satisfy the following conditions:
 *
 *  <UL>
 *  <LI>It must at least extend, either directly or indirectly, the
 *  interface <code>InternalGMIListener</code>.  </LI>
 *  @return
 *  <code>java.lang.Object</code>.  </LI>
 *  </UL> *
 *  The <code>InternalGMIListener</code> is a marker interface that
 *  declares no methods.
 * @author Gurvinder Singh
 */

public interface InternalGMIListener extends Serializable{

}
