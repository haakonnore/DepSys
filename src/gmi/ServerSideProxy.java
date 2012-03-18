
package gmi;

import spread.*;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.io.Serializable;
import java.util.HashMap;

/**ServerSideProxy Object implements the AdvancedMessageListener interface provided
 * by Spread java API to receive the message, it differentiate received message depending upon
 * it's type which is either membership or regular and depending upon it's type it called 
 * corresponding method to handle it. This Object communicate with spread daemon for
 * delivering of message to target.
 * 
 * @author Gurvinder Singh
 * @see http://www.spread.org
 * Copyright (c) 1993-2006 Spread Concepts LLC.  All rights reserved.
 */


public class ServerSideProxy implements AdvancedMessageListener{
        
    private MethodTable methodtable;
    private Class serverClass;
    private Object server, result;    
    private InternalGMIListener internal;
    private ExternalGMIListener external;  
    private String servername, groupname, sender, externalIdentifier, externalGroupName;
    private String methodSignature, messageType, protocol, leader = "null";
    private Method method;
    private int messageNumber;
    private HashMap<String, Integer> externalmessageNumber;
    private Object[] args;
    private SpreadGroup group;
    private SpreadConnection connection; 
    private MembershipService membershipService;
    private ExternalGMIService externalService;
    private InternalGMIService internalService;
    private MergingService mergingService;
    private long startTime;
    private final boolean DEBUG = false;

    public ServerSideProxy(Object server, int port, String name, String address) {
    this.server = server;
    servername = name;
    serverClass = server.getClass();
    methodtable = new MethodTable();
    methodtable.addMethod(serverClass);   
    System.out.println("kom hit");
    mergingService = new MergingService(this, server);
    membershipService = new MembershipService(this, server);
    externalService = new ExternalGMIService(this, server);
    internalService = new InternalGMIService(this, server);
    createSkelton(server);
    externalmessageNumber = new HashMap<String, Integer>();
    connection = new SpreadConnection();
    group = new SpreadGroup();    
   
        try {
            /*To connect to Spread Daemon Running on System and this connection will
             *connect at port on which your daemon is running and
             *receiving Memebrship message due to last parameter which is set to true.
             */
            connection.connect(InetAddress.getByName(address), port, servername, false, true); 
            System.out.println("Server '"+servername+"' started on "+InetAddress.getLocalHost().toString());
        }
        catch (UnknownHostException e) {
            System.out.println("Exceptiom");
            e.printStackTrace();
        }
        catch (SpreadException e) {
            e.printStackTrace();
            System.exit(1);
        }    
    startTime = System.currentTimeMillis(); 
    }
    //Join the Server group.
    public void join(String groupname) {        
        this.groupname = groupname;
        try{
            group.join(connection, groupname);
            System.out.println("Server '"+servername+" joined group '"+groupname+"'.\n");
        }        
        catch(SpreadException e) {
            e.printStackTrace();
            System.exit(1);
        }
        connection.add(this);
    }
    /**this method is invoked when membership message is received and it create
     * a new thread to handle the message depending upon it's cause of triggering
     */ 
    
    public void membershipMessageReceived(SpreadMessage msg) {
    
        membershipService.getMessage(msg);
        Thread t = new Thread(membershipService);
        t.start();              
    }
    /**this method is invoked when regular message is received and depend upon
     * the methodSignature which is requested to be invoked it delivered message
     * to particular Service Object according to that.
     */ 
    
    public void regularMessageReceived(SpreadMessage msg) {
        
        Object obj = null;
        sender = msg.getSender().toString();         
        try {
            obj = msg.getObject();
        }
        catch (SpreadException e) {
            e.printStackTrace();
            System.exit(1);
        }                  
        Message message = (Message) obj;
        messageNumber = message.getMessageNumber();
        externalGroupName = message.getGroupName();
        if(!groupname.equals(externalGroupName)){                
                externalmessageNumber.put(sender, messageNumber);
                Message acknowledge = new Message();
                acknowledge.setMessageNumber((Integer)externalmessageNumber.get(sender));
                acknowledge.setMessageType("Reply");
                acknowledge.setMethodSignature("Ack");
                sendAnycast(sender,acknowledge);
                if(DEBUG) {
                    System.out.println("number is: "+externalmessageNumber.get(sender)+" sender is: "+sender);
                }                         
        }               
        methodSignature = message.getMethodSignature();  
        externalIdentifier = message.getOriginalSender();
        result = message.getResult();
        messageType = message.getMessageType();
        if(DEBUG) {
            System.out.println("Result is: "+result);
            System.out.println("Received Method Signature: "+methodSignature);
        }        
        if(methodSignature.equals("getView")){           
            externalService.invokeMethod(method, "Anycast", methodSignature, args, sender, message);            
            return;
        }
        if(methodSignature.equals("putState")){
            mergingService.putGlobalState(message.getResult(), sender, messageType);
            Thread t = new Thread(mergingService);
            t.start();
            return;
        }
        if(methodSignature.equals("getServerStub")){            
            Message replyMessage = new Message();
            Class[] externalInterface = external.getClass().getInterfaces(); 
            String name[] = new String[externalInterface.length];
            for(int i=0;i<externalInterface.length;i++){
                name[i] = externalInterface[i].getName();
                }
            replyMessage.setResult(name);
            replyMessage.setMessageType("Reply");
            replyMessage.setMethodSignature(methodSignature);                       
            sendAnycast(sender,replyMessage);
            return;
        }
        else {
            method = methodtable.getMethod(methodSignature);
            String methodType = methodtable.getMethodType(methodSignature);
            args = message.getArguments();
            protocol = methodtable.getAnnotation(methodSignature);
	    if(protocol == null){               
                protocol = "Anycast";
            }
            if(methodType.equals("IGMI")){
               if(DEBUG) {
                   System.out.println("invoking internal" + methodSignature);
               }                    
               internalService.invokeMethod(method, sender, args, result, methodSignature, messageType);
            }
            if(methodType.equals("EGMI")){  
               if(DEBUG) {
                   System.out.println("Invoking external"+ methodSignature);
               }
               externalService.invokeMethod(method, protocol, methodSignature, args, sender, message);               
            }                
        }   
    }   
    /**sendAnycast() sends the anycast message to the receiver.
     * 
     * @param receiver to whom the message is to be sent.
     * @param obj message which is to be sent
     */
    public void sendAnycast(String receiver, Message obj) {
        if(DEBUG) {
            System.out.println("Sending reply to: "+receiver+" reply is: "+obj.getResult());
        }
	if(!getCurrentView().getView().contains(receiver)) {
            try{
                obj.setMessageNumber((Integer)externalmessageNumber.get(receiver));
            }
            catch(NullPointerException e){}
        }
        obj.setGroupName(groupname);
        SpreadMessage replyMsg = new SpreadMessage();
        //Preparing Spread Message to sent to server.
        try {
            replyMsg.setObject(obj);
            replyMsg.addGroup(receiver);
            replyMsg.setReliable();            
        }
        catch (SpreadException e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
            connection.multicast(replyMsg);
        }
        catch (SpreadException e) {
            e.printStackTrace();
            System.exit(1);
        }      
    }
    /**sendMulticast() multicast the message in the groupname to whom
     * message is deleivered.
     * 
     * @param obj message to be multicasted
     */
    
