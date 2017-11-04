import java.util.HashMap;

/**
 * The SymbolTable class represents a Table with storage of relevant information of a Yal module. 
 * It stores the module name, global declarations, functions and the module validity (if it's error free)
 * It possesses Semantic Methods to test the referenced module validity
 */
public class SymbolTable {
	
	/** Module name */
	String module;
	
	/** Map of Functions */
	HashMap<String,Function> functions;
	
	/** Map of Global Declarations */
	HashMap<String,Declaration> globalDeclarations;
	
	/** Auxiliar Map of Declarations to help the Semantics process */
	HashMap<String,String> tempDeclarations;
	
	/** Module validity */
	boolean Erro;
	
	/** Constructor
	 * 
	 */
	public SymbolTable(){
		
		System.out.println("\n\nTABELA DE SÍMBOLOS E ANALISE SEMANTICA\n");
		
		functions=new HashMap<String,Function>();
		globalDeclarations=new HashMap<String,Declaration>();
		tempDeclarations=new HashMap<String,String>();
		Erro=false;
	}
	
	/** Tests if the given String is a valid Integer
	 * 
	 * @param i			given string for parsing
	 * @return			true if its a valid Integer, false if invalid
	 */
	public static boolean isInt(String i)
	{
		try
		{
			Integer.parseInt(i);
			return true;
		}
		catch(NumberFormatException nfe)
		{
			return false;
		}
	}
	
