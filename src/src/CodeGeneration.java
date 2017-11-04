import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * The CodeGeneration class uses the Symbol Table 
 * to enable an AST to be turned into a .j file 
 * with java bytecodes
 */
public class CodeGeneration {

	/** Tree Root Node */
	SimpleNode Root;
	
	/** Tree Symbol Table */
	SymbolTable Table;
	
	/** File Descriptor of the .j file */
	File FileDescriptor;
	
	/** Output Stream of the .j file */
	FileOutputStream FileOut;
	
	/** Print Stream of the .j file */
	PrintStream FileStream;
	
	/** Module name */
	String Module;
	
	/** List of Declarations that need to be in the <clinit> method */
	ArrayList<Declaration> initArrays;
	
	/** Stack Limit value */
	int stacklimit;
	
	/** Stack's actual value */
	int stackval;
	
	/** Number of local variables */
	int localsval;
	
	/** Label Counter for goto's */
	int label;
	
	/** Flags existence of Global Declarations */
	boolean decs;

	/** Constructor (String filename, SimpleNode root, SymbolTable table)
	 * 
	 * @param filename			filename of .j
	 * @param root			root node
	 * @param table			symbol table
	 */
	public CodeGeneration(String filename, SimpleNode root, SymbolTable table) throws IOException {
		this.FileDescriptor = new File(filename + ".j");
		this.FileOut = new FileOutputStream(this.FileDescriptor);
		this.FileStream = new PrintStream(this.FileOut);
		this.Table = table;
		this.Root = root;
		this.Module = ((SimpleNode) Root.jjtGetChild(0)).val;
		this.initArrays = new ArrayList<Declaration>();
		this.label = 0;
		this.decs = false;
	}

	/** 
	 * Generates file with bytecodes.
	 * First for the module name, and then calls the method for the rest. 
	 * 
	 * @return		true - if successful; false - unsuccessful
	 */
	public boolean initCodeGeneration() throws IOException {
		System.setOut(this.FileStream);

		System.out.println(".class public " + Module);
		System.out.println(".super java/lang/Object\n");

		boolean res = Generate();

		System.setOut(System.out);
		this.FileStream.close();
		this.FileOut.close();

		return res;
	}

	/** 
	 * Generates bytecodes for global declarations, 
	 * vector initialization (clinit) and functions.
	 * 
	 * @return		if an error is not thrown it reaches the end and returns true.
	 */
	public boolean Generate() throws IOException {
		GenerateGlobals(Root);
		if(decs)
			System.out.println();
		staticCode();
		GenerateFunctions(Root);

		return true;
	}

	/** 
	 * Generates bytecodes for global declarations.
	 * 
	 * @param node		global declaration node
	 */
	public void GenerateGlobals(SimpleNode node) throws IOException {
		for(int i=0; i< node.jjtGetNumChildren(); i++)
		{
			GenerateGlobals((SimpleNode) node.jjtGetChild(i));
		}

		// DECLARATION
		if(node.id == YalTreeConstants.JJTDECLARATION)
		{
			genDeclaration(node);
			decs=true;
		}
	}

	/** 
	 * Generates bytecodes for <clinit>
	 */
	public void staticCode() throws IOException {
		if(initArrays.size()!=0){		
			System.out.println(".method static public <clinit>()V");
			System.out.print(".limit stack    \n");
			stacklimit = 0;
			stackval = 0;
			long stackpos = FileOut.getChannel().position() - 4;
			System.out.print(".limit locals 0\n");
			System.out.println();

			for(int i = 0; i<initArrays.size();i++)
			{
				LdConst(initArrays.get(i).size);
				System.out.println("newarray int");
				StGlobal(initArrays.get(i));
				System.out.println();
			}

			//actualizar o limit stack no ficheiro
			long tmp = FileOut.getChannel().position();
			FileOut.getChannel().position(stackpos);
			System.out.print(stacklimit);
			FileOut.getChannel().position(tmp);

			System.out.println("return");
			System.out.println(".end method");
			System.out.println();
		}
	}

