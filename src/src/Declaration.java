/**
 * The Declaration class represents a variable and its properties.
 * It serves the purpose of identifying a variable name, type and
 * stack location. Is also stores if the variable as been initialized.
 */
public class Declaration {
	
	/** Name of the variable */
	public String name;
	
	/** Type of the variable */
	public String type;
	
	/** Status of the variable (if it's initialized or not) */
	public boolean init;
	
	/** Integer value of the variable in case it's unique */
	public int value;
	
	/** Array of values of the variable in case it's an array */
	public int values[];
	
	/** Size of the array */
	public int size;
	
	/** Stack position on Code Generation */
	public int local;
	
	/** Constructor
	 * 
	 */
	public Declaration(){
		init = false;
		local = -1;
	}
	
	/** Constructor (String n)
	 * 
	 * @param n			Name
	 */
	public Declaration(String n){
		name = n;
		init = false;
		local = -1;
	}
	
	/** Constructor (String n, String t)
	 * 
	 * @param n			Name
	 * @param t			Type
	 */
	public Declaration(String n, String t){
		name = n;
		type = t;
		init = false;
		local = -1;
	}
	
	/** Constructor (String t, int l)
	 * 
	 * @param t			Type
	 * @param l			Local
	 */
	public Declaration(String t, int l){
		name = "";
		type = t;
		init = false;
		local = l;
	}
	
	/** Initializes the array with the specified size.
	 * @param s			the size of the array
	 */
	public void initArray (int s){
		if(!init)
		{
			init = true;
			size = s;
			values = new int[s];
			type = "array";
		}
	}
	
	/** Initializes the variable with the specified value
	 * @param val		value of the variable
	 */
	public void initInt (int val){
		if(!init)
		{
			init = true;
			value = val;
			type = "inteiro";
		}
	}
	
	/**
	 * Returns a string representation of the object. 
	 * The toString method for class Declaration returns a string 
	 * consisting of the name of the variable, type and status.
	 */
	public String toString(){
		String str="";

		if(!init)
			str= "Variavel " + name + " declarada";
		else
		if(type.compareTo("inteiro") == 0 && init)
				str=name + " inicializado com o valor " + value;
		else if (type.compareTo("array") == 0 && init)
				str=name + " inicializado com tamanho " + size;
		
		return str;
	}
}