	//dado um termo que pode ser 
	//inteiro, funcao ou variavel
	//devolve o tipo
	/** Given a Term Node and current Function, return the term type
	 * 
	 * @param term			term node which can be an integer, function call or variable
	 * @param actualFunction		Current function name
	 * @return			null in case of error, "inteiro" in case of an integer type, "array" if its an array type or "ext" if its an external method
	 */
	public String TermType(SimpleNode term, String actualFunction) {
		String termval = term.val;
		//retira os sinais 
		if ((termval.indexOf("-")!=-1) || (termval.indexOf("+")!=-1)) {
			termval = termval.substring(1);
		}
		//System.out.println(termval);
		
		if (termval.compareTo("io.read")==0) {
			System.out.println("Aviso na linha " + term.line + 
					": Chamada de uma funcao de um modulo externo.");
			return "inteiro";
		}
		
		String tipo=null;
		
		if ( term.id==YalTreeConstants.JJTINTEGER) {
			tipo="inteiro";
		} else if ( term.id==YalTreeConstants.JJTCALL) {
			//verifica se e um modulo externo
			if (termval.indexOf(".")!=-1) {
				String mod = termval.substring(0,termval.indexOf("."));
				termval = termval.substring(termval.indexOf(".")+1);
				if ( (mod.compareTo(module)!=0) || (mod.compareTo("io")==0) ) {
					System.out.println("Aviso na linha " + term.line + ": Chamada de uma funcao" +
							" de um modulo externo.");
					tipo="ext";
					return tipo;
				}
			}
			
			Function func = functions.get(termval);
			if (func!=null) {
				//se existir testa se o numero e tipo de parametros bate certo
				String nomefuncao = termval;
				System.out.println("Chamada a funcao: " + nomefuncao);
				//primeiro o numero
				if (term.jjtGetNumChildren()>0) { //se forem dados paramentros
					SimpleNode sn = (SimpleNode) term.jjtGetChild(0);
					if (func.parameters.size()!=sn.jjtGetNumChildren()) {
						System.out.println("Erro na linha " + term.line + 
						": Numero de argumentos errado" );
						Erro=true;
						return tipo;
					} else {
						//depois o tipo
						for (int i=0; i<sn.jjtGetNumChildren();i++) {
							String tempval = sn.jjtGetChildVal(i);
							String tipocomp="void";
							if (isInt(tempval)) {
								tipocomp="inteiro";
							} else {
								//verificamos a sua existencia e retiramos o tipo
								if (functions.get(actualFunction).ret.name.compareTo(tempval)==0) {
									tipocomp=functions.get(actualFunction).ret.type;
								} else if (functions.get(actualFunction).localDeclarations.containsKey(tempval)) {
									tipocomp=functions.get(actualFunction).localDeclarations.get(tempval).type;
								} else if (functions.get(actualFunction).parameters.contains(tempval)) {
									tipocomp=functions.get(actualFunction).paramtype.get(functions.get(actualFunction).parameters.indexOf(tempval));
								} else if (globalDeclarations.containsKey(tempval)) {
									tipocomp=globalDeclarations.get(tempval).type;
								} else {
									System.out.println("Erro na linha " + term.line + 
											": Variavel '" + tempval + "' nao declarada" );
									Erro=true;
								}
							}
							//para cada parametro comparamos 
							//os tipos dados com os que a funcao espera
							if (tipocomp.compareTo("void")!=0) {
								if (func.paramtype.get(i).compareTo(tipocomp)!=0) {
									System.out.println("Erro na linha " + term.line + 
										": Parametro incompativel" );
									Erro=true;
								}
							}
						}
					}
				} else { 
					//se nao forem dados parametros
					//testamos se a funcao pede parametros
					if (func.parameters.size()>0) {
						System.out.println("Erro na linha " + term.line + 
						": Numero de argumentos errado" );
						Erro=true;
						return tipo;
					}
				}
				tipo=func.ret.type;
			} else {
				System.out.println("Erro na linha " + term.line + 
					": Funcao nao declarada" );
				Erro=true;
			}
		} else if ( term.id==YalTreeConstants.JJTACCESS) {
			if (term.jjtGetNumChildren()>0) { // se for variavel com indice
				//testa o indice
				String indexval = ((SimpleNode) term.jjtGetChild(0)).val;
				if (!isInt(indexval)) { //se nao for inteiro e nao existir da erro
					if( (!functions.get(actualFunction).localDeclarations.containsKey(indexval) ) && 
						(!globalDeclarations.containsKey(indexval)) &&
						(!functions.get(actualFunction).parameters.contains(indexval)) &&
						(functions.get(actualFunction).ret.name.compareTo(indexval)!=0) ) {
							System.out.println("Erro na linha " + term.line + 
								": Variavel nao declarada no indice" );
							Erro=true;
							return tipo;
					} else { //se existir variavel do indice testa onde e se o tipo e compativel
						if ( (functions.get(actualFunction).ret.name.compareTo(indexval)==0) &&
								(functions.get(actualFunction).ret.type.compareTo("inteiro")!=0) ) {
							System.out.println("Erro na linha " + term.line + 
								": Variavel de retorno tipo array num indice" );
							Erro=true;
							return tipo;
						} else if ( (functions.get(actualFunction).localDeclarations.containsKey(indexval)) &&
								(functions.get(actualFunction).localDeclarations.get(indexval).type.compareTo("inteiro")!=0) ) {
							System.out.println("Erro na linha " + term.line + 
								": Variavel local do tipo array num indice" );
							Erro=true;
							return tipo;
						} else if ( (functions.get(actualFunction).parameters.contains(indexval)) &&
								(functions.get(actualFunction).paramtype.get(functions.get(actualFunction).parameters.indexOf(indexval)).compareTo("inteiro")!=0) ) {
							System.out.println("Erro na linha " + term.line + 
								": Parametro do tipo array num indice" );
							Erro=true;
							return tipo;
						} else if ( (globalDeclarations.containsKey(indexval) ) &&
								(globalDeclarations.get(indexval).type.compareTo("inteiro")!=0) ) {
							System.out.println("Erro na linha " + term.line + 
								": Variavel global do tipo array num indice" );
							Erro=true;
							return tipo;
						}
					}
				}
				
				//testa a variavel e existencia dela
				if( (!functions.get(actualFunction).localDeclarations.containsKey(termval) ) && 
						(!globalDeclarations.containsKey(termval)) &&
						(!functions.get(actualFunction).parameters.contains(termval)) &&
						(functions.get(actualFunction).ret.name.compareTo(termval)!=0) ) {
							System.out.println("Erro na linha " + term.line + 
								": Variavel nao declarada" );
							Erro=true;
							return tipo;
				} else { //se existe variavel tem ser array
					if ( (functions.get(actualFunction).ret.name.compareTo(termval)==0) &&
							(functions.get(actualFunction).ret.type.compareTo("array")!=0) ) {
						System.out.println("Erro na linha " + term.line + 
							": Variavel de retorno de tipo incompativel" );
						Erro=true;
						return tipo;
					} else if ( (functions.get(actualFunction).localDeclarations.containsKey(termval)) &&
							(functions.get(actualFunction).localDeclarations.get(termval).type.compareTo("array")!=0) ) {
						System.out.println("Erro na linha " + term.line + 
							": Variavel local de tipo incompativel" );
						Erro=true;
						return tipo;
					} else if ( (functions.get(actualFunction).parameters.contains(termval)) &&
							(functions.get(actualFunction).paramtype.get(functions.get(actualFunction).parameters.indexOf(termval)).compareTo("array")!=0) ) {
						System.out.println("Erro na linha " + term.line + 
							": Parametro de tipo incompativel" );
						Erro=true;
						return tipo;
					} else if ( (globalDeclarations.containsKey(termval) ) &&
							(globalDeclarations.get(termval).type.compareTo("array")!=0) ) {
						System.out.println("Erro na linha " + term.line + 
							": Variavel global de tipo incompativel" );
						Erro=true;
						return tipo;
					}
				}
				tipo="inteiro";
			} else if (termval.indexOf(".")!=-1) { //testa se tem "."
				termval = termval.substring(0,termval.indexOf("."));
				//testa a variavel e existencia dela
				if( (!functions.get(actualFunction).localDeclarations.containsKey(termval) ) && 
						(!globalDeclarations.containsKey(termval)) &&
						(!functions.get(actualFunction).parameters.contains(termval)) &&
						(functions.get(actualFunction).ret.name.compareTo(termval)!=0) ) {
							System.out.println("Erro na linha " + term.line + 
								": Variavel nao declarada" );
							Erro=true;
							return tipo;
				} else { //se existe variavel tem ser array
					if ( (functions.get(actualFunction).ret.name.compareTo(termval)==0) &&
							(functions.get(actualFunction).ret.type.compareTo("array")!=0) ) {
						System.out.println("Erro na linha " + term.line + 
							": Variavel de retorno de tipo incompativel" );
						Erro=true;
						return tipo;
					} else if ( (functions.get(actualFunction).localDeclarations.containsKey(termval)) &&
							(functions.get(actualFunction).localDeclarations.get(termval).type.compareTo("array")!=0) ) {
						System.out.println("Erro na linha " + term.line + 
							": Variavel local de tipo incompativel" );
						Erro=true;
						return tipo;
					} else if ( (functions.get(actualFunction).parameters.contains(termval)) &&
							(functions.get(actualFunction).paramtype.get(functions.get(actualFunction).parameters.indexOf(termval)).compareTo("array")!=0) ) {
						System.out.println("Erro na linha " + term.line + 
							": Parametro de tipo incompativel" );
						Erro=true;
						return tipo;
					} else if ( (globalDeclarations.containsKey(termval) ) &&
							(globalDeclarations.get(termval).type.compareTo("array")!=0) ) {
						System.out.println("Erro na linha " + term.line + 
							": Variavel global de tipo incompativel" );
						Erro=true;
						return tipo;
					}
				}
				tipo="inteiro";
			} else {
				//testa a variavel e existencia dela
				if( (!functions.get(actualFunction).localDeclarations.containsKey(termval) ) && 
						(!globalDeclarations.containsKey(termval)) &&
						(!functions.get(actualFunction).parameters.contains(termval)) &&
						(functions.get(actualFunction).ret.name.compareTo(termval)!=0) ) {
							System.out.println("Erro na linha " + term.line + 
								": Variavel nao declarada" );
							Erro=true;
							return tipo;
				} else { //se existe variavel tiramos o tipo
					if  (functions.get(actualFunction).ret.name.compareTo(termval)==0) {
						if (functions.get(actualFunction).ret.type.compareTo("undefined")==0) {
							functions.get(actualFunction).ret.type="inteiro";
							System.out.println("Retorno da funcao definido como inteiro");
						}
						tipo=functions.get(actualFunction).ret.type;
					} else if (functions.get(actualFunction).localDeclarations.containsKey(termval)) {
						tipo=functions.get(actualFunction).localDeclarations.get(termval).type;
					} else if (functions.get(actualFunction).parameters.contains(termval)) {
						tipo=functions.get(actualFunction).paramtype.get(functions.get(actualFunction).parameters.indexOf(termval));
					} else if  (globalDeclarations.containsKey(termval) ) {
						tipo=globalDeclarations.get(termval).type;
					} else return tipo;
				}
			}
		}
		return tipo;
	}
	//-----
	