	/** 
	 * Generates bytecodes for functions. 
	 * 
	 * @param node		function node
	 */
	public void GenerateFunctions(SimpleNode node) throws IOException {
		for(int i=0; i< node.jjtGetNumChildren(); i++)
		{
			GenerateFunctions((SimpleNode) node.jjtGetChild(i));
		}

		// FUNCTION
		if(node.id == YalTreeConstants.JJTFUNCTION)
		{
			genFunction(node);
			System.out.println();
		}		
	}

	/** 
	 * Auxiliary method for global declaration's bytecodes. 
	 * 
	 * @param node		global declaration node
	 */
	public void genDeclaration(SimpleNode node) throws IOException {
		SimpleNode left = (SimpleNode)node.jjtGetChild(0);
		String name = left.val;

		Declaration dec;

		//tira os "[]" do nome caso existam
		if(name.indexOf("[]")!=-1)
		{
			name = name.substring(0, name.indexOf("["));
			dec = Table.globalDeclarations.get(name);
			if (dec.type == null)
				dec.type = "array";
		}
		else
		{
			dec = Table.globalDeclarations.get(name);
			if (dec.type == null)
				dec.type = "inteiro";

		}

		if (dec.type.compareTo("inteiro")==0)
		{
			if (dec.init)
				System.out.println(".field static " + dec.name + " I = " + dec.value);
			else
				System.out.println(".field static " + dec.name + " I");
		}
		else if (dec.type.compareTo("array")==0)
		{
			System.out.println(".field static " + dec.name + " [I");
			if (dec.init)
				initArrays.add(dec);
		}				 
	}

	/** 
	 * Auxiliary method for function's bytecodes. 
	 * 
	 * @param node		function node
	 */
	public void genFunction(SimpleNode node) throws IOException {
		String name = node.val;
		Function function = Table.functions.get(name);

		stacklimit = 0;
		stackval = 0;
		localsval = 0;

		localsval = function.parameters.size();
		if(function.ret.type.compareTo("void")!=0)
		{
			function.ret.local=localsval;
			localsval++;
		}

		System.out.print(".method public static " + name + "(");

		if(name.equals("main")){
			System.out.print("[Ljava/lang/String;)V\n");
			localsval++;
		}
		else
		{
			for(int i=0;i<function.parameters.size();i++){
				if(function.paramtype.get(i).compareTo("array")==0)
					System.out.print("[I");
				else
					System.out.print("I");
			}        
			System.out.print(")");

			if(function.ret.type.compareTo("array") == 0)
				System.out.print("[I");
			else if (function.ret.type.compareTo("inteiro") == 0)
				System.out.print("I");
			else if (function.ret.type.compareTo("void") == 0)
				System.out.print("V");

			System.out.println();
		}

		System.out.print(".limit stack    \n");
		long stackpos = FileOut.getChannel().position() - 4;
		System.out.print(".limit locals    \n");
		long localspos = FileOut.getChannel().position() - 4;

		System.out.println();

		for(int i=0; i< node.jjtGetNumChildren(); i++)
		{
			SimpleNode son = (SimpleNode) node.jjtGetChild(i);
			if(son.id == YalTreeConstants.JJTBODY)
			{
				genBody(name,son);
			}
		}

		if(function.ret.type.compareTo("void")!=0)
		{
			LdLocal(function.ret);

			if(function.ret.type.compareTo("array")==0)
				System.out.print("areturn\n");
			else
				System.out.print("ireturn\n");
		}
		else
			System.out.print("return\n");

		System.out.print(".end method\n");

		//escrever o limit stack e limit locals, no inicio da funcao
		long tmp = FileOut.getChannel().position();
		FileOut.getChannel().position(stackpos); 
		System.out.print(stacklimit);
		FileOut.getChannel().position(localspos);
		System.out.print(localsval);

		FileOut.getChannel().position(tmp);
	}

