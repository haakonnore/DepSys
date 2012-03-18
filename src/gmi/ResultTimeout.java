
package gmi;
import spread.SpreadMessage;
import spread.SpreadException;

/** Timer in case if server crash after sending the acknowledgement of message received 
 *but befire sending the reply of requested service.
 * @author Gurvinder Singh
 */
public class ResultTimeout extends Thread {

    private GroupProxy proxy;   
    private String sender;
    private SpreadMessage msg;
    private Object obj;
    boolean acknowledge;
    private Boolean ack;
    
    public ResultTimeout(GroupProxy proxy, String sender, SpreadMessage msg) {
       this.proxy = proxy;       
       this.msg =  msg;   
       this.sender = sender;
    }  
    @Override
    public void run() {
        try {
            Thread.sleep(5000);
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
            ack = (Boolean)proxy.getResultAck().get(((Message)obj).getMessageNumber());          
        }
        
        acknowledge = ack.booleanValue();
        if(!acknowledge) {                                         
                proxy.resultTimeout(obj);
            
         }       
    }
}