	// DECLARACOES GLOBAIS
	/** Given a Declaration Node tests the semantics and stores the global declaration in the corresponding Map
	 * 
	 * @param node			Global Declaration Node of the AST
	 */
	public void stDeclaration(SimpleNode node)
	{
		int nchildren = node.jjtGetNumChildren();
		String typeL="void";
		String typeR="void";
		
		SimpleNode left = (SimpleNode)node.jjtGetChild(0);
		String name = left.val;
		
		SimpleNode right = null;
		String rightval = "void";
		
		//tira os "[]" do valor esquerdo se os tiver e indica que e array
		if(name.indexOf("[]")!=-1) {
			typeL = "array";
			name = name.substring(0, name.indexOf("["));
		}
		
		Declaration dec = new Declaration(name);
		
		//lado direito
		if(nchildren == 2) {
			right = (SimpleNode)node.jjtGetChild(1);
			rightval = right.val;
			
			//verifica se esquerdo e direito sao iguais
			String eqval=rightval;
			if(eqval.indexOf(".size")!=-1) {
				eqval=eqval.substring(0, eqval.indexOf("."));
			}
			//se forem iguais da erro
			if (name.compareToIgnoreCase(eqval)==0) {
				System.out.println("Erro na linha " + right.line + 
				": Nao pode atribuir a variavel a ela propria");
				Erro=true;
				return;
			}
			
			//vamos ver se e inteiro
			if(right.id == YalTreeConstants.JJTINTEGER)
			{
				//se esquerdo for array da erro se nao tiver inicializado
				if (typeL.compareTo("array")==0) {
					if(!(globalDeclarations.containsKey(name))) {
						System.out.println("Erro na linha " + right.line + 
						": Inteiro atribuido a um array inexistente");
						Erro=true;
						return;
					}
				}
				
				//verifica se inteiro esta nos limites
				if(isInt(rightval)) {
					int nsize = Integer.parseInt(rightval);
					dec.initInt(nsize);
					typeR="inteiro";
				} else {
					System.out.println("Erro na linha " + right.line + 
					": Erro no parsing do inteiro.");
					Erro=true;
					return;
				}
				
			} else if(right.id == YalTreeConstants.JJTARRAYSIZE) {
				int nsize=0;
				typeR="array";
				
				// ARRAYSIZE == INTEGER
				if(isInt(rightval)) {
					nsize = Integer.parseInt(rightval);
				} 
				
				// ARRAYSIZE == VARIAVEL.SIZE
				else if(rightval.indexOf(".size")!=-1) 
				{
					rightval=rightval.substring(0, rightval.indexOf("."));
					if ( (globalDeclarations.containsKey(rightval)) && 
					(globalDeclarations.get(rightval).type.compareTo("array")==0) ) {
							nsize = globalDeclarations.get(rightval).size;
					} else {
						System.out.println("Erro na linha " + right.line + ": Variavel nao existe ou nao e do tipo array.");
						Erro=true;
						return;
					}
				}
				
				// ARRAYSIZE == VARIAVEL
				else if ( (rightval.indexOf(".size")==-1) && (globalDeclarations.containsKey(rightval)) ) {
					if (globalDeclarations.get(rightval).init==false) {
						System.out.println("Erro na linha " + right.line + ": Variavel nao inicializada.");
						Erro=true;
						return;
					} else 
					if(globalDeclarations.get(rightval).type.compareTo("inteiro")==0) {
						nsize = globalDeclarations.get(rightval).value;
					}
					else
					{
						System.out.println("Erro na linha " + right.line + ": Tipo do ArraySize desadequado.");
						Erro=true;
						return;
					}
				}
				else
				{
					System.out.println("Erro na linha " + right.line + ": Variavel utilizada no ArraySize nao declarada.");
					Erro=true;
					return;
				}
								
				if (nsize<=0) {
					System.out.println("Erro na linha " + right.line + 
					": Tamanho negativo ou zero.");
					Erro=true;
					return;
				} else {
					dec.initArray(nsize);
					typeR="array";
				}
			}
		}
		
		//lado esquerdo
		
		// VARIAVEL NAO EXISTE
		if(!(globalDeclarations.containsKey(name)))
		{	
			//se esquerdo for array direito tem de ser array
			if (typeL.compareTo("array")==0) {
				System.out.println(dec.toString());
				globalDeclarations.put(name, dec);					
				/*if (typeR.compareTo("array")==0) {
					System.out.println(dec.toString());
					globalDeclarations.put(name, dec);
				} else {
					System.out.println("Erro na linha " + left.line + 
							": Variavel declarada com tipo incompativel.");
					Erro=true;
					return;
				}*/
				
			//se esquerdo nao for array entao direito pode ser qualquer coisa
			} else { 
				System.out.println(dec.toString());
				globalDeclarations.put(name, dec);
			}			
		} else
		
		// VARIAVEL EXISTE
		if(globalDeclarations.containsKey(name))
		{
			//se existe so pode ser aceite se for um inteiro 
			//e for atribuido outro inteiro
			//senao e uma redeclaracao
			String otipo = globalDeclarations.get(name).type;
			if ((otipo.compareTo("inteiro")==0) && (typeR.compareTo("inteiro")==0)) {
				//ok - aceitou
			} else if ((otipo.compareTo("array")==0) && (typeR.compareTo("inteiro")==0)) {
				//por values
				//globalDeclarations.get(name).values;
			} else {
				System.out.println("Erro na linha " + left.line + ": Variavel ja declarada.");
				Erro=true;
			}
		}
	}
	