	/** 
	 * Generates bytecodes for function bodys. 
	 * 
	 * @param functionName		current function name
	 * @param node		body node
	 */
	public void genBody(String functionName, SimpleNode node) {
		for(int i=0; i< node.jjtGetNumChildren(); i++)
		{
			SimpleNode son = (SimpleNode) node.jjtGetChild(i);

			// ASSIGN
			if(son.id == YalTreeConstants.JJTASSIGN)
			{
				genAssign(functionName,son);
				System.out.println();
			}	
			// CALL
			if(son.id == YalTreeConstants.JJTCALL)
			{
				genCall(functionName,son);
				System.out.println();

			}
			// WHILE
			if(son.id == YalTreeConstants.JJTWHILE)
			{
				genWhile(functionName,son);
				System.out.println();		
			}
			// IF
			if(son.id == YalTreeConstants.JJTIF)
			{
				genIf(functionName,son);
				System.out.println();
			}
		}
	}

	/** 
	 * Generates bytecodes for if's. 
	 * 
	 * @param functionName		current function name
	 * @param node			if node
	 */
	public void genIf(String functionName, SimpleNode node) {
		label++;
		int gif=label;

		String cmp="";

		SimpleNode expr = (SimpleNode)node.jjtGetChild(0);
		SimpleNode body = (SimpleNode)node.jjtGetChild(1);

		if(expr.id == YalTreeConstants.JJTCONDITION)
		{
			for(int i = 0; i < expr.jjtGetNumChildren();i++)
			{
				loadTerm(functionName,(SimpleNode)expr.jjtGetChild(i));
			}
			cmp = operator2string(expr);
			System.out.println(cmp+" endIf"+gif);
		}
		genBody(functionName,body);

		if (node.jjtGetNumChildren()==3) {
			System.out.println("goto else" + gif);
		}

		System.out.println("endIf" + gif + ": ");

		if (node.jjtGetNumChildren()==3) {
			SimpleNode body2 = (SimpleNode)node.jjtGetChild(2);
			genBody(functionName,body2);
			System.out.println("else" + gif + ": ");
		}

	}

	/** 
	 * Generates bytecodes for while's. 
	 * 
	 * @param functionName		current function name
	 * @param node			while node
	 */
	public void genWhile(String functionName, SimpleNode node) {
		label++;
		int gwhile=label;

		String cmp="";

		SimpleNode expr = (SimpleNode)node.jjtGetChild(0);
		SimpleNode body = (SimpleNode)node.jjtGetChild(1);

		System.out.println("while" + gwhile + ": ");

		if(expr.id == YalTreeConstants.JJTCONDITION)
		{
			for(int i = 0; i < expr.jjtGetNumChildren();i++)
			{
				loadTerm(functionName,(SimpleNode)expr.jjtGetChild(i));
			}
			cmp = operator2string(expr);		
			System.out.println(cmp+" endWhile"+gwhile);
		}
		else
			System.out.println("Erro na atribuição do filho.");

		System.out.println();

		genBody(functionName,body);

		System.out.println("goto while"+gwhile);
		System.out.println("endWhile"+gwhile+":");
	}

	/** 
	 * Generates bytecodes for terms. 
	 * 
	 * @param functionName		current function name
	 * @param term			term node
	 */
	public void loadTerm(String functionName, SimpleNode term) {
		if(term.id==YalTreeConstants.JJTINTEGER)
		{
			LdConst(Integer.parseInt(term.val));
		}
		else if(term.id==YalTreeConstants.JJTCALL)
		{
			genCall(functionName,term);
		}
		else if(term.id==YalTreeConstants.JJTACCESS)
		{
			genAccess(functionName,term);
		}
	}

