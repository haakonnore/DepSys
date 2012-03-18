package allocator;

import gmi.GroupProxy;
import hello.HelloClient;

public class Client {
	/*
	 * Not Implemented
	 * 
	 * 
	 * The client must cach AddressPoolExeptions 
	 * and print to the console that there are no IPaddresses left
	 * 
	 */
		
	private String groupname = "servergroup";
	private GroupProxy groupProxy;
	private String address = "localhost"; 
	private IPAddress myIpAddress;
	
	
	public Client(String name, int port){
		groupProxy = new GroupProxy(this, name, port, groupname, address );
		ExternalInterface server = (ExternalInterface) groupProxy.getServer();
		myIpAddress = server.getAddress(); // Metoden som tildeler ny adresse -- mot server interface? 
		System.out.println("I have successfully been given the IPAddress: " + myIpAddress);
		while (true) { 
			try {
				Thread.sleep(AddressServer.leaseTimeInMinutes/2);
			} catch (InterruptedException e) { }
//			server.refresh(myIpAddress); 	
		}	
	}	
	
	private static void usage() {
    	System.out.println("Usage Client :: Client -c <clientname> -p <port>");
    	System.exit(1);
	}
		
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
                
        new Client(connName, port);      
	}
}
