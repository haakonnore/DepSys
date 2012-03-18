
package gmi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * 
 * @author Gurvinder Singh
 */

public class ClientInvocationHandler implements InvocationHandler{
    
    private GroupProxy groupproxy;
    private boolean waitingResult = true;
    private Object result;
    private Object serverStub;
    private HashMap<Integer, String> egmitable;
    private boolean firsttime = true;
    private final boolean DEBUG = false;
    
    /**
     * Client side invocation hadler to handle the method invocation
     * on server from client side.
     * @param proxy
     */
    public ClientInvocationHandler(GroupProxy proxy)  {
        groupproxy = proxy;       
        egmitable = new HashMap<Integer, String>();       
    }
    
    /**
     * Invoke method is called by java reflection every time when any method
     * is called on methos object. in this we wait untill we get reply from 
     * server and send received reply back to client application.
     * @param proxy
     * @param m Method which is to be invoked on server
     * @param args Arguments passed for method 
     * @return result from server after method invocation
     * @throws java.lang.Throwable throws the exception thrwon due to method execution
     * returned by server to client
     */
    
    public Object invoke(Object proxy, Method m, Object[] args)
           throws Throwable {    
    String signature = getNameAndDescriptor(m);    
    if(firsttime){       
        serverStub = groupproxy.getServer();        
        methodTable();
        firsttime = false;
    }
    if(egmitable.containsKey(signature.hashCode())){
        Message message = new Message();
        message.setMethodSignature(signature);       
        message.setArguments(args);     
        String server = groupproxy.getServerAddress();
        groupproxy.sendMessagetoServer(server, message);
        if(DEBUG) {
            System.out.println("Sending message: "+message.getMethodSignature()+" arguments: "+args[0]);
        }
        synchronized(this) {
               while(waitingResult) {
                   try {
                       this.wait();                       
                    }
                    catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                }
               waitingResult = true;
        }
    } else {
         throw new Error("Method \""+m.getName()+"\" is not defined.");                  
    }
    if(DEBUG) {
        System.out.println("result is: "+result);
    }
    if(result instanceof Exception){
        throw (Throwable) result;       
    }
    return result;
    }
    public void getResult(Object result) {
        this.result = result;
        synchronized(this){
            waitingResult = false;
            notifyAll();
        }
    }
    
    /**
     * This method is used to make the method table of declared methods in 
     * interface extending ExternalGMIListener, so that if client try to invoke 
     * undefined method then we raise error message saying method is not defined.
     * 
     */
    
    public void methodTable(){
        Class[] interfaces = serverStub.getClass().getInterfaces();
        for(int i=0; i<interfaces.length;i++){
                Method methods[] = interfaces[i].getMethods();
                for(int j=0; j<methods.length; j++) {                    
                   String name = getNameAndDescriptor(methods[j]);
                   int key = name.hashCode();
                   //System.out.println("Name: "+name+" key: "+key+ " method: "+methods[j]);
                   egmitable.put(key,name);                                      
                 }                                           
            }
    }
    
    /** This method return the method signature as string
     * which is stored in table.
     * @param method which is need to be stored in table
     * @return signature of message to be stored in table
     */
    
    public static String getNameAndDescriptor(Method method) {
    StringBuilder desc = new StringBuilder();  
    Class returnType = method.getReturnType();
    if (returnType == void.class) { 
        desc.append("void");
    } else {
      desc.append(getTypeDescriptor(returnType));     
    }
    desc.append(method.getName());
    Class[] paramTypes = method.getParameterTypes(); ;
    if(paramTypes.length==0) {
        desc.append("void");
    }
    for (int i = 0; i < paramTypes.length; i++) {
      desc.append(getTypeDescriptor(paramTypes[i]));
      
    }        
    return desc.toString();
  }

  /**
   * Get the descriptor of a particular type, as appropriate for either
   * a parameter or return type in a method descriptor.
   */
    
  public static String getTypeDescriptor(Class type) {
    if (type.isPrimitive()) {
      if (type == int.class) {
        return "int";
      } else if (type == boolean.class) {
        return "boolean";
      } else if (type == byte.class) {
        return "byte";
      } else if (type == char.class) {
        return "char";
      } else if (type == short.class) {
        return "short";
      } else if (type == long.class) {
        return "long";
      } else if (type == float.class) {
        return "float";
      } else if (type == double.class) {
        return "double";
      } else if (type == void.class) {
        return "void";
      } else {
        throw new Error("unrecognized primitive type: " + type);
      }
    } else if (type.isArray()) {      
      return "array";
    } else {
      return  type.getSimpleName();
    }
  }

}