	/** 
	 * Generates bytecodes for access load. 
	 * 
	 * @param functionName		current function name
	 * @param term			term node
	 */
	public void ldAccess(String functionName, SimpleNode term) {
		String termval = term.val;

		if (term.jjtGetNumChildren()>0) { //se for variavel com indice
			if (Table.globalDeclarations.containsKey(termval)) {
				LdGlobal(Table.globalDeclarations.get(termval));
			} else if (localPos(functionName,termval)!=-1) {
				LdLocal(functionName,termval);
			}
			termval = ((SimpleNode) term.jjtGetChild(0)).val;
			if (SymbolTable.isInt(termval)) {
				LdConst(Integer.parseInt(termval));
			} else {
				if (Table.globalDeclarations.containsKey(termval)) {
					LdGlobal(Table.globalDeclarations.get(termval));
				} else if (localPos(functionName,termval)!=-1) {
					LdLocal(functionName,termval);
				}
			}
			/*System.out.println("iaload");
			//changeStack(-1);
		} else if (termval.indexOf(".")!=-1) { //testa se tem "."
			termval = termval.substring(0,termval.indexOf("."));
			if (Table.globalDeclarations.containsKey(termval)) {
				LdGlobal(Table.globalDeclarations.get(termval));
			} else if (localPos(functionName,termval)!=-1) {
				LdLocal(functionName,termval);
			}
			System.out.println("arraylength");*/
		} else { //variavel normal
			if (Table.globalDeclarations.containsKey(termval)) {
				LdGlobal(Table.globalDeclarations.get(termval));
			} else if (localPos(functionName,termval)!=-1) {
				LdLocal(functionName,termval);
			}
		}
	}

	/** 
	 * Generates bytecodes for accesses. 
	 * 
	 * @param functionName		current function name
	 * @param term			access node
	 */
	public void genAccess(String functionName, SimpleNode term) {
		String termval = term.val;

		if (term.jjtGetNumChildren()>0) { //se for variavel com indice
			if (Table.globalDeclarations.containsKey(termval)) {
				LdGlobal(Table.globalDeclarations.get(termval));
			} else if (localPos(functionName,termval)!=-1) {
				LdLocal(functionName,termval);
			}
			termval = ((SimpleNode) term.jjtGetChild(0)).val;
			if (SymbolTable.isInt(termval)) {
				LdConst(Integer.parseInt(termval));
			} else {
				if (Table.globalDeclarations.containsKey(termval)) {
					LdGlobal(Table.globalDeclarations.get(termval));
				} else if (localPos(functionName,termval)!=-1) {
					LdLocal(functionName,termval);
				}
			}
			System.out.println("iaload");
			changeStack(-1);
		} else if (termval.indexOf(".")!=-1) { //testa se tem "."
			termval = termval.substring(0,termval.indexOf("."));
			if (Table.globalDeclarations.containsKey(termval)) {
				LdGlobal(Table.globalDeclarations.get(termval));
			} else if (localPos(functionName,termval)!=-1) {
				LdLocal(functionName,termval);
			}
			System.out.println("arraylength");
		} else { //variavel normal
			if (Table.globalDeclarations.containsKey(termval)) {
				LdGlobal(Table.globalDeclarations.get(termval));
			} else if (localPos(functionName,termval)!=-1) {
				LdLocal(functionName,termval);
			}
		}
	}