	// FUNCOES
	/** Given a Function Node tests the semantics and stores Function information 
	 * relative to name, parameters and function return in the corresponding Map
	 * 
	 * @param node			Function Node of the AST
	 */
	public void stFunction(SimpleNode node)
	{
		String nomefuncao = node.val;
		String retorno="void";
		String retname="";
		String comp=null;
		int next=0; //proximo filho
		
		//testa se o nome da funcao tem ponto
		if (nomefuncao.indexOf(".")!=-1) {
			nomefuncao = nomefuncao.substring(nomefuncao.indexOf(".")+1);
			System.out.println("Aviso na linha " + node.line + ": Funcao contem um ponto. " +
					"Nome truncado para " + nomefuncao);
		}
		
		//testa se a funcao ja existe
		if (functions.containsKey(nomefuncao)) {
			//System.out.println("Erro na linha " + node.line + 
			//": Funcao ja declarada");
			//Erro=true;
			return;
		}
		
		//testa se tem variavel de retorno
		if ( ((SimpleNode) node.jjtGetChild(next)).id == YalTreeConstants.JJTFUNCTIONRETURN ) {
			retorno="undefined";
			//para efeitos de comparacao retira-se apenas o nome da variavel
			comp=node.jjtGetChildVal(next);
			if(comp.indexOf("[]")!=-1)
			{
				comp = comp.substring(0, comp.indexOf("["));
				retorno="array";
			}
			retname=comp;
			next++;
			//System.out.println("Retorno: " + retorno);
		} 
		
		//cria objecto
		Function func = new Function(nomefuncao,retorno,retname);

		//verifica se tem parametros
		if ( ((SimpleNode) node.jjtGetChild(next)).id == YalTreeConstants.JJTVARLIST ) {
			//para cada paramentro verificamos se ja existe 
			//ou se e igual a variavel de retorno
			//System.out.println("Lista de Parametros:");
			for (int i=0; i<node.jjtGetChild(next).jjtGetNumChildren();i++) {
				String temp = ((SimpleNode) node.jjtGetChild(next)).jjtGetChildVal(i);
				String tipoparam="inteiro";

				if(temp.indexOf("[]")!=-1)
				{
					temp = temp.substring(0, temp.indexOf("["));
					tipoparam="array";
				}
				//testa se nao e repetido
				if ( (!func.parameters.contains(temp)) ){
					//se nao tem retorno acrescenta logo
					//senao verifica se nao e igual ao retorno
					if (comp!=null) { 
						if (temp.compareTo(comp)==0) {
							System.out.println("Erro na linha " + node.line + 
							": Parametro nao pode ser igual a variavel de retorno");
							Erro=true;
							return;
						}
					}
					func.parameters.add(temp);
					func.paramtype.add(tipoparam);
					//System.out.println(temp);
				} else {
					System.out.println("Erro na linha " + node.line + ": Argumento repetido");
					Erro=true;
					return;
				}
			}
			next++;
		}
		
		//acrescentamos a lista de funcoes declaradas
		functions.put(nomefuncao, func);
		//System.out.println("Declarada " + func.toString());
		
		//encontrar o tipo do retorno no corpo da funcao
		if ( ((SimpleNode) node.jjtGetChild(next)).id == YalTreeConstants.JJTBODY ) {
			if (retorno.compareTo("undefined")==0) {
				tempDeclarations.clear();
				stBodyRet((SimpleNode) node.jjtGetChild(next),nomefuncao);
			}
		}
		
	}
	
	// CORPO DE FUNCOES
	/** Given a Function Node tests the semantics and stores the Function Body information in the corresponding Map
	 * 
	 * @param node			Function Node of the AST
	 */
	public void stFunctionBody(SimpleNode node)
	{
		String nomefuncao = node.val;
		int next=0; //proximo filho
		
		//testa se o nome da funcao tem ponto
		if (nomefuncao.indexOf(".")!=-1) {
			nomefuncao = nomefuncao.substring(nomefuncao.indexOf(".")+1);
		}
		
		//testa se a funcao esta na hash
		if (!functions.containsKey(nomefuncao)) {
			return;
		}
		
		//testa se tem variavel de retorno
		if ( ((SimpleNode) node.jjtGetChild(next)).id == YalTreeConstants.JJTFUNCTIONRETURN ) {
			next++;
		} 

		//verifica se tem parametros
		if ( ((SimpleNode) node.jjtGetChild(next)).id == YalTreeConstants.JJTVARLIST ) {
			next++;
		}
		
		//chegamos ao corpo da funcao
		if ( ((SimpleNode) node.jjtGetChild(next)).id == YalTreeConstants.JJTBODY ) {
			//testar se ja foi declarada
			if (functions.get(nomefuncao).declared) {
				System.out.println("Erro na linha " + node.line + 
				": Funcao ja declarada");
				Erro=true;
				return;
			} else {
				functions.get(nomefuncao).declared=true;
				System.out.println("Declarada " + functions.get(nomefuncao).toString());
				stBody((SimpleNode) node.jjtGetChild(next),nomefuncao);
			}
		}
		
		if (functions.get(nomefuncao).ret.type.compareTo("undefined")==0) {
			System.out.println("Aviso na linha " + node.line + 
			": Retorno nao e definido no corpo da funcao");
		}
		
	}
	
	/** Given a Function Body Node tests the semantics and stores the Function return information in the corresponding Map
	 * 
	 * @param node			Function Body Node of the AST
	 * @param funcao		Current function name
	 */
	public void stBodyRet(SimpleNode node, String funcao) {
		
		//testa para todos os filhos o tipo de nodo
		for (int j=0;j<node.jjtGetNumChildren();j++) {
			SimpleNode sn = (SimpleNode) node.jjtGetChild(j);
			//While
			if ( sn.id == YalTreeConstants.JJTWHILE) {
				stBodyRet((SimpleNode) sn.jjtGetChild(1),funcao);
			} else
			//If
			if ( sn.id == YalTreeConstants.JJTIF) {
				stBodyRet((SimpleNode) sn.jjtGetChild(1),funcao);
				if (sn.jjtGetNumChildren()==3) {
					stBodyRet((SimpleNode) sn.jjtGetChild(2),funcao);
				}
			} else
			//Assign
			if ( sn.id == YalTreeConstants.JJTASSIGN) {
				stAssignRet(sn,funcao);
			}
		}
	}
	
