package bluec.base.cache;
  
public class CSqlTree {
	String sqlid;
	String sqls;
	int sqlType;
	String procName;
	String[] paramType;
	String[] paramSVar;
	String[] paramSMark;
  
	CSqlTree leftTree;
	CSqlTree rightTree;
	int leftCount;
	int rightCount;
	boolean isValid;
	
	CSqlTree(String vsqlid,String vsqls,int vsqlType,String vprocName,
			String[] vparamType,String[] vparamSVar,String[] vparamSMark){
		sqlid = vsqlid;
		sqls = vsqls;
		sqlType = vsqlType;
		procName = vprocName;
		paramType = vparamType;
		paramSVar = vparamSVar;
		paramSMark = vparamSMark;
		leftTree = null;
		rightTree = null;
		leftCount = 0;
		rightCount = 0;
		isValid = true;
	}

  public void setInValid(){
  	isValid = false;
  }
  
  public void insertNode(String vsqlid,String vsqls,int vsqlType,String vprocName,
  		String[] vparamType,String[] vparamSVar,String[] vparamSMark){
	  if(sqlid.equals("")){
			sqlid = vsqlid;
			sqls = vsqls;
			sqlType = vsqlType;
			procName = vprocName;
			paramType = vparamType;
			paramSVar = vparamSVar;
			paramSMark = vparamSMark;
			isValid = true;
	  }else{
		  if(sqlid.compareTo(vsqlid)>0){
		  	if(leftTree==null)
			    leftTree = new CSqlTree(vsqlid,vsqls,vsqlType,vprocName,vparamType,vparamSVar,vparamSMark);
		  	else
		  		leftTree.insertNode(vsqlid,vsqls,vsqlType,vprocName,vparamType,vparamSVar,vparamSMark);
		  }else{
		  	if(rightTree==null)
			    rightTree = new CSqlTree(vsqlid,vsqls,vsqlType,vprocName,vparamType,vparamSVar,vparamSMark);
		  	else
		  		rightTree.insertNode(vsqlid,vsqls,vsqlType,vprocName,vparamType,vparamSVar,vparamSMark);
		  }  
	  }
  }	
  
  public CSqlTree findNode(String vsqlid){
  	CSqlTree result = null;
	  int k = sqlid.compareTo(vsqlid);
	  if(k==0)
	  	result = (this.isValid)?this:null;
	  else if(k>0){
	  	if(leftTree!=null)
		    result = leftTree.findNode(vsqlid);
	  }else{
	  	if(rightTree!=null)
		    result = rightTree.findNode(vsqlid);
	  }  
	  return result;
  }

  public void setNodeInValid(String vsqlid){
  	CSqlTree node = findNode(vsqlid);
  	if(node!=null)
  		node.setInValid();
  }
  
	public String[] getParamSMark() {
		return paramSMark;
	}

	public String[] getParamSVar() {
		return paramSVar;
	}

	public String[] getParamType() {
		return paramType;
	}

	public String getProcName() {
		return procName;
	}

	public String getSqlid() {
		return sqlid;
	}

	public String getSqls() {
		return sqls;
	}

	public int getSqlType() {
		return sqlType;
	}  
}