	/** 
	 * Generates bytecodes for function calls. 
	 * 
	 * @param functionName		current function name
	 * @param node			call node
	 */
	public void genCall(String functionName, SimpleNode node) {
		String mod = "";
		String func = "";
		String params = "";
		String ret = "";
		int nparam = 0;

		if (node.val.indexOf('.') != -1){
			mod = node.val.substring(0, node.val.indexOf('.'));
			func = node.val.substring(node.val.indexOf('.')+1);
		}
		else
		{
			func = node.val;
			if(Table.functions.containsKey(func))
				mod = Module;
			else
				mod = "Externo";	
		}

		if(node.jjtGetNumChildren() != 0){
			params = argumentList(functionName,(SimpleNode) node.jjtGetChild(0));
			nparam = node.jjtGetChild(0).jjtGetNumChildren();
		}
		else
			params = "()";

		if (mod.compareTo("io") == 0){
			if ((func.compareTo("print") == 0) || (func.compareTo("println") == 0) || (func.compareTo("write") == 0))
				ret = "V";
			else
				ret = "I";
		}
		else if(mod.compareTo(Module) == 0){
			if(Table.functions.get(func).ret.type.compareTo("void")==0)
				ret = "V";
			else if(Table.functions.get(func).ret.type.compareTo("array")==0)
				ret = "[I";
			else
				ret = "I";
		}
		else
			ret ="I";

		System.out.println("invokestatic "+mod+"/"+func+params+ret);
		changeStack(-nparam);
	}

	/** 
	 * Generates bytecodes for function's argument list 
	 * 
	 * @param functionName		current function name
	 * @param node			argument list node
	 */
	public String argumentList(String functionName, SimpleNode node){
		String ret = "(";
		for(int i = 0; i<node.jjtGetNumChildren(); i++)
		{
			SimpleNode arg = (SimpleNode)node.jjtGetChild(i);
			if(arg.val.indexOf("\"") !=-1)
			{
				LdStr(arg.val);
				ret+="Ljava/lang/String;";
			}
			else if(SymbolTable.isInt(arg.val)){
				LdConst(Integer.parseInt(arg.val));
				ret+="I";
			}
			else if(Table.globalDeclarations.containsKey(arg.val))
			{
				Declaration dec = Table.globalDeclarations.get(arg.val);
				LdGlobal(dec);
				if(dec.type.compareTo("array")==0)
					ret+="[I";
				else if (dec.type.compareTo("inteiro")==0)
					ret+="I";
			}
			else if(localPos(functionName,arg.val)!=-1)
			{
				LdLocal(functionName,arg.val);
				String type = localType(functionName,arg.val);
				if(type.compareTo("array")==0)
					ret+="[I";
				else if (type.compareTo("inteiro")==0)
					ret+="I";
			}
		}
		ret+=")";
		return ret;
	}