	/** Given a Function Body Node tests the semantics and stores the complete Function Body information in the corresponding Map
	 * 
	 * @param node			Function Body Node of the AST
	 * @param funcao		Current function name
	 */
	public void stBody(SimpleNode node, String funcao) {
		
		//testa para todos os filhos o tipo de nodo
		for (int j=0;j<node.jjtGetNumChildren();j++) {
			SimpleNode sn = (SimpleNode) node.jjtGetChild(j);
			//While
			if ( sn.id == YalTreeConstants.JJTWHILE) {
				SimpleNode Term1=(SimpleNode) sn.jjtGetChild(0).jjtGetChild(0);
				SimpleNode Term2=(SimpleNode) sn.jjtGetChild(0).jjtGetChild(1);
				String type1=TermType(Term1,funcao);
				String type2=TermType(Term2,funcao);
				if ( (type1==null) || (type2==null) ) {
					System.out.println("Erro na linha " + sn.line + ": Erro de comparacao" );
					Erro=true;
				} else if ( (type1.compareTo("void")==0) || (type2.compareTo("void")==0) ) {
					System.out.println("Erro na linha " + sn.line + ": Comparacao de tipo void" );
					Erro=true;
				} else if ( (type1.compareTo("array")==0) || (type2.compareTo("array")==0) ) {
					System.out.println("Erro na linha " + sn.line + ": Comparacao de tipos array" );
					Erro=true;
				} else {
					if (type1.compareTo(type2)!=0) {
						System.out.println("Erro na linha " + sn.line + ": Comparacao de tipos diferentes" );
						Erro=true;
					}
				}
				//System.out.println("While com condicao de " + type1 + " com " + type2);
				stBody((SimpleNode) sn.jjtGetChild(1),funcao);
			} else
			//If
			if ( sn.id == YalTreeConstants.JJTIF) {
				SimpleNode Term1=(SimpleNode) sn.jjtGetChild(0).jjtGetChild(0);
				SimpleNode Term2=(SimpleNode) sn.jjtGetChild(0).jjtGetChild(1);
				String type1=TermType(Term1,funcao);
				String type2=TermType(Term2,funcao);
				if ( (type1==null) || (type2==null) ) {
					System.out.println("Erro na linha " + sn.line + ": Erro de comparacao" );
					Erro=true;
				} else if ( (type1.compareTo("void")==0) || (type2.compareTo("void")==0) ) {
					System.out.println("Erro na linha " + sn.line + ": Comparacao de tipo void" );
					Erro=true;
				} else if ( (type1.compareTo("array")==0) || (type2.compareTo("array")==0) ) {
					System.out.println("Erro na linha " + sn.line + ": Comparacao de tipos array" );
					Erro=true;
				} else {
					if (type1.compareTo(type2)!=0) {
						System.out.println("Erro na linha " + sn.line + ": Comparacao de tipos diferentes" );
						Erro=true;
					}
				}
				//System.out.println("If com condicao de " + type1 + " com " + type2);
				stBody((SimpleNode) sn.jjtGetChild(1),funcao);
				if (sn.jjtGetNumChildren()==3) {
					stBody((SimpleNode) sn.jjtGetChild(2),funcao);
				}
			} else
			//Assign
			if ( sn.id == YalTreeConstants.JJTASSIGN) {
				stAssign(sn,funcao);
			} else
			//Call
			if ( sn.id == YalTreeConstants.JJTCALL) {
				stCall(sn,funcao);
			}
		}
	}

