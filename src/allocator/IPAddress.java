package allocator;

import java.io.Serializable;

public class IPAddress implements Serializable {
	    
	    private String IPAddress;
	    private int leaseTime;     

	    public IPAddress(String IPAddress)
	    {
	        this.IPAddress = IPAddress;
	    }
	

	    public void setLeaseTime(int time){
	    	leaseTime = time;
	    }
	    
	    
	    @Override
	    public String toString()
	    {

	        return IPAddress;
	    }
}