	/** 
	 * Generates bytecodes for variable assigns. 
	 * 
	 * @param functionName		current function name
	 * @param node			assign node
	 */
	public void genAssign(String functionName, SimpleNode node) {
		SimpleNode left = (SimpleNode)node.jjtGetChild(0);
		SimpleNode right = (SimpleNode)node.jjtGetChild(1);

		if(right.id==YalTreeConstants.JJTOPERATION)
		{

			if(right.jjtGetNumChildren() == 1)
			{
				//(caso em que é iastore)
				if(left.jjtGetNumChildren()!=0)
				{
					//LOAD LEFT
					ldAccess(functionName, left);

					//LOAD RIGHT
					SimpleNode term = (SimpleNode) right.jjtGetChild(0);
					loadTerm(functionName, term);

					//STORE LEFT
					System.out.println("iastore");
					changeStack(-3);
				}
				else
				{
					//LOAD RIGHT
					SimpleNode term = (SimpleNode) right.jjtGetChild(0);
					loadTerm(functionName, term);

					//STORE LEFT
					if(Table.globalDeclarations.containsKey(left.val))
						StGlobal(Table.globalDeclarations.get(left.val));
					else if(localPos(functionName,left.val)!=-1)
						StLocal(functionName,left.val);	
				}
			}
			else if(right.jjtGetNumChildren() == 2)
			{
				// caso especial iinc
				if(((left.val.compareTo(right.jjtGetChildVal(0)) == 0 && right.jjtGetChildVal(1).compareTo("1")==0) ||
						(left.val.compareTo(right.jjtGetChildVal(1)) == 0 && right.jjtGetChildVal(0).compareTo("1")==0)) &&
						localPos(functionName,left.val)!=-1 && right.val.compareTo("+")==0)
				{
					int n = localPos(functionName,left.val);
					System.out.println("iinc " + n + " 1");
				}
				else
				{					
					//LOAD RIGHT1
					SimpleNode term1 = (SimpleNode) right.jjtGetChild(0);
					loadTerm(functionName, term1);

					//LOAD RIGHT2
					SimpleNode term2 = (SimpleNode) right.jjtGetChild(1);
					loadTerm(functionName, term2);

					//OP
					String op = op2str(right);
					System.out.println(op);

					//(caso em que é iastore)
					if(left.jjtGetNumChildren()!=0){
						//STORE RIGHT 1+2
						StLocal("inteiro",localsval); //System.out.println("istore " + localsval);
						localsval++;

						//LOAD LEFT
						ldAccess(functionName, left);

						//LOAD RIGHT 1+2
						LdLocal("inteiro",localsval-1); //System.out.println("iload " + localsval-1);

						//STORE LEFT
						System.out.println("iastore");
						changeStack(-3);
					}
					else
					{
						//STORE LEFT
						if(Table.globalDeclarations.containsKey(left.val))
							StGlobal(Table.globalDeclarations.get(left.val));
						else if(localPos(functionName,left.val)!=-1)
							StLocal(functionName,left.val);
					}
				}
			}

			/*Collection<Declaration> col = Table.functions.get(functionName).localDeclarations.values();
			Iterator<Declaration> it = col.iterator();
			while(it.hasNext()){
				System.out.println(((Declaration) it.next()).name);
			}*/
		}

		else if(right.id==YalTreeConstants.JJTARRAYSIZE)
		{	//LOAD RIGHT
			String rightval = right.val;

			if (SymbolTable.isInt(rightval)) {
				LdConst(Integer.parseInt(rightval));
			}
			else if (rightval.indexOf(".")!=-1) { //testa se tem "."
				rightval = rightval.substring(0,rightval.indexOf("."));
				if (Table.globalDeclarations.containsKey(rightval)) {
					LdGlobal(Table.globalDeclarations.get(rightval));
				} else if (localPos(functionName,rightval)!=-1) {
					LdLocal(functionName,rightval);
				}
				System.out.println("arraylength");
			}
			else { //variavel normal
				if (Table.globalDeclarations.containsKey(rightval)) {
					LdGlobal(Table.globalDeclarations.get(rightval));
				} else if (localPos(functionName,rightval)!=-1) {
					LdLocal(functionName,rightval);
				}
			}
			//STORE LEFT
			System.out.println("newarray int");
			if(Table.globalDeclarations.containsKey(left.val))
				StGlobal(Table.globalDeclarations.get(left.val));
			else if(localPos(functionName,left.val)!=-1)
				StLocal(functionName,left.val);

		}
	}

	//////////////////////////////////////////////////////////////////

	/** 
	 * Generates bytecodes for local store 
	 * 
	 * @param function		current function name
	 * @param val			value to store
	 */
	public void StLocal(String function, String val)
	{
		//locais
		if(Table.functions.get(function).localDeclarations.containsKey(val))
			StLocal(Table.functions.get(function).localDeclarations.get(val));
		//retorno
		else if(Table.functions.get(function).ret.name.compareTo(val)==0)
			StLocal(Table.functions.get(function).ret);
		//argumentos
		else
		{
			String type = localType(function, val);
			int pos = localPos(function, val);
			if(pos!=-1)
				StLocal(type,pos);
		}
	}

	/** 
	 * Generates bytecodes for local store 
	 * of variables not included in the Symbol Table. 
	 * 
	 * @param t		variable name
	 * @param i			value
	 */
	public void StLocal(String t,int i){
		Declaration dec = new Declaration(t,i);
		StLocal(dec);
	}

