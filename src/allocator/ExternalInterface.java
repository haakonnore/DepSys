package allocator;

import gmi.ExternalGMIListener;
import gmi.protocols.Multicast;

public interface ExternalInterface extends ExternalGMIListener{
	IPAddress getAddress(String navn);
	void refresh(IPAddress ipAddress);	
}