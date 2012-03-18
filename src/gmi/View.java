
package gmi;

import java.io.Serializable;
import java.util.Collections;
import java.util.Vector;

/**View Object maintains the view of all the running members of the group
 * this is used by Membershipservice object to maintain the updated view of members.
 * 
 * @author Gurvinder Singh
 */

public class View implements Serializable{
    
    private Vector<String> sView = new Vector<String>();   

    public View() { }
    public Vector<String> getView() {
        return sView;
    }
    public void setView(Vector<String> sView) {        
        this.sView = sView;
    }
    public void addSever(String name) {
        if(sView.contains(name)){
            return;
        }
        else{
            sView.add(name);
            Collections.sort(sView);            
        }
    }    
        
    public void removeServer(String name) {
        if(name.equals("all")){
            sView.removeAllElements();
        }
        else{
            sView.remove(name);
        }
    }       
    public boolean memberHasPosition(int pos, String server){        
        return server.equals(getView().elementAt(pos));
    }
    @Override
    public String toString(){
		String vi = " ";
		for(int i=0; i<sView.size();i++){
			vi+=sView.elementAt(i) + " ";
                }
			vi+=" ";
		
		return vi;
    }
    public int getSize(){
    	return sView.size();
    }
}