	/** 
	 * Generates bytecodes for local store of a declaration. 
	 * 
	 * @param dec		declaration to store
	 */
	public void StLocal(Declaration dec){
		if(dec.local==-1)
		{
			dec.local = localsval;
			localsval++;
		}

		if(dec.type.compareTo("array")==0)
			StA(dec);
		else
			StI(dec);
	}

	/** 
	 * Generates bytecodes to store integer values 
	 * 
	 * @param dec		declaration to store
	 */
	public void StI(Declaration dec){
		int i=dec.local;

		if(i>3)
			System.out.println("istore "+i);
		else
			System.out.println("istore_"+i);
		changeStack(-1);
	}

	/** 
	 * Generates bytecodes to store array values 
	 * 
	 * @param dec		declaration to store
	 */
	public void StA(Declaration dec){
		int i=dec.local;

		if(i>3)
			System.out.println("astore "+i);
		else
			System.out.println("astore_"+i);
		changeStack(-1);
	}

	/** 
	 * Generates bytecodes to store global declarations 
	 * 
	 * @param dec		declaration to store
	 */
	public void StGlobal(Declaration dec){
		System.out.print("putstatic " + Module + "/" + dec.name );
		if(dec.type.compareTo("array")==0)
			System.out.print(" [I\n");
		else
			System.out.print(" I\n");
		changeStack(-1);
	}

	//////////////////////////////////////////////////////////////////

	/** 
	 * Generates bytecodes for local load 
	 * 
	 * @param function		current function name
	 * @param val			value to load
	 */
	public void LdLocal(String function, String val)
	{
		//locais
		if(Table.functions.get(function).localDeclarations.containsKey(val))
			LdLocal(Table.functions.get(function).localDeclarations.get(val));
		//retorno
		else if(Table.functions.get(function).ret.name.compareTo(val)==0)
			LdLocal(Table.functions.get(function).ret);
		//argumentos
		else
		{
			String type = localType(function, val);
			int pos = localPos(function, val);
			if(pos!=-1)
				LdLocal(type,pos);
		}
	}

	/** 
	 * Generates bytecodes for local load 
	 * of variables not included in the Symbol Table. 
	 * 
	 * @param t		variable name
	 * @param i			value
	 */
	public void LdLocal(String t,int i){
		Declaration dec = new Declaration(t,i);
		LdLocal(dec);
	}

	/** 
	 * Generates bytecodes for local load of a declaration. 
	 * 
	 * @param dec		declaration to load
	 */
	public void LdLocal(Declaration dec){
		if(dec.type.compareTo("array")==0)
			LdA(dec);
		else
			LdI(dec);
	}

	/** 
	 * Generates bytecodes to load integer values 
	 * 
	 * @param dec		declaration to load
	 */
	public void LdI(Declaration dec){
		int i=dec.local;

		if(i<0)         
			return;

		if(i>3)
			System.out.println("iload "+i);
		else
			System.out.println("iload_"+i);
		changeStack(1);
	}

	/** 
	 * Generates bytecodes to load array values 
	 * 
	 * @param dec		declaration to load
	 */
	public void LdA(Declaration dec){
		int i=dec.local;

		if(i<0)
			return;

		if(i>3)
			System.out.println("aload "+i);
		else
			System.out.println("aload_"+i);
		changeStack(1);
	}

	/** 
	 * Generates bytecodes to load global declarations 
	 * 
	 * @param dec		declaration to load
	 */
	public void LdGlobal(Declaration dec){
		System.out.print("getstatic " + Module + "/" + dec.name);
		if(dec.type.compareTo("array")==0)
			System.out.print(" [I\n");
		else
			System.out.print(" I\n");
		changeStack(1);
	}

	/** 
	 * Generates bytecodes to load constant 
	 * 
	 * @param val		constant to load
	 */
	public void LdConst(int val){
		if( val>5 || val<0){
			if((val>-127 && val<127))
				System.out.println("bipush " + val);
			else
				System.out.println("ldc " + val);
		}else
			System.out.println("iconst_" + val);
		changeStack(1);
	}

