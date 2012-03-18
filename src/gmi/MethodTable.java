
package gmi;

import java.lang.reflect.Method;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Iterator;

/**MethodTable object maintains all the method define in server application based on
 * particular interface implmented by application, and provide these mehtods to 
 * ServerSideProxy to invoke method on request from requestor.
 * 
 * @author Gurvinder Singh
 */

public class MethodTable {
    
    private HashMap<Integer, Method> igmitable;
    private HashMap<Integer, Method> egmitable;
    private HashMap<Integer, Method> membershiptable;
    private HashMap<Integer, Method> mergingtable;
    private HashMap<Integer, String> table;
    private HashMap<Integer, String> annotation;
    Class server;

    public MethodTable() {
        
       igmitable = new HashMap<Integer, Method>();
       egmitable = new HashMap<Integer, Method>();
       membershiptable = new HashMap<Integer, Method>();
       table = new HashMap<Integer, String>();
       mergingtable = new HashMap<Integer, Method>();
       annotation = new HashMap<Integer, String>();
       
       
    }
    
    public void addMethod(Class server) {
        this.server = server;       
        
        /** Getting Annotation defined on methods defined in server and stroing them in
         * annotation() hashmap.
         */
        Method[] serverMethods = server.getDeclaredMethods();
        for(Method m : serverMethods) {
            String name = getNameAndDescriptor(m);
            int key = name.hashCode();
             Annotation[] annos = m.getAnnotations();
                   if(annos.length>0) {
                      for(int k=0;k<annos.length; k++) {
                          String ann  = annos[k].toString();
                           if(ann !=null) {
                                int pos = ann.indexOf(".",13);           
                                ann = ann.substring(pos+1, ann.length()-2);        
                            }
                           annotation.put(key,ann);                           
                      }
                 }
        }
         
        Class[] interfaces = server.getInterfaces();
        for( int i=0; i<interfaces.length; i++) {
         /** Getting Method defined in server which are declared as internal methods to server
          *and stroing them in igmitable() hashmap.
          */
            if(InternalGMIListener.class.isAssignableFrom(interfaces[i])) {
                Method methods[] = interfaces[i].getMethods();
                for(int j=0; j<methods.length; j++) {
                    Class retType = methods[j].getReturnType();
                    if (!(retType.toString().equals("void") || retType.equals(Object.class))) {
                    throw new IllegalArgumentException(
                        "Return type for methods in the internal interfaces must be Object or void:\n"
                         + methods[j]);                   
                   }
                   String name = getNameAndDescriptor(methods[j]);
                   int key = name.hashCode();                   
                   igmitable.put(key,methods[j]);
                   table.put(key,"IGMI");
                }
            }
         /** Getting Method defined in server which are declared as External methods to server
          *  which client can call to get service and stroing them in egmitable() hashmap.
          */
            if(ExternalGMIListener.class.isAssignableFrom(interfaces[i])) {
                Method methods[] = interfaces[i].getMethods();
                for(int j=0; j<methods.length; j++) {                    
                   String name = getNameAndDescriptor(methods[j]);
                   int key = name.hashCode();                   
                   egmitable.put(key,methods[j]);
                   table.put(key,"EGMI");                   
                 }                       
              }                          
         /** Getting Method defined in server which are declared as Membership methods called when 
          * there is a memebership change in server group and stroing them memebershiptable() hashmap.
          */
            if(MembershipListener.class.isAssignableFrom(interfaces[i])) {
                Method methods[] = interfaces[i].getMethods();
                for(int j=0; j<methods.length; j++) {                    
                   String name = getNameAndDescriptor(methods[j]);
                   int key = name.hashCode();                  
                   membershiptable.put(key,methods[j]);
                   table.put(key,"Membership");
                }
            }
            if(MergingListener.class.isAssignableFrom(interfaces[i])) {
                Method methods[] = interfaces[i].getMethods();
                for(int j=0; j<methods.length; j++) {                    
                   String name = getNameAndDescriptor(methods[j]);
                   int key = name.hashCode();                  
                   mergingtable.put(key,methods[j]);
                   table.put(key,"Merging");                   
                 }                       
              } 
        }
    }
    
  /**Return the requested method  
   * 
   * @param name Method Signature of requested method
   * @return Methos which is requested
   */
  public Method getMethod(String name)
  {
    int key = name.hashCode();    
    Object method = "null";
    String methodtype = (String) table.get(key);    
    if(methodtype.equals("IGMI")) {
        method = igmitable.get(key);
    }
    if(methodtype.equals("EGMI")) {        
        method = egmitable.get(key);         
    }
    if(methodtype.equals("Membership")) {
        method = membershiptable.get(key);
    }
    if(methodtype.equals("Merging")) {
        method = mergingtable.get(key);
    }    
    if(method.equals("null")) {
        System.out.println("Method not found");
    }
    return (Method) method;
  }
  
  /**Returns the whole method table of requested type
   * 
   * @param methodtype Method Type of which table is requested
   * @return return the mathod table
   */
  
  public Method[] getMethodTable(String methodtype){
    Method[] method = new Method[1];    
    int i=0;
    if(methodtype.equals("IGMI")) {
        Iterator valueIterator = igmitable.values().iterator();
        method = new Method[igmitable.size()];
        while (valueIterator.hasNext()) {
        Method temp = (Method) valueIterator.next();
        method[i]=temp;
        i++;
        }
    }
    if(methodtype.equals("EGMI")) {        
        Iterator valueIterator = egmitable.values().iterator();
        method = new Method[egmitable.size()];
        while (valueIterator.hasNext()) {
        Method temp = (Method) valueIterator.next();
        method[i]=temp;
        i++;
        }  
    }
    if(methodtype.equals("Membership")) {
        Iterator valueIterator = membershiptable.values().iterator();
        method = new Method[membershiptable.size()];
        while (valueIterator.hasNext()) {
        Method temp = (Method) valueIterator.next();
        method[i]=temp;
        i++;
        }
    }
    if(methodtype.equals("Merging")) {
        Iterator valueIterator = mergingtable.values().iterator();
        method = new Method[mergingtable.size()];
        while (valueIterator.hasNext()) {
        Method temp = (Method) valueIterator.next();
        method[i]=temp;
        i++;
        }
    }    
    return  method;
      
  }
  
  public String getMethodType(String name) {
      int key = name.hashCode();
      return (String) table.get(key);
  }
  
  public String getAnnotation(String name) {
      int key = name.hashCode();      
      return (String) annotation.get(key);
  }
    
  public String getNameAndDescriptor(Method m) {
    StringBuilder desc = new StringBuilder();  
    Class returnType = m.getReturnType();
    if (returnType == void.class) {   
        desc.append("void");
    } else {
      desc.append(getTypeDescriptor(returnType));     
    }
    desc.append(m.getName());
    Class[] paramTypes = m.getParameterTypes();
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
  public String getTypeDescriptor(Class type) {
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