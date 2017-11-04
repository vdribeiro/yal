/* Generated By:JJTree: Do not edit this line. SimpleNode.java Version 4.3 */
/* JavaCCOptions:MULTI=false,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class SimpleNode implements Node {

  protected Node parent;
  protected Node[] children;
  protected int id;
  protected Object value;
  protected Yal parser;
  
  protected String val;
  protected int line;
  protected boolean bypass;

  public void setBypass(boolean b) {
	  bypass=b;
  }
  
  public void setLine(int l) {
	  line = l;
  }

  public void setVal(String v) {
	  if(v.indexOf("\"") != -1)
		  val = v;
	  else
		  val = v.toLowerCase();
  }
  
  public void addVal(String v) {
	  if (val == null)
		  if(v.indexOf("\"") != -1)
			  val = v;
		  else
			  val = v.toLowerCase();
	  else
		  if(v.indexOf("\"") != -1)
			  val = val + v;
		  else
			  val = val + v.toLowerCase();
  }
  
  public SimpleNode(int i) {
    id = i;
    bypass=false;
  }

  public SimpleNode(Yal p, int i) {
    this(i);
    parser = p;
    bypass=false;
  }

  public void jjtOpen() {
  }

  public void jjtClose() {
  }

  public void jjtSetParent(Node n) { parent = n; }
  public Node jjtGetParent() { return parent; }

  public void jjtAddChild(Node n, int i) {
    if (children == null) {
      children = new Node[i + 1];
    } else if (i >= children.length) {
      Node c[] = new Node[i + 1];
      System.arraycopy(children, 0, c, 0, children.length);
      children = c;
    }
    children[i] = n;
  }

  public Node jjtGetChild(int i) {
    return children[i];
  }
  
  public String jjtGetChildVal(int i) {
	  return ((SimpleNode)children[i]).val;
  }

  public int jjtGetNumChildren() {
    return (children == null) ? 0 : children.length;
  }

  public void jjtSetValue(Object value) { this.value = value; }
  public Object jjtGetValue() { return value; }

  /* You can override these two methods in subclasses of SimpleNode to
     customize the way the node appears when the tree is dumped.  If
     your output uses more than one line you should override
     toString(String), otherwise overriding toString() is probably all
     you need to do. */

  public String toString() { return YalTreeConstants.jjtNodeName[id]; }
  public String toString(String prefix) {
	  String str = prefix + toString();
	  if (this.val!=null) {
		  str += " " + this.val;
	  }
	  return str; 
  }

  /* Override this method if you want to customize how the node dumps
     out its children. */

  public void dump(String prefix) {
	//if ((val!=null) && (val.compareTo("bypassnode")==0)) {
	if (this.bypass) {
		//nao imprime
	}
	else {
		System.out.println(toString(prefix));
	}
	
    if (children != null) {
      for (int i = 0; i < children.length; ++i) {
        SimpleNode n = (SimpleNode)children[i];
        if (n != null) {
        	if (this.bypass) {
        		n.dump(prefix);
        	}
        	else {
        		n.dump(prefix + " ");
        	}
        }
      }
    }
  }
}

/* JavaCC - OriginalChecksum=0af36a26ba5a98618dfdc8b13e1e0fbc (do not edit this line) */
