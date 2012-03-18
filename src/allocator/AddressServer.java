package allocator;
import gmi.MembershipListener;
import gmi.ServerSideProxy;
import gmi.View;
import gmi.protocols.Anycast;
import gmi.protocols.Multicast;

public class AddressServer implements MembershipListener, ExternalInterface, InternalInterface{
/*
 * A server must provide an external interface used by clients and an internal interface used between the servers
 * by extending ExternalGMIListener and InternalGMIListener, respectively.
 */
	
	private String groupname="servergroup";
    private String address = "localhost";
    private ServerSideProxy proxy;

    private String[] addressPool= {"10.0.1.1","10.0.1.2","10.0.1.3","10.0.1.4","10.0.1.5","10.0.1.6","10.0.1.7","10.0.1.8","10.0.1.9","10.0.1.10","10.0.2.1","10.0.2.2","10.0.2.3","10.0.2.4","10.0.2.5","10.0.2.6","10.0.2.7","10.0.2.8","10.0.2.9","10.0.2.10"};
    private String[] leasePool = new String[20];
    private Long[] leaseTimeout = new Long[20];
    private int startPointer; 
    private int endPointer;
    private boolean onlyOneServer;
	public static Long leaseTimeInMinutes = (long) 20;
    private InternalInterface internalInt;
	
	
	public AddressServer(String name, int port){
		/*
		 * In the constructor of the AddressServer, you need to obtain a reference to the same services as those used by
		 * the HelloServer, i.e. references to ServerSideProxy and the internal interface.
		 */
		
		proxy = new ServerSideProxy(this, port, name, address);
		proxy.join(groupname);
		internalInt = (InternalInterface) proxy.getInternalStub(InternalInterface.class);
		emptyLeasePool();
	}
	
	private void emptyLeasePool() {
		for (int i = 0; i < leasePool.length; i++) {
			leasePool[i] = null;
			leaseTimeout[i] = null;
		}
		
	}
	
	private boolean isEmpty(){
		for (int i = 0; i < leasePool.length; i++) {
			if(leasePool[i] != null)
				return false;
		}
		return true;
	}

	public void ViewChange(View view){
		/*
		 * server must invoke its own data strcture instace to say wich part 
		 * it is to control
		 * 
		 */
		int position = -1;
		for (int i = 0; i < view.getSize(); i++)
			if (view.memberHasPosition(i, proxy.getIdentifier()))
				position = i;
		if(view.getSize() == 1){
			startPointer = 0;
			endPointer = 19;
			onlyOneServer = true;

		}else {
			onlyOneServer = false;
			if (position == 0) {
				startPointer = 0;
				endPointer = 9;
			} else {
				startPointer = 10;
				endPointer = 19;
			}
		}
		
		if (!isEmpty()) {
			System.out.println("Is Not Empty");
			for (int i = 0; i < leasePool.length; i++) {
				System.out.println("has values");
				internalInt.reserveAddress(getClientObject(i));
			}
		}
		printMyLeases();
	}
	
	private void printMyLeases() {
		System.out.println("Current Time: "+  time());
		System.out.println("Current Address Pool:");
		for (int i = startPointer; i <= endPointer; i++) {
			System.out.print(addressPool[i]);
			if (leasePool[i]!= null) {
				System.out.print(" is leased by: " + leasePool[i]);
				System.out.println(" End of lease at: " + leaseTimeout[i]);
			}
			else {
				
				System.out.println(" ");
			}
		}
		
	}
	
	private ClientObject getClientObject(int index){
		ClientObject toReturn = new ClientObject(index, leasePool[index], leaseTimeout[index]);
		return toReturn;
	}
	
	@Anycast public IPAddress getAddress() {
		String ipAddressToLease = "Address Pool full";
		ClientObject a = null;
		boolean isFull = true; 
		for (int i = startPointer; i < endPointer +1 ; i++) {
			if (leasePool[i]== null){
				ipAddressToLease = addressPool[i];
				leasePool[i] = "Implementer denne";
				String time = time().toString();
				leaseTimeout[i] = ((Long)time()) + (leaseTimeInMinutes  *1000 * 60);
				System.out.println("A new lease has been made: " + ipAddressToLease + " " + leasePool[i] + " " + leaseTimeout[i]);
				a = getClientObject(i);
				isFull = false;
				break;				
			}
		}
		if(!isFull){
			internalInt.reserveAddress(a);			
		}
		IPAddress toReturn = new IPAddress(ipAddressToLease);
		return toReturn;
	}

	@Multicast public void refresh(IPAddress ipAddress) {
		for (int i = 0; i < leasePool.length; i++) {
			if(leasePool[i].equals(ipAddress))
				leaseTimeout[i] = ((Long)time()) + (leaseTimeInMinutes  *1000 * 60);
		}
		printMyLeases();
		
	}

@Multicast	public void releaseAddress(ClientObject releasedAddress) {
		int i = releasedAddress.getIndexInIPPool();
		leasePool[i] = null;
		leaseTimeout[i] = null;
	}
	
@Multicast	public void reserveAddress(ClientObject reservedAddress) {
		System.out.println("starting reserveing addresses");
		int i = reservedAddress.getIndexInIPPool();
		leasePool[i]= reservedAddress.getClientName();
		leaseTimeout[i] = reservedAddress.getLeaseTimeout();
	}
	
	private static void usage() {
    	System.out.println("Usage Server :: server -c <srvname> -p <port>");
    	System.exit(1);
    }
	
	public Object time() {
        long time = System.currentTimeMillis();
        System.out.println("Generating time: " + time);        
        return new Long(time);
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
	       
	       new AddressServer(connName, port);       
	       while (true) {
	    	   try {
	    		   Thread.sleep(100);
	    	   } catch (InterruptedException e) {
	    		   e.printStackTrace();
	    	   }
	       }
	}

}
