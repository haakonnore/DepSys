
package hello;

import gmi.ExternalGMIListener;


/**External Hello interface.  Methods provided in this interface can be
 * invoked from clients external to the object group.
 *
 * @author Gurvinder Singh
 */
public interface Hello extends ExternalGMIListener {
    
   Answer sayhello(String s);

}
