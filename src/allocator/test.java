package allocator;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author haakonlo
 *
 */
public class test {	
	public static void main(String[] args) throws UnknownHostException {
		InetAddress oneAddress= InetAddress.getByAddress("192.168.0.1", null);
		System.out.println(oneAddress.getHostAddress());
	}
}
