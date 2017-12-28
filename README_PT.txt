
------------------------------README---------------------------------------

Ap�s a cria��o da AST, a primeira opera��o que o programa faz � obter o nome das fun��es e o seu
retorno, dado que estas podem ser declaradas e chamadas numa ordem n�o especifica como no 'Java'
(e ao contr�rio do 'C'). De seguida, corre outra vez e constr�i a tabela de s�mbolos ao mesmo tempo 
que faz a an�lise sem�ntica. Finalmente corre uma �ltima vez para fazer a gera��o de c�digo.

O compilador pode ser chamado usando o comando java Yal <direct�rio e nome do ficheiro>.yal sendo este
ultimo opcional, dado que se n�o for referido nenhum ficheiro, o programa ir� procurar ficheiros ".yal"
no directorio corrente + "\Yal". Ou seja, se correr o programa na pasta "C:\Compiladores", este vai
automaticamente listar na consola os ficheiros existentes na pasta "C:\Compiladores\Yal". Se a pasta
estiver vazia o programa pede para manualmente inserir um c�digo Yal.

O compilador corre por fases: primeiro const�i a �rvore e faz a an�lise sint�tica, depois constr�i a tabela 
de simbolos ao mesmo tempo que efectua a an�lise sem�ntica, e por ultimo procede para a gera��o de c�digo.

O resultado de cada fase dita o comportamento das restantes, isto �, se por alguma raz�o existirem erros
numa fase, as seguites s�o descartadas.

---------------------------------------------------------------------

**ERROS SINT�TICOS:

Na constru��o da �rvore se houver um erro sint�tico o compilador para a execu��o e imprime uma mensagem
identificando onde e que tipo de erro. Em baixo podemos ver um exemplo:

Exception in thread "main" ParseException: Encountered "something" at line 7, column 1.
Was expecting one of:
    "=" ...
    ";" ...
    "[" ...

**AN�LISE SEM�NTICA:

Declara��es Globais:

	Uma vari�vel global n�o pode ser declarada igual a ela pr�pria.
	Ex:
		a=a; <- erro
	
	Nas declara��es globais uma vari�vel pode ser declarada sem ser atribuido um valor ou tipo,
	ficando como integer por defeito, a n�o ser que o utilizador obrigue esta a ser um array com '[]'.
	A primeira atribui��o define o tipo dessa vari�vel.
	Ex: 
		a; <- por defeito � integer
		a=[10]; <- primeira atribui��o, passa a ser um vector com 10 posi��es
		a=1; <- todas as posi��es t�m o valor 1.
		b[]; <- nao inicializado, mas obrigado a ser array
		b=1; <- erro
		b=[5]; <- ok
		
	Nas declara��es globais se � feita uma atribui��o a uma vari�vel que j� existe essa 
	so � aceite se for do tipo inteiro e for atribuido outro inteiro 
	senao � uma redeclar��o de um vector.
	Ex:
		a=1; 
		b=[10];
		a=2; <- ok
		b=[5]; <- erro
		
Fun��es:
		
	O tipo de retorno da fun��o � definido quando � feita a primeira atribui��o � vari�vel de retorno,
	a n�o ser que o utilizador obrigue o retorno a ser um array com '[]'.
	Ex:
		function a=funcao1() {
			a=1; <- primeira atribui��o, vari�vel integer
			a[]=3; <- erro
		}
		
		function b[]=funcao2() {
			b=1; <- erro, de acordo com os slides 'yal-intro.pdf' - 'particularidades 1', p�gina 5,
				esta opera��o s� � permitida nas declarac�es globais.
			c=[10];
			b=c; <- ok
			b[2]=1; <- ok
		}
	
	Uma fun��o com retorno tem de ter esse retorno especificado 
	pelo menos uma vez no corpo da fun��o.
	Ex:
		function funcao1(a,b) {} <- ok, funcao void
		function c=funcao2(x,y) {} <- erro, nao se sabe o que 'c' retorna
		
	Uma fun��o n�o pode ser declarada mais de uma vez.
	Ex:
		function f() {}
		function f() {} <- erro
		
	A lista de par�metros de uma fun��o n�o pode ter par�metros repetidos 
	ou ter um par�metro igual � variavel de retorno.
	Ex:
		function f1(a,a) {} <- erro
		function r=f2(a,r) {} <- erro
		function r=f3(a,b) {} <- ok
		
	Na chamada de uma fun��o s�o testados se o numero e tipo 
	de argumentos dados � correcto,	assim como a sua exist�ncia, 
	tanto dos parametros como da pr�pria fun��o.
		
Ciclos, Condi��es e Opera��es Aritm�ticas:
		
	Nas express�es dos ciclos 'while' ou 'if' e nas opera��es aritm�ticas 
	s� s�o aceites compara��es ou opera��es entre inteiros ou 
	vectores e funcoes cujo resultado seja um inteiro.
	Ex:
		function f1[]=funcao1() {
			v=[10];
			f1=v;
		}
		
		function f2=funcao2(x,y,z[]) {
			soma = x+y; <-ok
			sub = y-z; <- erro
			mult = x*z[0]; <- ok
			div = y/funcao1(); <-erro
			
			f2=1;
		}
		
		function main() {
			a=2;
			b[0]=3;
		
			while (a<1) {...} <- ok
			if (a==funcao()) {...} <- ok
			while (a>b) {...} <- erro
			if (a!=b[0]) {...} <- ok
		}
		
		Numa express�o condicional n�o podemos ter a mesma vari�vel com dois tipos poss�veis 
		de retorno como est� especificado nos exemplos de Yal.
		Ex:
			function m=f(a) 
			{
				if(a>10) {
					m=[a];
				} else {
					m=2;
				}
			}

