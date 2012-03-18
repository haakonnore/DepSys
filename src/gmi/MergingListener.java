
package gmi;

/**
 *
 * @author Gurvinder Singh
 */
public interface MergingListener {

    /**
   *  Method <code>getState</code> is invoked when the member has been
   *  selected as Leader for the Merging protocol.
   *
   *  
   *  @return 
   *    The Current Glabal State
   */
  public Object getState();


  /**
   *  Method <code>putState</code> is invoked to inform the member about
   *  the state of other members belonging to different partitions.
   *
   *  @param status
   *    The Global State
   *  
   */
  public void putState(Object status);
}
