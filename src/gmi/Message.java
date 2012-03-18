
package gmi;

import java.io.Serializable;

/**Message Object is the actual object which is passed during communication between
 * two proxies.
 * 
 * @author Gurvinder Singh
 */

public class Message implements Serializable {
    
    // messageNumber is an sequence number between ServersideProxy and GroupProxy
    private int messagenumber;
    // sender is who sends the request 
    private String sender, groupname;
    //methodSignature is signature of the requested method to be invoked on target.
    private String methodSignature;
    //messageType is either "Request" or "Reply" to repersent the corresponding message type.
    private String messageType;
    //result Object contains the result obtained after invocation of requested method
    private Object result;
    //args[] is the argumets which are needed to be passed to method for it's invocation.
    private Object[] args;
    
    public void setMessageNumber(int num){
        messagenumber = num;
    }
    public int getMessageNumber(){
        return messagenumber;
    }    
    public void setOriginalSender(String name){
        sender = name;
    }
    public String getOriginalSender(){
        return sender;
    }
    public void setMethodSignature(String signature){
        methodSignature = signature;
    }
    public String getMethodSignature(){
        return methodSignature;
    }
    public void setMessageType(String type) {
        messageType = type;
    }
    public String getMessageType(){
        return messageType;
    }    
    public void setResult(Object result) {
        this.result = result;
    }
    public Object getResult(){
        return result;
    }
    public void setArguments(Object[] args){
        this.args = args;
    }
    public Object[] getArguments(){
        return args;
    }
    public void setGroupName(String name) {
        groupname = name;
    }
    public String getGroupName(){
        return groupname;
    }    

}
