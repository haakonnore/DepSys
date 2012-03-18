
package gmi;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;

/**ExternalGMIService is used by sever side proxy to handle the
 * Method invocation request by client. this invoke the requested method on
 * server application and send the result back to the client who make the request.
 * @author Gurvinder Singh
 */

public class ExternalGMIService implements InvocationHandler{
    private ServerSideProxy proxy;   
    private ExternalInvocationHandler invocationHandler;
    private Object server, obj="null";             
    private int size,key;   
    private HashMap<Integer,Object[]> resultMap;
    private final boolean DEBUG = false;
   
    public ExternalGMIService(ServerSideProxy proxy, Object server){
        this.proxy = proxy;  
        this.server = server; 
        resultMap = new HashMap<Integer,Object[]>();
        invocationHandler = new ExternalInvocationHandler(proxy, server);
    }
    
    /**
     * This method is just to fulfill the condition of InvocationHandler java
     * interface.
     * @param proxy
     * @param m
     * @param args
     * @return
     */
    public Object invoke(Object proxy, Method m, Object[] args){
        return " ";
    }
    /**
     * This method is invoked by serversideproxy to set the parameter from the
     * received method invocation request from the client or one of the servers
     * in multicast semantics case. it chekcs if request has multicast semantics
     * it creates a result array and store it in resultmap with key as methodsignature,
     * sender and messagenumber. it create a ExternalInvocationHandler Thread on each 
     * request to invoke the requested method on server application. 
     * @param m Method which need to be invoked.
     * @param protocol Protocal which is used to handle this invocation request.
     * @param methodSignature Method signature used to identify the unique method from table
     * @param args Arguments which is provided by request to pass to the invoked method
     * @param sender who sends the request to invoke the method
     * @param message Actual Message received from sender, it is needed in case of 
     * Multicast semantics case to multicast among the server group.
     */
    
     synchronized public void invokeMethod(Method m, String protocol,String methodSignature, Object[] args, String sender, Message message){
        size = proxy.getCurrentView().getView().size();  
        if(protocol.equals("Multicast") && !proxy.getCurrentView().getView().contains(sender) && message.getMessageType().equals("Request") && size>1) {
            Object[] serverResult = new Object[size+3];
            serverResult[0]=(Integer)0;
            serverResult[1]=(String)sender;
            serverResult[2]="Valid";
            key = (methodSignature+sender+message.getMessageNumber()).hashCode();            
            resultMap.put(key,serverResult);
            Thread timer = new Thread(new ExternalInvocationTimer(this, key, methodSignature));
            timer.start();
        }         
        if(message.getMessageType().equals("Reply")){
            key = (methodSignature+message.getOriginalSender()+message.getMessageNumber()).hashCode();
            int count = 0;
            try{
                Object[] serverResult = (Object[]) resultMap.get(key);
                if(((String)serverResult[2]).equals("Valid")){
                count = (Integer) serverResult[0];                  
                serverResult[count+3] = message.getResult();               
                count++;
                serverResult[0] = (Integer)count;
                resultMap.put(key, serverResult);
                }
            }
            catch(NullPointerException e){}
            catch(ArrayIndexOutOfBoundsException e){}            
            if(count == size) {
               sendReply(key, methodSignature);
               return;
            }             
        }
        if(message.getMessageType().equals("Request")){
            invocationHandler.invokeMethod(m, protocol, methodSignature, args, sender, message);
            Thread t = new Thread(invocationHandler);
            t.start();
        }
     }
    /**This is used to check that either the request is completed or not by 
     * ExternalInvoationTimer.
     * 
     * @param key for unique request
     * @return boolean - either valid or notvalid
     */
    public boolean isCompleted(int key){
        Object[] serverResult = (Object[])resultMap.get(key);
        String validity = (String)serverResult[2];
        return validity.equals("NotValid");
    }
    /**This is used to send reply back in Multicast case.
     * 
     * @param key
     * @param methodSignature
     */
    public void sendReply(int key, String methodSignature){
        Object[] serverResult = (Object[])resultMap.get(key);
        Message reply = new Message();
        reply.setMethodSignature(methodSignature);
        for(int i=3;i< serverResult.length; i++){
            obj=serverResult[i];
            if(obj instanceof Exception){
                obj = "null";
                continue;
            }
            else{                       
                reply.setResult(obj);
                break;
            }                       
        }
        try {
            if(obj.equals("null")){
                reply.setResult(serverResult[3]);
            }                  
        }
        catch(NullPointerException e){
            reply.setResult(serverResult[3]);
        }
        reply.setMessageType("Reply");
        serverResult[2]=(String)"NotValid";
        if(DEBUG){
	   System.out.println("sending reply result is: "+serverResult[3]+" to sender: "+serverResult[1]);
	}
        proxy.sendAnycast((String)serverResult[1], reply);         
    }        
    public void removeResult(int key){
        resultMap.remove(key);
    }
}
