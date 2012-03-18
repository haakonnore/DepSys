
package gmi;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**This Object is used to handle the invocation on server application
 * on receiving the request from clients or servers for External Methods.
 *
 * @author Gurvinder Singh
 */
public class ExternalInvocationHandler implements Runnable{
    
    private ServerSideProxy proxy;
    private Object server;
    private Object result;
    private String protocol, methodSignature, sender;
    private Object[] args;
    private Message message;
    private Method method;    
    
    public ExternalInvocationHandler(ServerSideProxy proxy, Object server){
        this.proxy = proxy;  
        this.server = server;   
    }    
    
    synchronized public void invokeMethod(Method m, String protocol,String methodSignature, Object[] args, String sender, Message message){
        method = m;
        this.protocol=protocol;
        this.methodSignature = methodSignature;
        this.args = args;
        this.sender = sender;
        this.message = message;                            
    }
    
    public void run() {
        final String orgsender = sender;
        final String orgmethodSignature = methodSignature;
        final Message orgMessage = message;
        if(orgmethodSignature.equals("getView")){           
            Message replyMessage = new Message();
            replyMessage.setMethodSignature(orgmethodSignature);
            replyMessage.setMessageType("Reply");
            replyMessage.setResult(proxy.getCurrentView());
            proxy.sendAnycast(orgsender,replyMessage);
            return;
        }        
        if(protocol.equals("Multicast") && !proxy.getCurrentView().getView().contains(orgsender) && proxy.getCurrentView().getView().size()>1){
                proxy.sendMulticast(orgMessage); 
                return;
        }		
        if(orgMessage.getMessageType().equals("Request")) {            
                try {
                    result = method.invoke(server, args);                     
                }
                catch (IllegalAccessException e){
                    e.printStackTrace();
                }
                catch (InvocationTargetException e){
                    result = e.getTargetException();
                }
                Message reply = new Message();
                reply.setMethodSignature(orgmethodSignature);
                reply.setResult(result);
                reply.setOriginalSender(orgMessage.getOriginalSender());
                reply.setMessageType("Reply");
                reply.setMessageNumber(orgMessage.getMessageNumber());
                proxy.sendAnycast(orgsender, reply);            
         }
    }

}
