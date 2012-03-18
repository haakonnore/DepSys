
package gmi;
import spread.*;

/**TomeOut Object is used to set the timer when GroupProxy sends the message
 * to serversideproxy and if it receives the reply with in specified time which
 * is currently set to 1 sec then it do nothing and if groupproxy doen't receive
 * reply with in time limit it specify timeout or grouptimeout depending upon
 * the message is sent to one server or group.
 * @author Gurvinder Singh
 */

public class TimeOut extends Thread {

    private GroupProxy proxy;   
    private String sender;
    private SpreadMessage msg;
    private Object obj;
    boolean group, acknowledge;
    private Boolean ack;
    private final boolean DEBUG = false;
    
    public TimeOut(GroupProxy proxy, String sender, SpreadMessage msg, boolean group) {
       this.proxy = proxy;       
       this.msg =  msg;   
       this.sender = sender;
       this.group = group;
    }  
    @Override
    public void run() {
        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            obj = msg.getObject();            
        }
        catch (SpreadException e) {
            e.printStackTrace();
        }
        
        if(obj instanceof Message) {                     
            ack = (Boolean)proxy.getAck().get(((Message)obj).getMessageNumber());          
        }
        
        acknowledge = ack.booleanValue();
        if(!acknowledge) {
            
            if(group) {
                proxy.groupTimeOut();
            }
            else {                
                if(DEBUG) {
                    System.out.println("Timeout due to server::"+sender);
                    System.out.println("timeout for "+((Message)obj).getMessageNumber()+" which is "+proxy.getAck().get(((Message)obj).getMessageNumber()));
                }                
                proxy.timeOut(sender, obj);
            
            }
        }
    }
}
