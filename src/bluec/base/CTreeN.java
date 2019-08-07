package bluec.base;

import java.util.Vector;

public class CTreeN {
	private int id = 0;
	private int pid = 0;
	private String key = "";
	private String pkey = "";
    private Boolean isFirst = true;
	public class CTreeNode {
		public int id = 0;
		public int pid = 0;
		public String key = "";
		public String pkey = "";
		public String[] values = null;
		public int level = 0;
		public Vector<CTreeNode> cnodes = null;

		CTreeNode(int id, int pid, String key, String pkey, String[] values) {
			this.id = id;
			this.pid = pid;
			this.key = key;
			this.pkey = pkey;
			this.values = values;
			cnodes = new Vector<CTreeNode>();
		}
	}

	public Vector<CTreeNode> allnodes = null;
	public Vector<CTreeNode> cnodes = null;

	public CTreeN() {
		allnodes = new Vector<CTreeNode>();
		cnodes = new Vector<CTreeNode>();
	}

	public void insertNode(int id, int pid, String key, String pkey, String[] values) {
		CTreeNode node = new CTreeNode(id, pid, key, pkey, values);
		if (this.isFirst || this.id == pid) {
			this.isFirst = false;
			this.id = pid;
			cnodes.addElement(node);
		} else {
			for (int i = 0; i < allnodes.size(); i++) {
				CTreeNode cnode = allnodes.elementAt(i);
				if (cnode.id == pid) {
					cnode.cnodes.addElement(node);
					break;
				}
			}
		}
		allnodes.addElement(node);
	}

	public void insertNode(String key, String pkey, String[] values) {
		CTreeNode node = new CTreeNode(0, 0, key, pkey, values);
		if (this.key.equals(pkey)) {
			cnodes.addElement(node);
		} else {
			for (int i = 0; i < allnodes.size(); i++) {
				CTreeNode cnode = allnodes.elementAt(i);
				if (cnode.id == pid) {
					cnode.cnodes.addElement(node);
					break;
				}
			}
		}
		allnodes.addElement(node);
	}

	public String toString() {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < allnodes.size(); i++) {
			buf.append(allnodes.elementAt(i).key).append("\n");
		}
		return buf.toString();
	}
}
