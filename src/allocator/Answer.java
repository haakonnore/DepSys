package allocator;

import java.io.Serializable;

/**
 *  Answer object returned by sayHello sds
 *
 *  @author Gurvinder Singh
 */

public class Answer implements Serializable {
	    
	    /** Hello message */
	    private String message;
	    private int leaseTime;
	    /** Time at which the last view has been installed by members */
	    private Object[] time;       
	    
	    /**
	    *  Creates a new <code>Answer</code> object instance with the
	    *  specified message.
	    *
	    *  @param message
	    *    The message to return as an answer to the sayHello() method.
	    */
	    public Answer(String message)
	    {
	        this.message = message;
	    }
	
	    public void appendToMessage(String message)
	    {
	        this.message += message;
	    }
	    public void setLeaseTime(int time){
	    	leaseTime = time;
	    }
	
	    public void setTime(Object[] timeValues)
	    {
	        time= timeValues;
	    }
	
	    @Override
	    public String toString()
	    {
	        StringBuilder buf = new StringBuilder();
	        buf.append(message);
	        buf.append(": ");
	        if (time != null) {
	            for (int i = 0; i < time.length; i++) {
	                buf.append(time[i]);
	                buf.append(" ");
	            }
	        }
	        return buf.toString();
	    }
}