	/** Given an Assign Node tests the semantics 
	 * and stores the Assign information in the corresponding Map
	 * 
	 * @param node			Assign Node of the AST
	 * @param actualFunction		Current function name
	 */
	public void stAssign(SimpleNode node, String actualFunction)
	{
		
		SimpleNode left = (SimpleNode)node.jjtGetChild(0);
		SimpleNode right = (SimpleNode)node.jjtGetChild(1);
		
		String typeL = null;
		String typeR = null;
		
		String name = left.val;
		String rval = right.val;
		
		//testa se e metodo, por exemplo "a.size"
		if (name.indexOf(".")!=-1) {
			System.out.println("Erro na linha " + left.line + 
				": Variavel esquerda nao pode ser um metodo estatico" );
			Erro=true;
			return;
		}
		
		//testa se variavel e diferente de algum parametro da funcao
		/*if (functions.get(actualFunction).parameters.contains(left.val)) {
			System.out.println("Erro na linha " + left.line + 
			": Variavel esquerda igual a parametro da funcao" );
			return;
		}*/

		Declaration dec = new Declaration(name);

		//testa variavel direita
		/*if ( (rval.indexOf(".")!=-1) && 
				(rval.substring(0,rval.indexOf(".")).compareTo("io")==0) ) {*/
		//System.out.println(rval);
		if ((rval.compareTo("io.read")==0) ){
			System.out.println("Aviso na linha " + node.line + ": Chamada de uma funcao de um modulo externo.");
			typeR="inteiro";
		} else
			
		if(right.id == YalTreeConstants.JJTARRAYSIZE) {
			typeR="array";
			dec.type=typeR;
			
			//se for inteiro
			if(isInt(rval))
			{
				dec.size=Integer.parseInt(rval);
				if (dec.size<0) {
					System.out.println("Erro na linha " + right.line + 
					": Tamanho nao pode ser negativo" );
					Erro=true;
					return;
				}
			} else { //se nao for precisamos de saber se existe a variavel
				
				//retira os sinais 
				if ((rval.indexOf("-")!=-1) || (rval.indexOf("+")!=-1)) {
					rval = rval.substring(1);
					if (rval.indexOf("-")!=-1) {
						System.out.println("Erro na linha " + right.line + 
						": Tamanho nao pode ser negativo" );
						Erro=true;
						return;
					}
				}
				String tipocomp="inteiro";
				//trunca o nome
				if (rval.indexOf(".")!=-1) {
					tipocomp="array";
					rval = rval.substring(0,rval.indexOf("."));
				}
				//existe?
				if( ( !functions.get(actualFunction).localDeclarations.containsKey(rval) ) && 
						(!globalDeclarations.containsKey(rval)) &&
						(!functions.get(actualFunction).parameters.contains(rval)) &&
						(functions.get(actualFunction).ret.name.compareTo(rval)!=0) ) {
					System.out.println("Erro na linha " + right.line + 
						": Variavel direita nao existe" );
					Erro=true;
					return;
				} else {//se existe variavel tem ser do tipo 'tipocomp'
					if ( (functions.get(actualFunction).ret.name.compareTo(rval)==0) &&
							(functions.get(actualFunction).ret.type.compareTo(tipocomp)!=0) ) {
						System.out.println("Erro na linha " + right.line + 
							": Variavel de retorno de tipo incompativel ou nao referenciada" );
						Erro=true;
						return;
					} else if ( (functions.get(actualFunction).localDeclarations.containsKey(rval)) &&
							(functions.get(actualFunction).localDeclarations.get(rval).type.compareTo(tipocomp)!=0) ) {
						System.out.println("Erro na linha " + right.line + 
							": Variavel local de tipo incompativel" );
						Erro=true;
						return;
					} else if ( (functions.get(actualFunction).parameters.contains(rval)) &&
							(functions.get(actualFunction).paramtype.get(functions.get(actualFunction).parameters.indexOf(rval)).compareTo(tipocomp)!=0) ) {
						System.out.println("Erro na linha " + right.line + 
							": Parametro de tipo incompativel" );
						Erro=true;
						return;
					} else if  (globalDeclarations.containsKey(rval) ) {
						if (globalDeclarations.get(rval).init) {
							if (globalDeclarations.get(rval).type.compareTo(tipocomp)!=0)  {
								System.out.println("Erro na linha " + right.line + 
									": Variavel global de tipo incompativel" );
								Erro=true;
								return;
							}
						} else {
							System.out.println("Erro na linha " + right.line + 
							": Variavel global nao inicializada" );
							Erro=true;
							return;
						}
					}
					
				}
			}
		} else if(right.id == YalTreeConstants.JJTOPERATION) {
			if (right.jjtGetNumChildren()>1) {
				typeR=TermType((SimpleNode) right.jjtGetChild(0),actualFunction);
				if (typeR==null)  {
					System.out.println("Erro na linha " + ((SimpleNode) right.jjtGetChild(0)).line + 
						": Erro de atribuicao");
					Erro=true;
					return;
				}
				if (typeR=="ext")  {
					return;
				}
				if (typeR.compareTo("void")==0) {
					System.out.println("Erro na linha " + ((SimpleNode) right.jjtGetChild(0)).line + 
							": Variavel direita de tipo incompativel ou inexistente (void ou null)" );
					Erro=true;
					return;
				}
				for (int i=1; i<right.jjtGetNumChildren();i++) {
					String temptype=TermType((SimpleNode) right.jjtGetChild(i),actualFunction);
					if (temptype==null) {
						System.out.println("Erro na linha " + ((SimpleNode) right.jjtGetChild(0)).line + 
							": Erro de atribuicao");
						Erro=true;
						return;
					}
					if (temptype=="ext")  {
						return;
					}
					if (temptype.compareTo("void")==0) {
						System.out.println("Erro na linha " + ((SimpleNode) right.jjtGetChild(0)).line + 
								": Variavel direita de tipo incompativel ou inexistente (void ou null)" );
						Erro=true;
						return;
					} else if (typeR.compareTo(temptype)!=0) {
						System.out.println("Erro na linha " + ((SimpleNode) right.jjtGetChild(i)).line + 
								": Operacao aritmetica de tipos diferentes" );
						Erro=true;
						return;
					} else if ((typeR.compareTo("array")==0) && (temptype.compareTo("array")==0)){
						System.out.println("Erro na linha " + ((SimpleNode) right.jjtGetChild(i)).line + 
							": Soma de arrays" );
						Erro=true;
							return;
					} else {
						typeR=TermType((SimpleNode) right.jjtGetChild(i),actualFunction);
					}
				}
			} else {
				typeR=TermType((SimpleNode) right.jjtGetChild(0), actualFunction);
				if (typeR==null) {
					System.out.println("Erro na linha " + ((SimpleNode) right.jjtGetChild(0)).line + 
						": Erro de atribuicao");
					Erro=true;
					return;
				}
				if (typeR=="ext")  {
					return;
				}
				if (typeR.compareTo("void")==0) {
					System.out.println("Erro na linha " + ((SimpleNode) right.jjtGetChild(0)).line + 
							": Variavel direita de tipo incompativel ou inexistente (void ou null)" );
					Erro=true;
					return;
				}
			}
		}
		
		//testa se a variavel esquerda existe
		if ( (functions.get(actualFunction).ret.name.compareTo(name)==0) ||
				(functions.get(actualFunction).parameters.contains(name)) ||
				(functions.get(actualFunction).localDeclarations.containsKey(name)) || 
				(globalDeclarations.containsKey(name))) {
			
			//VARIAVEL EXISTE EM RETORNO
			if (functions.get(actualFunction).ret.name.compareTo(name)==0) {
				if (functions.get(actualFunction).ret.type.compareTo("undefined")==0) {
					functions.get(actualFunction).ret.type=typeR;
					System.out.println("Retorno da funcao definido como " + typeR);
				}
				typeL=functions.get(actualFunction).ret.type;
			}
			
			//VARIAVEL EXISTE COMO PARAMETRO
			else if (functions.get(actualFunction).parameters.contains(name)) {
				typeL=functions.get(actualFunction).paramtype.get(functions.get(actualFunction).parameters.indexOf(name)); 
			}
			
			//VARIAVEL EXISTE EM LOCAL
			else if(functions.get(actualFunction).localDeclarations.containsKey(name))
			{
				typeL = functions.get(actualFunction).localDeclarations.get(name).type;
			}
			
			// VARIAVEL EXISTE EM GLOBAL
			else if(globalDeclarations.containsKey(name))
			{
				if (globalDeclarations.get(name).init) {
					typeL = globalDeclarations.get(name).type;
				} else {
					typeL = typeR;
					if (typeL.compareTo("inteiro")==0) {
						globalDeclarations.get(name).initInt(0);
					} else {
						globalDeclarations.get(name).initArray(1);
					}
				}
			}
			
			//System.out.println("Tipo left: " + typeL);
			//se tem indice so pode receber inteiro apesar de ser um array
			if (left.jjtGetNumChildren()>0) {
				if (typeL.compareTo("inteiro")==0) {
					System.out.println("Erro na linha " + left.line + 
						": Nao pode aceder a uma posicao de uma variavel tipo inteiro" );
					Erro=true;
					return;
				} else {
					typeL="inteiro";
				}
				//System.out.println("Tipo left depois: " + typeL);
			}
			
		} else { //nao existe
			if (left.jjtGetNumChildren()>0) { // se for variavel com indice
				System.out.println("Erro na linha " + left.line + 
					": Nao pode aceder a uma posicao de uma variavel nao declarada" );
				Erro=true;
				return;
			} else {
				if (typeR.compareTo("undefined")!=0) {
					typeL=typeR;
					dec.type=typeL;
					functions.get(actualFunction).localDeclarations.put(name, dec);
					System.out.println("Declaracao local da variavel '" + name + "' do tipo " + typeL);	
				} else {
					return;
				}
			}
		}
		
		if (typeL.compareTo(typeR)==0) {
			//System.out.println("Tipos compativeis na atribuicao");
		} else {
			System.out.println("Erro na linha " + left.line + 
			": Tipos de variaveis incompativeis" );
			Erro=true;
		}

	}
	
