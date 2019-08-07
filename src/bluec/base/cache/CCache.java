package bluec.base.cache;
  
public class CCache {
  static CTree  mData = new CTree("","","");
  static CTree  mDelta = new CTree("","","");
  static CTree  mMeta = new CTree("","","");
  
  static CSqlTree sql = new CSqlTree("","",0,"",null,null,null);
			
  static public void cacheMData(String vKey,String vcKey,String vValue){
  	mData.insertNode(vKey,vcKey,vValue);
  }
  static public CChildTree getMData(String vKey,String vcKey){
  	return mData.findNode(vKey,vcKey);
  }
  static public void cacheMDelta(String vKey,String vcKey,String vValue){
  	mDelta.insertNode(vKey,vcKey,vValue);
  }
  static public CChildTree getMDelta(String vKey,String vcKey){
  	return mDelta.findNode(vKey,vcKey);
  }  
  static public void cacheMMeta(String vKey,String vcKey,String vValue){
  	mMeta.insertNode(vKey,vcKey,vValue);
  }
  static public CChildTree getMMeta(String vKey,String vcKey){
  	return mMeta.findNode(vKey,vcKey);
  }
  
  static public void cacheSql(String vsqlid,String vsqls,int vsqlType,String vprocName,
  		String[] vparamType,String[] vparamSVar,String[] vparamSMark){
  	sql.insertNode(vsqlid,vsqls,vsqlType,vprocName,vparamType,vparamSVar,vparamSMark);
  }
  
  static public CSqlTree getSql(String vsqlid){
  	return sql.findNode(vsqlid);
  }

  static public void setSqlInValid(String vsqlid){
  	sql.setNodeInValid(vsqlid);
  }

  static public void setMDataInValid(String vKey){
  	mData.setNodeInValid(vKey);
  }

  static public void setMDeltaInValid(String vKey){
  	mDelta.setNodeInValid(vKey);
  }

  static public void setMMetaInValid(String vKey){
  	mMeta.setNodeInValid(vKey);
  }  
  
  static public void setDataInValid(String vKey){
  	mData.setNodeInValid(vKey);
  	mDelta.setNodeInValid(vKey);
  	mMeta.setNodeInValid(vKey);
  }	
}
