
package hello;

import gmi.MembershipListener;
import gmi.ServerSideProxy;
import gmi.View;
import gmi.protocols.Anycast;
import gmi.protocols.Multicast;

/**Simple Hello server aimed at demonstrating the capabilities of both
 * internal and external invocations. 
 * @author Gurvinder Singh
 */
public class HelloServer implements InternalHello, Hello, MembershipListener {
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    // Fields
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    private String groupname="servergroup";
    private String address = "localhost";
    private Answer answer;
    private ServerSideProxy proxy;
    private InternalHello internalHello;
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    // Main method (initialize the HelloServer object)
    ////////////////////////////////////////////////////////////////////////////////////////////

    public static void main(String[] arg) {
       String connName = null;
       int port = 0;
       try {
           for (int i = 0 ; i < arg.length ; i += 2) {
        	   if (arg[i].equals("-c")) {
        		   connName = arg[i+1];
        	   } else if (arg[i].equals("-p")) {
        		   port = Integer.parseInt(arg[i+1]);
        	   } else {
        		   usage();
        	   }
           }
       }
       catch (Exception e) {
    	   usage();
       }
       new HelloServer(connName, port);       
       //thread sleep to avoid cpu utilization when they are waiting for any messages
       while (true) {
    	   try {
    		   Thread.sleep(100);
    	   } catch (InterruptedException e) {
    		   e.printStackTrace();
    	   }
       }
    }
    
    private static void usage() {
    	System.out.println("Usage Server :: server -c <srvname> -p <port>");
    	System.exit(1);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor for the HelloServer
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    public HelloServer(String name, int port) {
    	//Getting Serversideproxy Object to communicate with Spread Daemon.
    	proxy = new ServerSideProxy(this, port, name, address);
    	//Join server group.
    	proxy.join(groupname);
    	//internalHello to refrence dynamic proxy object for IGMI invocation.
    	internalHello = (InternalHello) proxy.getInternalStub(InternalHello.class);
    	//Preparing Answer to send to client on request.
    	answer = new Answer("Hello from " + proxy.getIdentifier());
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    // Methods from the Hello interface (External Group Method Invocation)
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    @Multicast public Answer sayhello(String arg) {
    	System.out.println("sayHello: returning " + answer +" to "+proxy.getExternalIdentifier()+" received "+arg);        
        return answer;
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    // Methods from the InternalHello interface (Internal Group Method Invocation)
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    public Object time() {
        long time = System.currentTimeMillis();
        System.out.println("Generating time: " + time);        
        return new Long(time);
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    // Methods from MembershipListener
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    public void ViewChange(View view) {
      /*
       * The time() method is defined in the InternalHello interface and
       * is marked as a group internal method.  By definition, all group
       * internal methods will return an array of values instead of a
       * single value as with standard remote (or external group) method
       * calls.
       */
        Object[] objs = (Object[]) internalHello.time();
        for (int i = 0; i < objs.length; i++)
            System.out.println("Time: " + objs[i]);
        answer.setTime(objs);
    }    
}
