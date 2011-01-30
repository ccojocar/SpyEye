package libsvm;

public class svm_problem implements java.io.Serializable
{
	/* for serialization */
	private static final long serialVersionUID = -4933331874038702283L;
	
	public int l;
	public double[] y;
	public svm_node[][] x;
} 