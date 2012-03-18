package allocator;

import gmi.InternalGMIListener;

public interface InternalInterface extends InternalGMIListener {
	void reserveAddress(ClientObject reservedAddress );
	void releaseAddress(ClientObject releasedAddress);
	Object time();
}
