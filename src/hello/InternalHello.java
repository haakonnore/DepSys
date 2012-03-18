
package hello;

import gmi.InternalGMIListener;

/**
 *  Internal Hello interface.  Methods provided in this interface can
 *  only be invoked from within the object group, that is only servers
 *  that are members of the group can invoke this method.  Although
 *  methods in this interface are declared as returning a single object,
 *  they will in fact return an array of objects, one for each member of
 *  the group. 
 *
 *  @author Gurvinder Singh
 */

public interface InternalHello extends InternalGMIListener {

    Object time();
}
