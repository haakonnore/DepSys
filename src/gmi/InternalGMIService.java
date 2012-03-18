
package gmi;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;

/** InternalGMIService handles the method invocation which are internal to server groups
 * these method are invoked by all the running members of group and return result
 * is the array of Objects of size running members at the time of invocation.
 * @author Gurvinder Singh
 */

public class InternalGMIService implements InvocationHandler {

  private Object server;
  private ServerSideProxy serverproxy;
  private Object[] serverresult;
  private Object reply;
  private int serverCount=0, size;
  private boolean waitingReply = true, timeExpired = false;
  private long startTime;
  private final boolean DEBUG = false;
  
    public InternalGMIService(ServerSideProxy proxy, Object server)  {
        serverproxy = proxy;
        this.server = server;
    }
    
    /**
     * This method is invoked each time when any internal method is invoked on 
     * Internal interface reference proxy object. on invocatoin the request is multicasted
     * in the group and on receiving the multicasted message requested method is invoked
     * and result is send back to the sender who request the method invocation. when multicasted
     * it wait for time which is set to 1 sec now, if it receive the reply with in this time then 
     * it sends the reply back to the server otherwise it says time expire and sends the result which
     * it has by that time to the server application who invoked the method. 
     * @param proxy Internal Proxy Object
     * @param m Method which is requested to be invoked
     * @param args Arguments required to pass to method
     * @return Result obtained after method invocation locally
     * @throws java.lang.Throwable
     */
    public Object invoke(Object proxy, Method m, Object[] args)
           throws Throwable {    
    size = serverproxy.getCurrentView().getView().size();
    if(DEBUG) {
        System.out.println("Server: Invoking " + m.getName());
        System.out.println("Size is: "+size);
    }
    serverresult = new Object[size];
    String signature = serverproxy.getMethodSignature(m);
    Message message = new Message();
    message.setMethodSignature(signature);
    message.setArguments(args);
    message.setMessageType("Request");
    message.setMessageNumber(0);
    serverproxy.sendMulticast(message);
        synchronized(this){
            startTime = System.currentTimeMillis();
            while(waitingReply){
                try {                
                    this.wait(1000);
                    if(System.currentTimeMillis()-startTime>1000)
                       timeExpired = true;                            
                    if(timeExpired)
                        waitingReply = false;
                }    
                catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
            waitingReply = true;
            serverCount = 0;
            return serverresult;         
        }       
    }
    
    /**
     * Method is invoked when serversideproxy receives the request from one of the member
     * to invoke the internal method and send the result back to the sender who request
     * the method invocation.
     * @param m Method to invoke
     * @param internalSender Sender who sends the request for invocation
     * @param args Arguments which is passed to method for invocation
     * @param result Result obtained after invocation and send back to sender
     * @param signature Method signature which is invoked
     * @param messagetype it is either "Request" or "Reply", if it is "Request"
     * method is invoked and result is sent back to sender and if it is "Reply"
     * then result is set in the serverResult array.
     * 
     * After it receives the result from all of the members in the group and
     * then it notify the Blocking request for reply and sends back the result to 
     * application who requested to invoke method.
     */
    
    public void invokeMethod (Method m, String internalSender, Object[] args, Object result, String signature, String messagetype) {        
        if(messagetype.equals("Request")){
            try {
               reply = m.invoke(server, args);
               if(DEBUG) {
                   System.out.println(m.getName()+" Invoked igmi"+" result is "+reply);
               }
            }
            catch (IllegalAccessException e){
                e.printStackTrace();
            }
            catch (InvocationTargetException e){
                reply = e.getTargetException();
            }
            Message message = new Message();
            message.setMethodSignature(signature);
            message.setArguments(args);
            message.setMessageType("Reply");
            message.setResult(reply);            
            serverproxy.sendAnycast(internalSender,message);
        }
        else{
            try{
                 serverresult[serverCount] = result;
            }
            catch(ArrayIndexOutOfBoundsException e){ }           
            serverCount++;
            if(serverCount==size){
                synchronized(this){
                    waitingReply = false;
                    serverCount =0;
                    notifyAll();
                }
            }            
        }         
    }    
}
