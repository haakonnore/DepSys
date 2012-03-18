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
		
		boolean notGivenIP = false; 
		do {
			if (notGivenIP== true) {
				try {
					System.out.println("Not given IP. Trying again in one min.");
					Thread.sleep(1*60*1000);
					System.out.println("Trying");
				} catch (InterruptedException e) {}
			}
			myIpAddress = server.getAddress(name);
			if (myIpAddress.toString().equals("Address Pool full")) {
				notGivenIP = true; 
			}
		} while (notGivenIP);


		System.out.println("I have successfully been given the IPAddress: " + myIpAddress);
		while (true) { 
			try {
				Thread.sleep(AddressServer.leaseTimeInMinutes/2*60*1000);
				System.out.println("Wow, the time goes fast, only half of the lease time is left. Refreshing");
				server.refresh(myIpAddress);
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
