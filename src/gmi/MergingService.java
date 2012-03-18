
package gmi;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**MergingService Object handles synchronization of Global State, Group Leader
 * invokes the getGlobalState() method to get the global state of the group
 * and multicast global state to group only newly created member or other
 * members during partion merging update their global state by calling 
 * putGlobalState() method which actually invoke the method putState()
 * declared in MergingListener interface.
 * 
 * @author Gurvinder Singh
 */

public class MergingService extends Thread{
    
    private Object server, result;
    private ServerSideProxy proxy;  
    private Method[] mergingMethod;
    private Object[] args;
    private String sender, cause;
    private final boolean DEBUG = false;

    public MergingService(ServerSideProxy proxy, Object server) {        
        this.proxy = proxy;
        this.server = server;        
        mergingMethod = proxy.getMethodTable("Merging");
       
    }
    /**Mehtod is invoked only in leader to get the global state
     * 
     * @param cause Cause repersents the cause to invoke getGlobalState() method
     * this is either "join" in case of joining new server or "Network" in case 
     * of partition Merging.
     */
    public void getGlobalState(String cause) {
        if(mergingMethod.length==0){
            return;
        }
        for(int i=0; i<mergingMethod.length; i++){
            if(mergingMethod[i].getName().equals("getState")){
                try{                    
                    result = mergingMethod[i].invoke(server);
                    if(DEBUG) {
                        System.out.println("result is: "+result+" invoked in: "+proxy.getIdentifier());
                    }
                }
                catch(IllegalAccessException e){
                    e.printStackTrace();
                }
                catch(InvocationTargetException e){                                               
                    e.printStackTrace();
                }
            }                                        
        }
        Message globalState = new Message();
        globalState.setMethodSignature("putState");
        globalState.setResult(result);
	globalState.setMessageType(cause);
	globalState.setOriginalSender(proxy.getIdentifier());
        proxy.sendMulticast(globalState);       
    }
    
    public void putGlobalState(Object state, String sender, String cause){        
        if(!proxy.getIdentifier().equals(sender)) {
            setArgument(state);            
            if(DEBUG){
                System.out.println("Result is: "+state);
            }
        }
	this.sender = sender;
        this.cause = cause;	
    }
    /**When the putState message is received by serversideproxy it create a 
     * new thread to handle the synchronization of global state and run() is executed by 
     * newly created thread to handle the method invocation for putting new state. 
     */
    @Override
    public void run(){
	try{
            if(!proxy.getIdentifier().equals(sender) && ((System.currentTimeMillis()-proxy.getStartTime())<1000 || cause.equals("Network"))){
                for(int i=0; i<mergingMethod.length; i++){
                    if(mergingMethod[i].getName().equals("putState")){
                        try{                            
                            result = mergingMethod[i].invoke(server, args);
                            if(DEBUG) {
                                System.out.println("put global state result is: "+result);
                            }
                        }
                        catch(IllegalAccessException e){
                            e.printStackTrace();
                        }
                        catch(InvocationTargetException e){                                               
                            result = e;
                        }
                    }                                        
                }                  
                if(DEBUG) {
                    System.out.println("putting new state");
                }
                proxy.synchronizedView();  
            }      
        }
        catch(NullPointerException e){
            return;
        }                   
    }
    public void setArgument(Object arg){
       ArrayList<Object> argv = new ArrayList<Object>();
       argv.add(arg);
       args = argv.toArray();
   }
}
