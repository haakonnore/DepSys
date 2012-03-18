
package gmi;

/** In order to be notified of membership events like view changes, an
 *  object must implement this interface.
 *
 * @author Gurvinder Singh
 */

public interface MembershipListener {
    
   /**Upcall invoked on members implementing the <code>MembershipListener</code>
   *  interface, when a view change occurs.
   *
   * @param view
   *   The new view that has been installed.
   * 
   */
    void ViewChange(View view);    
}
