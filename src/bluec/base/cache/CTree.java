package bluec.base.cache;

public class CTree {
  String keyName;   //节点标识
  CChildTree childTree;  //节点值
  CTree leftTree;
  CTree rightTree;  
  
  CTree(String vkeyName,String vcKeyName,String vkeyValue){
  	keyName = vkeyName;
  	childTree = new CChildTree(vcKeyName,vkeyValue);
  	leftTree = null;
  	rightTree = null;
  }

  public void setChildInValid(){
  	childTree.setAllInValid();
  }
  
  public void insertNode(String vkeyName,String vcKeyName,String vkeyValue){
  	if(keyName.equals("")){
  		keyName = vkeyName;
  		childTree.insertNode(vcKeyName,vkeyValue);
  	}else{
  		if(keyName.compareTo(vkeyName)>0){
  			if(leftTree==null)
  			  leftTree = new CTree(vkeyName,vcKeyName,vkeyValue);
  			else
  				leftTree.insertNode(vkeyName,vcKeyName,vkeyValue);
  		}else{
  			if(rightTree==null)
  			  rightTree = new CTree(vkeyName,vcKeyName,vkeyValue);
  			else
  				rightTree.insertNode(vkeyName,vcKeyName,vkeyValue);
  		}	
  	}
  }
  
  public CTree findNode(String vkeyName){
  	CTree result = null;
	  int k = keyName.compareTo(vkeyName);
	  if(k==0)
	 	  result = this;
	  else if(k>0){
	  	if(leftTree!=null)
	  		result = leftTree.findNode(vkeyName);
	  }else{
	  	if(rightTree!=null)
		  result = rightTree.findNode(vkeyName);
	  }  
	  return result;  	
  }
  
  public void setNodeInValid(String vkeyName){
  	CTree node = findNode(vkeyName);
  	if(node!=null)
  		node.setChildInValid();
  }
  
  public CChildTree findNode(String vkeyName,String vcKeyName){
  	CTree tree = null;
  	CChildTree ctree = null;
  	tree = findNode(vkeyName);
  	if(tree!=null)
  		ctree = tree.childTree.findNode(vcKeyName);
  	return ctree;
  }
  
}
