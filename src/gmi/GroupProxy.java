
package gmi;

import spread.*;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Random;
import java.lang.reflect.Proxy;

/**GroupProxy Object implements the AdvancedMessageListener interface provided
 * by Spread java API to receive the message, it differentiate received message depending upon
 * it's type which is either membership or regular and depending upon it's type it called 
 * corresponding method to handle it. it never received the membership message
 * because when we make connection to spread daemon we specify that this
 * connection will not receive membership messages. This Object communicate with
 * spread daemon for delivering of message to target.
 * 
 * @author Gurvinder Singh
 * @see http://www.spread.org
 * Copyright (c) 1993-2006 Spread Concepts LLC.  All rights reserved.
 */

public class GroupProxy implements AdvancedMessageListener{
    private Object client;
    private SpreadConnection connection;
    private String sender = null, server, groupname, methodSignature;   
    private boolean grouptimeout, waitingView = true,recovery =  false, waitingProxy=true;    
    private Object message = null, result;
    private Class[] serverStub;
    private ExternalGMIListener external;
    private View view; 
    private ArrayList<Boolean> ack;
    private ArrayList<Boolean> resultAck;
    private Random generator = new Random();
    private ClientInvocationHandler handler;
    private final boolean DEBUG = false;
    
    public GroupProxy(Object client,String name, int port, String groupname, String address) {
                         
        this.client = client;                
        this.groupname = groupname;
	connection = new SpreadConnection();
	view = new View();
	ack = new ArrayList<Boolean>();
        resultAck = new ArrayList<Boolean>();
        try {
            //To connect to Spread Daemon Running on System and this connection is not
           //receiving Memebrship message due to last parameter which is set to false.
              connection.connect(InetAddress.getByName(address),port,name,false,false);             
        } 
        catch (UnknownHostException e) {
                e.printStackTrace();
        }
        catch (SpreadException e) {
		e.printStackTrace();
                System.exit(1);
	}        
         //Adding Listener for Client to listen the Reply Message from Server.
        connection.add(this);
        handler = new ClientInvocationHandler(this);
        updateView();
        getserverStub();
    }
    
    public void membershipMessageReceived(SpreadMessage msg){}
    
    /**This method is called by spread when it receive the regular message and 
     * deliver the message to GroupProxy object. Upon receiving message it checks
     * the methodSignature and according to that it takes action
     * 
     * @param msg Spread Message received from sender
     */
    public void regularMessageReceived(SpreadMessage msg){
    
       Object obj = null;
       try {
           obj = msg.getObject();  
           sender = msg.getSender().toString();
       }
       catch (SpreadException e) {
           e.printStackTrace();
           System.exit(1);
       }       
       getAck().set(((Message)obj).getMessageNumber(), new Boolean(true));
       Message replyMessage = (Message) obj;
       if(replyMessage.getMethodSignature().equals("Ack"))
           return;
       getResultAck().set(((Message)obj).getMessageNumber(), new Boolean(true)); 
       result = replyMessage.getResult();
       methodSignature = replyMessage.getMethodSignature();
       if(DEBUG) {
           System.out.println("Message number in regular: "+((Message)obj).getMessageNumber()+" from "+sender);
            System.out.println(methodSignature+ " from server "+sender+" result is "+result);
            System.out.println(result);
       }        
       if(methodSignature.equals("getView")){
           receivedUpdatedView((View)result);
       }           
       else {
           if(methodSignature.equals("getServerStub")){       
                setserverStub(result);
            }
           else handler.getResult(result);
       }    
    }
    
    /**This method sends the anycast message to receiver which is one of the
     * member of servergroup. it sets the message number for acknowledgement.
     * 
     * @param receiver 
     * @param obj
     */
    public void sendMessagetoServer(String receiver, Message obj) {
       ack.add(new Boolean(false));
       resultAck.add(new Boolean(false));
       obj.setMessageNumber(ack.size()-1);
       obj.setOriginalSender(getIdentifier());
       obj.setMessageType("Request");
       obj.setGroupName("null");
       SpreadMessage sm = new SpreadMessage();
        try {
            //Preparing Spread Message to sent to server.            
            sm.setObject(obj);
            sm.addGroup(receiver);
            sm.setReliable();
        }
        catch (SpreadException e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
            //Multicast the Message to Network as defined in spread.conf.
            connection.multicast(sm);            
            TimeOut t = new TimeOut(this,receiver, sm, false);            
            ResultTimeout tr = new ResultTimeout(this, sender, sm);
            t.start();
            tr.start();      
        }
        catch (SpreadException e) {
            e.printStackTrace();
            System.exit(1);
        }          
   }
    
   /**updateView() creates a message to send to group to get all the running servers
    * in the servergroup to get the service. 
    */
   public void updateView() {       
       String getview = "getView";
       Message viewMessage = new Message();
       viewMessage.setMethodSignature(getview);
       getUpdateView(viewMessage);          
   } 
   
