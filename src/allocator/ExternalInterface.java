package allocator;

import gmi.ExternalGMIListener;
import gmi.protocols.Multicast;

public interface ExternalInterface extends ExternalGMIListener{
	IPAddress getAddress();
	void refresh(IPAddress ipAddress);	
}