package allocator;

import java.io.Serializable;

public class ClientObject implements Serializable {
private String clientName;
private int indexInIPPool;
private Long leaseTimeout;
	
	public ClientObject(int index, String client, Long leaseTimeout){
		clientName = client;
		indexInIPPool = index;
		this.leaseTimeout = leaseTimeout;
	}

	public String getClientName() {
		return clientName;
	}

	public int getIndexInIPPool() {
		return indexInIPPool;
	}

	public Long getLeaseTimeout() {
		return leaseTimeout;
	}
	
}