	/** Given an Assign Node tests the semantics and stores the Assign information 
	 * relative to the return of the given function in the corresponding Map
	 * 
	 * @param node			Assign Node of the AST
	 * @param actualFunction		Current function name
	 */
	public void stAssignRet(SimpleNode node, String actualFunction)
	{
		SimpleNode left = (SimpleNode)node.jjtGetChild(0);
		SimpleNode right = (SimpleNode)node.jjtGetChild(1);
		
		String typeL = null;
		String typeR = "inteiro";
		
		String name = left.val;
		String rval = right.val;
		
		//testa se e metodo, por exemplo "a.size"
		if (name.indexOf(".")!=-1) {
			return;
		}

		if ((rval.compareTo("io.read")==0) ){
			typeR="inteiro";
		} else if(right.id == YalTreeConstants.JJTARRAYSIZE) {
			typeR="array";
		} else if(right.id == YalTreeConstants.JJTOPERATION) {
			if (right.jjtGetNumChildren()>1) {
				typeR="inteiro";
			} else {
				SimpleNode term = (SimpleNode) right.jjtGetChild(0);
				String termval = term.val;
				
				//retira os sinais 
				if ((termval.indexOf("-")!=-1) || (termval.indexOf("+")!=-1)) {
					termval = termval.substring(1);
				}
				
				if ( term.id==YalTreeConstants.JJTINTEGER) {
					typeR="inteiro";
				} else if ( term.id==YalTreeConstants.JJTCALL) {
					//verifica se e um modulo externo
					if ( (termval.indexOf(".")!=-1) || (termval.compareTo("bypassnode")==0) ) {
						termval = termval.substring(termval.indexOf(".")+1);
						typeR="inteiro";
					} else {
						Function func = functions.get(termval);
						if (func!=null) {
							typeR=func.ret.type;
						} else {
							typeR="undefined";
						}
					}
					
				} else if ( term.id==YalTreeConstants.JJTACCESS) {
					
					if (term.jjtGetNumChildren()>0) {
						typeR="inteiro";
					} else
					
					//VARIAVEL EXISTE EM RETORNO
					if (functions.get(actualFunction).ret.name.compareTo(termval)==0) {
						typeR=functions.get(actualFunction).ret.type;
					}
					
					//VARIAVEL EXISTE COMO PARAMETRO
					else if (functions.get(actualFunction).parameters.contains(termval)) {
						typeR=functions.get(actualFunction).paramtype.get(functions.get(actualFunction).parameters.indexOf(termval));

					}
					
					//VARIAVEL EXISTE EM LOCAL
					else if(tempDeclarations.containsKey(termval))
					{
						typeR=tempDeclarations.get( termval );
					}
					
					// VARIAVEL EXISTE EM GLOBAL
					else if(globalDeclarations.containsKey(termval))
					{
						typeR = globalDeclarations.get(termval).type;
					} else {
						typeR="undefined";
					}
				}
			}
		}
		
		//testa se a variavel esquerda existe
		if ( (functions.get(actualFunction).ret.name.compareTo(name)==0) ||
				(functions.get(actualFunction).parameters.contains(name)) ||
				(tempDeclarations.containsKey(name)) || 
				(globalDeclarations.containsKey(name))) {
			
			//VARIAVEL EXISTE EM RETORNO
			if (functions.get(actualFunction).ret.name.compareTo(name)==0) {
				if (functions.get(actualFunction).ret.type.compareTo("undefined")==0) {
					functions.get(actualFunction).ret.type=typeR;
					//System.out.println("Retorno da funcao definido como " + typeR);
				}
				typeL=functions.get(actualFunction).ret.type;
			}
			
			//VARIAVEL EXISTE COMO PARAMETRO
			else if (functions.get(actualFunction).parameters.contains(name)) {
				typeL=functions.get(actualFunction).paramtype.get(functions.get(actualFunction).parameters.indexOf(name)); 
			}
			
			//VARIAVEL EXISTE EM LOCAL
			else if(tempDeclarations.containsKey(name))
			{
				typeL = tempDeclarations.get(name);
			}
			
			// VARIAVEL EXISTE EM GLOBAL
			else if(globalDeclarations.containsKey(name))
			{
				typeL = globalDeclarations.get(name).type;
			}
			
			//se tem indice so pode receber inteiro apesar de ser um array
			if (left.jjtGetNumChildren()>0) {
				if (typeL.compareTo("inteiro")==0) {
					return;
				} else {
					typeL="inteiro";
				}
			}
			
		} else { //nao existe
			if (left.jjtGetNumChildren()>0) { // se for variavel com indice
				return;
			} else {
				typeL=typeR;
				tempDeclarations.put(name, typeL);
			}
		}

	}
	