	/** 
	 * Generates bytecodes to load a string 
	 * 
	 * @param str		string to load
	 */
	public void LdStr(String str){
		System.out.println("ldc " + str);
		changeStack(1);
	}

	//////////////////////////////////////////////////////////////////

	/** 
	 * Generates bytecodes for conditional operations 
	 * 
	 * @param expr		expression
	 * @return		bytecode of given expression
	 */
	public String operator2string(SimpleNode expr){
		String op="";
		if(expr.val.equals("=="))
			op="if_icmpne";
		else if(expr.val.equals("=<")||expr.val.equals("<="))
			op="if_icmpgt";
		else if(expr.val.equals("=>")||expr.val.equals(">="))
			op="if_icmplt";
		else if(expr.val.equals(">"))
			op="if_icmple";
		else if(expr.val.equals("!="))
			op="if_icmpeq";
		else if(expr.val.equals("<"))
			op="if_icmpge";
		changeStack(-2);
		return op;
	}

	/** 
	 * Generates bytecodes for arithmetic or bitwise operations 
	 * 
	 * @param operation		operator
	 * @return		bytecode of given operator
	 */
	public String op2str(SimpleNode operation){
		String op="";
		if(operation.val.equals("*")){
			op="imul";
		}else if(operation.val.equals("/")){
			op="idiv";
		}else if(operation.val.equals("<<")){
			op="ishl";
		}else if(operation.val.equals(">>")){
			op="ishr";
		}else if(operation.val.equals("&")){
			op="iand";
		}else if(operation.val.equals("+")){
			op="iadd";
		}else if(operation.val.equals("-")){
			op="isub";
		}else if(operation.val.equals("|")){
			op="ior";
		}
		changeStack(-1);
		return op;
	}

	/** 
	 * Gets the index of an element in a list  
	 * 
	 * @param list		given list
	 * @param name		element name
	 * 
	 * @return		index if found, -1 if not found
	 */
	public int getParamN(ArrayList<String> list, String name)
	{
		int i = 0;
		for (i = 0; i<list.size(); i++)
			if(list.get(i).compareTo(name)==0)
				return i;
		return -1;
	}

	/** 
	 * Changes stack value
	 * 
	 * @param i		value to add or subtract to stack
	 */
	public void changeStack(int i){
		stackval = stackval + i;
		if(stackval < 0)
			stackval = 0;
		if(stackval > stacklimit)
			stacklimit = stackval;
	}

	/** 
	 * Gets the stack position of a function variable  
	 * 
	 * @param function		function name
	 * @param val		variable
	 */
	public int localPos(String function, String val) //apenas devolve o valor não altera no limitlocals
	{
		if(Table.functions.get(function).localDeclarations.containsKey(val))
		{
			if(Table.functions.get(function).localDeclarations.get(val).local==-1)
				return localsval;
			else
				return Table.functions.get(function).localDeclarations.get(val).local;
		}
		else if(Table.functions.get(function).parameters.contains(val))
			return getParamN(Table.functions.get(function).parameters, val);
		else if(Table.functions.get(function).ret.name.compareTo(val)==0)
			return Table.functions.get(function).ret.local;
		else
			return -1;	
	}	

	/** 
	 * Gets the variable type of a function parameter  
	 * 
	 * @param function		function name
	 * @param val		variable
	 */
	public String localType(String function, String val)
	{
		if(Table.functions.get(function).localDeclarations.containsKey(val))
			return Table.functions.get(function).localDeclarations.get(val).type;
		else if(Table.functions.get(function).parameters.contains(val))
		{
			int i = getParamN(Table.functions.get(function).parameters, val);
			return Table.functions.get(function).paramtype.get(i);
		}
		else if(Table.functions.get(function).ret.name.compareTo(val)==0)
			return Table.functions.get(function).ret.type;
		else
			return null;	
	}
}