Vari�veis locais e atribui��es:

	Um array n�o pode ser atribuido um inteiro.
	Um array para ser acedido pode estar declarado mas tem de ser inicializado.
	Um inteiro n�o pode:
		-ser atribuido a uma variavel tipo array.
		-ser atribuido a uma posi��o de um array n�o declarado.
		-ser atribuido a uma posi��o de um array declarado mas n�o inicializado.
		-passar dos limites maximo e minimos permitidos.
	Ex:
		a[];
		b=0;
		
		b=a; <- erro
		a[0] = b; <- erro
		
		a=b; <- erro
		b=a[1]; <- erro
		
		a=[10]; <- inicializamos
		b=a[1]; <- ok
		
		c=98765432101234567890; <- erro
		
	Num array o valor ou vari�vel dada no tamanho tem de existir, 
	ser do tipo inteiro ou ser um array com um m�todo que devolva um inteiro (p.ex 'size'),
	estar dentro dos limites e ser maior que zero.
	Ex:
		v=[10];
		a=1;
		
		b=[c]; <- erro
		b=[-2]; <- erro
		b=[v]; <- erro
		
		b=[v.size]; <- ok
		b=v; <- ok

	Numa atribui��o, a vari�vel esquerda n�o pode ser um m�todo, ou seja,
	n�o pode ser atribuido um valor a um m�todo de uma vari�vel.
	Ex:
		a.size = 1 <- erro
		
	Todas as vari�veis com ou sem indice s�o testadas, tanto para o indice 
	como para a vari�vel, a validade. Ou seja, se existe e/ou se o tipo � compativel. 
	Se nao for diz qual a origem da vari�vel que deu erro (parametro, retorno, local, global)
	Se uma vari�vel contiver "." significa que esta a ser chamado um metodo
	(p/ex var.size) e portanto verifica se e array.
	
Modulos externos:

	� imprimido um aviso sempre que � chamado um m�todo de um modulo externo,
	e � considerado que esse m�todo devolve um inteiro.
	
	Por defeito, � considerado que todos os m�todos da classe 'io' 
	devolvem um inteiro, com excepcao ao 'io.print' e 'io.println'
	que devolvem 'void'.


**REPRESENTA��O INTERM�DIA:

Ap�s a constru��o da �rvore, � impresso na consola os n�s correspondentes com o respectivo valor,
em que os filhos s�o posicionados ligeiramente mais � frente da posi��o anterior dos seus pais, 
para poderem ser identificados com tal.

Exemplo:
O m�dulo seguinte:

module programa1 {

	data=[100]; // vector of 100 integers

	function a=det(d[]) {
		i=0;
		while(i<d[0]) {
			i=i+1;
		}
	}

	function main() {
		a=det(data);

		io.println("ok: ",a); 
	}
}

gera a AST:

Root
 Module programa1
 Declaration =
  Element data
  ArraySize 100
 Function det
  FunctionReturn a
  Varlist
   Element d[]
  Body
   Assign =
    Access i
    Integer 0
   While
    Condition <
     Access i
     Access d
      Index 0
    Body
     Assign =
      Access i
      Operation +
       Access i
       Integer 1
 Function main
  Body
   Assign =
    Access a
    Call det
     ArgumentList
      Argument data
   Call io.println
    ArgumentList
     Argument "ok: "
     Argument a

**GERA��O DE C�DIGO:

Quanto � gera��o de c�digo, ap�s as fases anteriores terem sido execut�das com sucesso, 
� gerado um ficheiro .j correspondente. Foi de seguida executado o jasmin 2.4 em linux 
nos ficheiros .j gerados pelo compilador obtendo o ficheiro .class correspondente. 
Comparando com os resultados dados, n�o foram identificados erros.

**OPTIMIZA��ES E ALOCA��O DE REGISTOS:

Na parte das optimiza��es, o programa tenta ser o mais eficiente e com menor custo de mem�ria possivel, 
para isso foram implementadas as instru��es .limit stack e .limit locals, para melhor gerir a mem�ria,
em que s�o efectivamente contadas as vari�veis e respectivas instru��es que afectam a stack.

A gera��o de c�digo est� separada por fun��es de modo a que o c�digo seja mais f�cil de interpretar
e tamb�m intuitivo. S�o feitas fun��es para alterar a stack e aproveitar registos da JVM 
de maneira a tornar o programa o mais optimizado poss�vel.

Por exemplo, podiamos ler uma constante sempre com 'ldc', mas isso seria um prov�vel desperd�cio.
Por isso se estiver entre 0 e 5 usamos o iconst_'valor' (iconst_2, por exemplo), se estiver entre -127 e 127
usa-se o bipush, sen�o o ldc. O mesmo acontece com os loads e stores. De acordo com a posi��o da vari�vel
na stack, de 0 a 3, usamos o iload_'valor' ou o istore_'valor' sempre que poss�vel. Tamb�m se tivermos
uma opera��o de incremento, tipo 'i=i+1', usamos o iinc. Numa an�lise ao c�digo ou � documenta��o disponivel
isto poder� ser visto com clareza.

**GLOBALMENTE:

Foi constru�do um compilador robusto, feito a pensar em efic�cia e num resultado �ptimo.
As optimiza��es s�o feitas automaticamente sem nenhum comando adicional. Isto poder� ser um defeito do
compilador j� que restringe e obriga o utilizador ao mesmo tipo de execu��o.
O jasmin n�o foi incorporado no compilador e tem de ser corrido � parte ap�s a execu��o do nosso programa
e gera��o do ficheiro .j correspondente.