	/** Given a Call Node tests the semantics 
	 * and stores the Call information in the corresponding Map
	 * 
	 * @param node			Call Node of the AST
	 * @param actualFunction		Current function name
	 */
	public void stCall(SimpleNode node, String actualFunction) {
		String nomefuncao = node.val;
		//testa se tem um ponto e testa se e o nosso modulo
		if (nomefuncao.indexOf(".")!=-1) {
			String mod = nomefuncao.substring(0,nomefuncao.indexOf("."));
			nomefuncao = nomefuncao.substring(nomefuncao.indexOf(".")+1);
			if ( (mod.compareTo(module)!=0) || (mod.compareTo("io")==0) ) {
				System.out.println("Aviso na linha " + node.line + ": Chamada de uma funcao" +
						" de um modulo externo.");
				return;
			}
		}
		
		//obtem funcao da hash, se nao existir da erro pois nao foi declarada
		Function func = functions.get(nomefuncao);
		if (func==null) {
			System.out.println("Erro na linha " + node.line + 
			": Funcao nao declarada" );
			Erro=true;
			return;
		} else { 
			//se existir testa se tem retorno 
			//e se o numero e tipo de parametros bate certo
			System.out.println("Chamada a funcao: " + nomefuncao);
			//primeiro o retorno
			if (func.ret.type.compareTo("void")!=0) {
				System.out.println("Erro na linha " + node.line + 
				": Funcao tem retorno" );
				Erro=true;
				return;
			}
			//depois o numero de parametros
			if (node.jjtGetNumChildren()>0) { //se forem dados paramentros
				SimpleNode sn = (SimpleNode) node.jjtGetChild(0);
				if (func.parameters.size()!=sn.jjtGetNumChildren()) {
					System.out.println("Erro na linha " + node.line + 
					": Numero de argumentos errado" );
					Erro=true;
					return;
				} else {
					//depois o tipo
					for (int i=0; i<sn.jjtGetNumChildren();i++) {
						String tempval = sn.jjtGetChildVal(i);
						String tipocomp="void";
						if (isInt(tempval)) {
							tipocomp="inteiro";
						} else {
							//verificamos a sua existencia e retiramos o tipo
							if (functions.get(actualFunction).ret.name.compareTo(tempval)==0) {
								tipocomp=functions.get(actualFunction).ret.type;
							} else if (functions.get(actualFunction).localDeclarations.containsKey(tempval)) {
								tipocomp=functions.get(actualFunction).localDeclarations.get(tempval).type;
							} else if (functions.get(actualFunction).parameters.contains(tempval)) {
								tipocomp=functions.get(actualFunction).paramtype.get(functions.get(actualFunction).parameters.indexOf(tempval));
							} else if (globalDeclarations.containsKey(tempval)) {
								tipocomp=globalDeclarations.get(tempval).type;
							} else {
								System.out.println("Erro na linha " + node.line + 
										": Variavel '" + tempval + "' nao declarada" );
								Erro=true;
							}
						}
						//para cada parametro comparamos 
						//os tipos dados com os que a funcao espera
						if (tipocomp.compareTo("void")!=0) {
							if (func.paramtype.get(i).compareTo(tipocomp)!=0) {
								System.out.println("Erro na linha " + node.line + 
									": Parametro incompativel" );
								Erro=true;
							}
						}
					}
				}
			} else { 
				//se nao forem dados parametros
				//testamos se a funcao pede parametros
				if (func.parameters.size()>0) {
					System.out.println("Erro na linha " + node.line + 
					": Numero de argumentos errado" );
					Erro=true;
					return;
				}
			}
		}
	}

	//cria tabela de simbolos e faz analise semantica
	/** Creates the Symbol Table and Runs a Semantic Analisys
	 * 
	 * @param node			Root Node of the AST
	 */
	public void createSymbolTable(SimpleNode node) {
		//primeiro as declaracoes globais e funcoes
		declareGlobalsFunctions(node);
		//depois o corpo das funcoes
		createTable(node);
	}
	
	/** Given a Root Node Creates the Symbol Table and Runs a Semantic Analisys
	 * relative to the module name, global declarations, 
	 * function names, its parameters and return
	 * 
	 * @param node			Root Node of the AST
	 */
	public void declareGlobalsFunctions(SimpleNode node) {
		for(int i=0; i< node.jjtGetNumChildren(); i++)
		{
			declareGlobalsFunctions((SimpleNode) node.jjtGetChild(i));
		}
		
		// MODULE
		if(node.id == YalTreeConstants.JJTMODULE)
		{
			module=node.val;
		} else
		
		// DECLARATION
		if(node.id == YalTreeConstants.JJTDECLARATION)
		{
			stDeclaration(node);
		} else
		
		// FUNCTION
		if(node.id == YalTreeConstants.JJTFUNCTION)
		{
			stFunction(node);
		}
	}
	
	/** Given a Root Node Creates the Symbol Table and Runs a Semantic Analisys
	 * relative to the each function body
	 * 
	 * @param node			Root Node of the AST
	 */
	public void createTable(SimpleNode node) {
		for(int i=0; i< node.jjtGetNumChildren(); i++)
		{
			createTable((SimpleNode) node.jjtGetChild(i));
		}
		
		// FUNCTION BODY
		if(node.id == YalTreeConstants.JJTFUNCTION)
		{
			stFunctionBody(node);
		}
	}
}
