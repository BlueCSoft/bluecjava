package bluec.base.cache;
  
public class CChildTree{
  String  ckeyName;
  String  ckeyValue;
  boolean isValid;
  CChildTree cleftTree;
  CChildTree crightTree;
 
  CChildTree(String vkeyName,String vkeyValue){
	  ckeyName = vkeyName;
	  ckeyValue = vkeyValue;
	  isValid = true;
	  cleftTree = null;
	  crightTree = null;
  }
  public String getValue(){
  	return ckeyValue;
  }
  
  public void setInValid(){
  	isValid = false;
  }
  
  public void setAllInValid(){
  	setInValid();
  	if(cleftTree==null)
  		cleftTree.setAllInValid();
  	else
  		crightTree.setAllInValid();
  }
  
  public void insertNode(String vkeyName,String vkeyValue){
	  if(ckeyName.equals("")){
		  ckeyName = vkeyName;
		  ckeyValue = vkeyValue;
		  isValid = true;
	  }else{
		  if(ckeyName.compareTo(vkeyName)>0){
		  	if(cleftTree==null)
			    cleftTree = new CChildTree(vkeyName,vkeyValue);
		  	else
		  		cleftTree.insertNode(vkeyName,vkeyValue);
		  }else{
		  	if(crightTree==null)
			    crightTree = new CChildTree(vkeyName,vkeyValue);
		  	else
		  		crightTree.insertNode(vkeyName,vkeyValue);
		  }  
	  }
  }
 
  public CChildTree findNode(String vkeyName){
	  CChildTree result = null;
	  int k = ckeyName.compareTo(vkeyName);
	  if(k==0){
	 	  result = (this.isValid)?this:null;
	  }else if(k>0){
	  	if(cleftTree!=null)
		    result = cleftTree.findNode(vkeyName);
	  }else{
	  	if(crightTree!=null)
		    result = crightTree.findNode(vkeyName);
	  }  
	  return result;
  }
}
