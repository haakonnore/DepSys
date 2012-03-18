
package gmi;

/**ExternalInvocationTimer is used to check if the result during
 * Multicast semantics is sent back or not. if not then it sends
 * the result which server application has till yet and sent one
 * of the result back to client.
 *
 * @author Gurvinder Singh
 */
public class ExternalInvocationTimer extends Thread{

    private ExternalGMIService egmi;
    private int key;
    private String methodSignature;
    public ExternalInvocationTimer(ExternalGMIService egmi, int key, String methodSignature){
        this.egmi=egmi;
        this.key = key;
        this.methodSignature = methodSignature;
    }
    @Override
    public void run(){
        try {
            Thread.sleep(2000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }         
        if(!egmi.isCompleted(key)){            
            egmi.sendReply(key, methodSignature);
        }   
        else{
            egmi.removeResult(key);
        }
    }
}