   /**this method multicast the message to get the update view of all the running
    * servers at that time.
    * 
    * @param msg Message which needs to be multicasted to get the update view
    */
   public void getUpdateView(Message msg) {
        ack.add(new Boolean(false));
	resultAck.add(new Boolean(false));
        msg.setMessageNumber(ack.size()-1);
        msg.setGroupName("null");
        msg.setMessageType("Request");
        msg.setOriginalSender(getIdentifier());
        SpreadMessage sm = new SpreadMessage();
        try {
            //Preparing Spread Message to sent to server.            
            sm.setObject(msg);
            sm.addGroup(groupname);
            sm.setReliable();
        }
        catch (SpreadException e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
            /**Multicast the Message to Network as defined in spread.conf.
             * to get the update list of running servers.
             */
            connection.multicast(sm);        
            TimeOut t = new TimeOut(this,groupname, sm, true);
            ResultTimeout tr = new ResultTimeout(this, sender, sm);
            t.start();
            tr.start();         
        }
        catch (SpreadException e) {
            e.printStackTrace();
            System.exit(1);
        }        
   }
   /**This method is called by regularMessageReceived() when it receive the message
    * with signature "GetView" which contains the view of all running members at
    * that instant of time.
    * 
    * @param view of all the running members at that time.
    */
   public void receivedUpdatedView (View view) {
        this.view = view;
        if(DEBUG) {
            System.out.println("View is"+view.toString()+"size is"+view.getView().size());
        }
        synchronized(this) {
            waitingView = false;
            this.notifyAll();
        }  
        if(recovery) {
            sender = getServerAddress();
            if(message !=null ){
                sendMessagetoServer(sender, (Message)message);
            }
            recovery = false;
        }
   }
   /**This method generate the random server to be choosen to request for service
    * out of the list GroupProxy Object have of running members.
    * 
    * @return the address of server which is choosed to request for service.
    */ 
   public String getServerAddress() {
       int size = getView().size();
       int pos = generator.nextInt();
       int mod = size+1;
       pos = pos%mod;
       pos = Math.abs(pos);
       if(pos>size-1) {
           pos = 0;
       }              
       try {
            server = getView().elementAt(pos);
       }
       catch (ArrayIndexOutOfBoundsException e){
           e.printStackTrace();
       }         
        return server;
    }
    /**This method used to get the Vector of group members out of View Object which 
     * is received.
     * 
     * @return Vector of Running members.
     */ 
    public Vector<String> getView() {
       grouptimeout = false;
       synchronized(this) {
           while(waitingView) {
               try {
                   this.wait();                   
               }
               catch(InterruptedException e) {
                   e.printStackTrace();
               }
           }
           if(grouptimeout) {
               System.out.println("Group Time Out in getting View");
               System.exit(1);
           }          
           return view.getView();
       }
   }
     
   /**This method create new message to request for server stub to get the service
    * from it. 
    */
   public void getserverStub(){
       methodSignature = "getServerStub";
       Message getProxy = new Message();
       getProxy.setMethodSignature(methodSignature);
       String destServer = getServerAddress();
       sendMessagetoServer(destServer, getProxy);
   }
   /**This method is called by regularMessageReceived() when it receive the message
    * with signature "getServerStub" which contains the class name array of
    * interfaces which implement the ExternalGMIListener interface. it creates the
    * server stub based on the class names it received from server. 
    * 
    * @param proxy
    */
   public void setserverStub(Object proxy){       
       String[] name = (String[]) proxy;
       serverStub = new Class[name.length];
       try {
           for(int i=0; i<name.length;i++){
               serverStub[i] = Class.forName(name[i]);
           }
           
       }
       catch(ClassNotFoundException e){
           e.printStackTrace();
       }       
       ClassLoader cl = serverStub[0].getClassLoader();
       external = (ExternalGMIListener) Proxy.newProxyInstance(cl, serverStub, handler);              
        synchronized(this) {
            waitingProxy = false;
            this.notifyAll();
        } 
       if(DEBUG) {
           System.out.println("Proxy created: ");
       }
   }  
   /**getServer() is called by client application to get the server stub to get the
    * service required by client application. 
    * 
    * @return server stub Object
    */
   public Object getServer(){
       synchronized(this) {
           while(waitingProxy) {
               try {
                   this.wait();                   
               }
               catch(InterruptedException e) {
                   e.printStackTrace();
               }               
           }               
           return external;
       }
       
   }
   /**This is called by TimeOut Object when it doesn't receive the acknowledgement
    * for the message sent with in the specified time limit which is set to 1 sec.
    * it removes the particular server on which it has time out and try tp send the
    * message again to next server from the list and if list is empty it try to get new
    * list of all the running servers. 
    * 
    * @param sender for whom time out occurs.
    * @param obj message which is sent to the server.
    */
   public void timeOut(String sender, Object obj) {
       view.removeServer(sender);
       int size= getView().size();
       if(DEBUG) {
           System.out.println("Size is:: "+size);
       }
       resultAck.set(resultAck.size()-1, new Boolean(true));
       if (size>0) {       
          sender = getServerAddress();
          sendMessagetoServer(sender, (Message) obj);   
       }    
       else {
           message = obj;
           recovery = true;
           updateView();
       }
   }
   /***This is called by TimeOut Object when it doesn't receive the acknowledgement
    * for the message sent with in the specified time limit which is set to 1 sec.
    * it try to get the updateView again and if it didn't receive the acknowledgment
    * again then it prints message and exit.
    */
   public void groupTimeOut() {
    
        grouptimeout = true;
	resultAck.set(resultAck.size()-1, new Boolean(true));
        synchronized(this){
            this.notifyAll();
        }
        recovery = true;
        waitingView = true;
        updateView();
         try {
             Thread.sleep(2000);
         }
         catch (InterruptedException e) {
             e.printStackTrace();
         }
         if(grouptimeout) {
         System.out.println("All Servers are Down.");
         System.exit(1);   
         }
   }    
   public void resultTimeout(Object obj){
       int size= getView().size();       
       if (size>0) {       
          sender = getServerAddress();
          sendMessagetoServer(sender, (Message) obj);   
       }    
       else {
           message = obj;
           recovery = true;
           updateView();
       }
   }
   public ArrayList<Boolean> getResultAck() {
       return resultAck;
   }
    
   public ArrayList<Boolean> getAck() {
       return ack;
   }

   public String getIdentifier() {
       return connection.getPrivateGroup().toString();
   }

}
