import java.util.ArrayList;
import java.util.HashMap;

/**
 * The Function class represents a function, its parameters and properties.
 * It serves the purpose of identifying a function's name, parameters, 
 * return name and type, local declarations and status. 
 */
public class Function {
	
	/** Function name */
	public String name;
	
	/** Function status to avoid redeclaration */
	public boolean declared;
	
	/** List of parameter names */
	public ArrayList<String> parameters;
	
	/** List of parameter types */
	public ArrayList<String> paramtype;
	
	/** Map of local declarations */
	public HashMap<String, Declaration> localDeclarations;
	
	/** Function Return */
	public Declaration ret;
	
	/** Constructor
	 * 
	 */
	public Function(){
		name="";
		declared=false;
		parameters = new ArrayList<String>();
		paramtype = new ArrayList<String>();
		localDeclarations = new HashMap<String, Declaration>();
	};
	
	/** Constructor (String n, String r, String rn)
	 * 
	 * @param n			Function Name
	 * @param r			Return Name
	 * @param rn			Return Type
	 */
	public Function(String n, String r, String rn){
		name=n;
		ret=new Declaration(rn,r);
		declared=false;
		parameters = new ArrayList<String>();
		paramtype = new ArrayList<String>();
		localDeclarations = new HashMap<String, Declaration>();
	};

	/**
	 * Returns a string representation of the object. 
	 * The toString method for class Function returns a string 
	 * consisting of the function name and return existence.
	 */
	public String toString(){
		if (ret.type.compareTo("void")==0) {
			return "Funcao " + name;
		}
		return "Funcao " + name + " com retorno";
		//return "Funcao " + name + " com retorno " + ret;
	}
	
	
	//Unused
	/*public Function(String n){
		name=n;
		ret=null;
		declared=false;
		//args = new HashMap<String,String>();
		parameters = new ArrayList<String>();
		paramtype = new ArrayList<String>();
		localDeclarations = new HashMap<String, Declaration>();
	};*/
	
	/*public Function(String n, String r){
		name=n;
		
		ret=r;
		retname="";
		declared=false;
		//args = new HashMap<String,String>();
		parameters = new ArrayList<String>();
		paramtype = new ArrayList<String>();
		localDeclarations = new HashMap<String, Declaration>();
	};*/
	
	//public Function(String n, String r, HashMap<String,String> param){
	/*public Function(String n, String r, ArrayList<String> param, ArrayList<String> paramt){
		name=n;
		ret=r;
		declared=false;
		//args = param;
		parameters = param;
		paramtype = paramt;
		localDeclarations = new HashMap<String, Declaration>();
	};*/
}
