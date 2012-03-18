/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gmi;


import java.lang.reflect.InvocationTargetException;
import spread.SpreadMessage;
import spread.MembershipInfo;
import spread.SpreadGroup;
import java.lang.reflect.Method;
import java.util.ArrayList;

/** MembershipService Object is handling the membership changes in the server
 * group. when there is any Membership change in group then Spread Daemon deleivered
 * membership change message to this Object and according to cause of message 
 * triggering object take the action and invoke all the function declared in
 * interface MembershipListener.
 * 
 * @author Gurvinder Singh
 */

public class MembershipService extends Thread{

    private ServerSideProxy proxy;
    private Method[] membershipChangeMethod;
    private View view;
    private SpreadMessage msg;
    private Object server, result;
    private Object args[];    
    private long startTime;
    private boolean firstTime = true;
    private final boolean DEBUG = false;
    
    public MembershipService(ServerSideProxy proxy, Object server) {
        this.proxy = proxy;
        this.server = server;       
        membershipChangeMethod = proxy.getMethodTable("Membership");         
        view = new View();
        startTime = System.currentTimeMillis();        
    }
    
    /**
     * Memebership Change message is set in this object by serversideproxy object.
     * @param msg Membership change message
     */
    public void getMessage(SpreadMessage msg){
        this.msg = msg;
    }
    
    /**serversideproxy create new thread on receiving membership message and  
     * run() method is executed by newly created thread according to cause of triggering 
     * of message it maintains the view of all the running members of the gorup
     * and invoke Membershipchange method declared in MembershipListener Interface.
     */
    @Override
    public void run(){    
        
        try{
            MembershipInfo info = msg.getMembershipInfo();
            SpreadGroup members[] = info.getMembers();  
            MembershipInfo.VirtualSynchronySet virtual_synchrony_sets[] = info.getVirtualSynchronySets();
            MembershipInfo.VirtualSynchronySet my_virtual_synchrony_set = info.getMyVirtualSynchronySet();	        
            System.out.println("\nMemebership Change for group \"" + info.getGroup() +"\" with " + members.length + " members:");
            for( int i = 0; i < members.length; ++i ) {
                System.out.println("\t\t" + members[i]);
            }
            System.out.println("Group ID is " + info.getGroupID());                        
            System.out.print("\tDue to ");
            if(info.isCausedByJoin()) {
                System.out.println("the JOIN of " + info.getJoined()+"\n");
                if(DEBUG) {
                    System.out.println("Leader st notifying: "+proxy.getLeader());
                }
                proxy.notifyMerging("Join");
                for(int i=0 ;i < members.length ; i++) {
                    view.addSever(members[i].toString());                                     
                }            
            }
            if(info.isCausedByDisconnect() || info.isCausedByLeave()) {
                System.out.println("the DISCONNECT of " + info.getDisconnected()+"\n");
                view.removeServer(info.getDisconnected().toString());                                                                
            }   
            if(info.isCausedByNetwork()) {
                System.out.println("NETWORK change");
                for( int i = 0 ; i < virtual_synchrony_sets.length ; ++i ) {
                    MembershipInfo.VirtualSynchronySet set = virtual_synchrony_sets[i];
                    SpreadGroup setMembers[] = set.getMembers();
                    System.out.print("\t\t");
                    if( set == my_virtual_synchrony_set ) {
                        System.out.print( "(LOCAL) " );
                        if(DEBUG) {
                            System.out.println("Leader st notifying due to network: "+proxy.getLeader());
                        }
                        proxy.notifyMerging("Network");
                        if(getCurrentView().getView().size() > set.getSize()&&i<1){			
                            view.removeServer("all");
                            for( int j = 0; j < set.getSize(); ++j ) {
                                view.addSever(setMembers[j].toString());
                            }
                        }                    
                    } else {
                            for( int j = 0; j < set.getSize(); ++j ) {
                                view.addSever(setMembers[j].toString());
                            } 
                            System.out.print( "(OTHER) " );
                    }   
                    System.out.println( "Virtual Synchrony Set "+i+" has "+set.getSize()+" members:");
                    for( int j = 0; j < set.getSize(); ++j ) {
                        System.out.println("\t\t\t" + setMembers[j]);
                    }
                }
            }
            if(!(firstTime && proxy.getMethodTable("Merging").length>0) || getCurrentView().getView().size()==1) {
                invokeMethod();
            } 
            if(firstTime) {
                proxy.setLeader(proxy.getIdentifier());
                firstTime = false;
            }
            if(view.getView().size()==1){
                proxy.setLeader(proxy.getIdentifier());                                         
            }
            else{
                if(System.currentTimeMillis()-startTime>1000){
                    proxy.setLeader(view.getView().elementAt(0));
                }
            }         
            if(DEBUG) {
                System.out.println("Leader is: "+proxy.getLeader());
            }
        }
        catch(NullPointerException e){
		return;
	}	
    }
   /**
    * Returns the current view object of running memebers
    * @return Current View Object
    */
    public View getCurrentView() {
        return view;
    }
    
    /**
     * Set the argument to pass in the dynamic method invocation call, because
     * we need arguments as array to pass in dynamic call, so we convert it to array.
     * @param arg Object which is to be passed as arguments.
     */
    public void setArgument(Object arg){
        ArrayList<Object> argv = new ArrayList<Object>();
        argv.add(arg);
        args = argv.toArray();
    }
    public void setView(View view) {
        this.view = view;
    }
    
    /**Method which actually invoke the methods declared in MembershipListener 
     * Interface. 
     */
    
    public void invokeMethod(){
        setArgument(getCurrentView());       
       for(Method m : membershipChangeMethod){
          try {
              result = m.invoke(server, args);
          }
          catch(InvocationTargetException e){
              result =e;
          }
          catch(Exception e){
              e.printStackTrace();
          }
       }
    }     
}
