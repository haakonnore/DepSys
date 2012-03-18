
package hello;

import gmi.GroupProxy;

/**
 *  Client program for Hello
 *
 *  @author Gurvinder Singh
 */

public class HelloClient {

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Fields
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    private String groupname = "servergroup";
    private GroupProxy groupProxy;
    private String address = "localhost";
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor for the HelloClient
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    public HelloClient(String name, int port) {
        
        /*
         *Getting GroupProxy Object to communicate with Object group through
         * Spread Toolkit. 
         */
        groupProxy = new GroupProxy(this, name, port, groupname, address); 
       /*
        * Retrieve a proxy for an object group implementing the Hello
        * interface.
        */
        Hello server = (Hello) groupProxy.getServer();    
       /*
        * Invoking method sayHello, which returns an object of type Answer
        * containing a string produced by the contacted object plus
        * the time at which the groups members installed the last view.
        */
        for(int i=0; i<10;i++){            
            Answer answer = server.sayhello("Hi");
            System.out.println(answer);        
        }
       /*
        * There may be several threads blocking (e.g., the threads waiting
        * for result acknowledgment), thus we just exit everything.
        */
        System.exit(0);
    }        
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    // Main method (initialize the HelloClient object)
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
                
        new HelloClient(connName, port);                    
    }    

    private static void usage() {
    	System.out.println("Usage Client :: Client -c <clientname> -p <port>");
    	System.exit(1);
    }

}