    public void sendMulticast(Serializable obj) {
        ((Message)obj).setGroupName(groupname);
        SpreadMessage sm = new SpreadMessage();
        try {
            sm.setObject(obj);
            sm.addGroup(groupname);
            sm.setSafe();            
        }
        catch (SpreadException e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
            connection.multicast(sm);
        }
        catch (SpreadException e) {
            e.printStackTrace();
            System.exit(1);
        }
        //System.out.println("sent message"+obj.toString());
    }  
    
   /**createSkelton() creates the skelton for interfaces depends upon different 
    * interfaces server implements.
    * 
    * @param server of which skelton to be created
    */
   public void createSkelton(Object server) {
        
        Class[] interfaces = server.getClass().getInterfaces(); 
        Class[] internalInterface = new Class[interfaces.length];
        Class[] externalInterface = new Class[interfaces.length];
        int l=0,j=0;
        for(int i=0; i< interfaces.length; i++) {
            if(InternalGMIListener.class.isAssignableFrom(interfaces[i])) { 
                internalInterface[l] = interfaces[i];                
                l++;                              
            }
            if(ExternalGMIListener.class.isAssignableFrom(interfaces[i])) {
                externalInterface[j] = interfaces[i];                 
                j++;                
            }            
        }       
        Class[] finalInternal = new Class[l];
        for(int i =0;i<l;i++){
            finalInternal[i]=internalInterface[i];
        } 
        Class[] finalExternal = new Class[j];
        for(int i =0;i<j;i++){
            finalExternal[i]=externalInterface[i];
        } 
        ClassLoader cl = InternalGMIListener.class.getClassLoader();                
        internal = (InternalGMIListener) Proxy.newProxyInstance(cl,  finalInternal ,internalService);
        ClassLoader ecl = ExternalGMIListener.class.getClassLoader();              
        external = (ExternalGMIListener) Proxy.newProxyInstance(ecl, finalExternal ,externalService);
    }
   /**notifyMerging() invokes the getGlobalState() from the leader to synchronize the global
    * state with the newly created member or during partition merging.
    * 
    * @param cause of invoking getGlobalState().
    */
   public void notifyMerging(String cause){	
       if(getIdentifier().equals(leader) && getCurrentView().memberHasPosition(0, leader)) {
               mergingService.getGlobalState(cause);   
       }
   }
   /**synchronizedView() is called after the global state is synchronized within 
    * all the members and MergingService call this method. 
    */
   public void synchronizedView(){
       membershipService.invokeMethod();
   }
   /**getInternalStub() provides application with the stub which implement the
    * given interface as parameter.
    * 
    * @param cl interface class of which stub is required
    * @return Object which implement this functionality
    */
   public Object getInternalStub(Class cl) {        
       return InternalGMIListener.class.isAssignableFrom(cl)?internal:external;
    }
   
   public Method[] getMethodTable(String type){
       return methodtable.getMethodTable(type);
   }
   
   public String getMethodSignature(Method m){
       return methodtable.getNameAndDescriptor(m);
   }
    
   public View getCurrentView() {
       return membershipService.getCurrentView();
   }
   public String getIdentifier() {
       return connection.getPrivateGroup().toString();
   }
   public String getExternalIdentifier(){
       return externalIdentifier;
   }
   public void setLeader(String leader){
       this.leader = leader;      
   }
   public String getLeader(){
       return leader;
   }
   public long getStartTime(){
       return startTime;
   }
}
