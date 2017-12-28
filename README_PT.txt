
------------------------------README---------------------------------------

Após a criação da AST, a primeira operação que o programa faz é obter o nome das funções e o seu
retorno, dado que estas podem ser declaradas e chamadas numa ordem não especifica como no 'Java'
(e ao contrário do 'C'). De seguida, corre outra vez e constrói a tabela de símbolos ao mesmo tempo 
que faz a análise semântica. Finalmente corre uma última vez para fazer a geração de código.

O compilador pode ser chamado usando o comando java Yal <directório e nome do ficheiro>.yal sendo este
ultimo opcional, dado que se não for referido nenhum ficheiro, o programa irá procurar ficheiros ".yal"
no directorio corrente + "\Yal". Ou seja, se correr o programa na pasta "C:\Compiladores", este vai
automaticamente listar na consola os ficheiros existentes na pasta "C:\Compiladores\Yal". Se a pasta
estiver vazia o programa pede para manualmente inserir um código Yal.

O compilador corre por fases: primeiro constói a árvore e faz a análise sintática, depois constrói a tabela 
de simbolos ao mesmo tempo que efectua a análise semântica, e por ultimo procede para a geração de código.

O resultado de cada fase dita o comportamento das restantes, isto é, se por alguma razão existirem erros
numa fase, as seguites são descartadas.

---------------------------------------------------------------------

**ERROS SINTÁTICOS:

Na construção da árvore se houver um erro sintático o compilador para a execução e imprime uma mensagem
identificando onde e que tipo de erro. Em baixo podemos ver um exemplo:

Exception in thread "main" ParseException: Encountered "something" at line 7, column 1.
Was expecting one of:
    "=" ...
    ";" ...
    "[" ...

**ANÁLISE SEMÂNTICA:

Declarações Globais:

	Uma variável global não pode ser declarada igual a ela própria.
	Ex:
		a=a; <- erro
	
	Nas declarações globais uma variável pode ser declarada sem ser atribuido um valor ou tipo,
	ficando como integer por defeito, a não ser que o utilizador obrigue esta a ser um array com '[]'.
	A primeira atribuição define o tipo dessa variável.
	Ex: 
		a; <- por defeito é integer
		a=[10]; <- primeira atribuição, passa a ser um vector com 10 posições
		a=1; <- todas as posições têm o valor 1.
		b[]; <- nao inicializado, mas obrigado a ser array
		b=1; <- erro
		b=[5]; <- ok
		
	Nas declarações globais se é feita uma atribuição a uma variável que já existe essa 
	so é aceite se for do tipo inteiro e for atribuido outro inteiro 
	senao é uma redeclarção de um vector.
	Ex:
		a=1; 
		b=[10];
		a=2; <- ok
		b=[5]; <- erro
		
Funções:
		
	O tipo de retorno da função é definido quando é feita a primeira atribuição à variável de retorno,
	a não ser que o utilizador obrigue o retorno a ser um array com '[]'.
	Ex:
		function a=funcao1() {
			a=1; <- primeira atribuição, variável integer
			a[]=3; <- erro
		}
		
		function b[]=funcao2() {
			b=1; <- erro, de acordo com os slides 'yal-intro.pdf' - 'particularidades 1', página 5,
				esta operação só é permitida nas declaracões globais.
			c=[10];
			b=c; <- ok
			b[2]=1; <- ok
		}
	
	Uma função com retorno tem de ter esse retorno especificado 
	pelo menos uma vez no corpo da função.
	Ex:
		function funcao1(a,b) {} <- ok, funcao void
		function c=funcao2(x,y) {} <- erro, nao se sabe o que 'c' retorna
		
	Uma função não pode ser declarada mais de uma vez.
	Ex:
		function f() {}
		function f() {} <- erro
		
	A lista de parâmetros de uma função não pode ter parâmetros repetidos 
	ou ter um parâmetro igual à variavel de retorno.
	Ex:
		function f1(a,a) {} <- erro
		function r=f2(a,r) {} <- erro
		function r=f3(a,b) {} <- ok
		
	Na chamada de uma função são testados se o numero e tipo 
	de argumentos dados é correcto,	assim como a sua existência, 
	tanto dos parametros como da própria função.
		
Ciclos, Condições e Operações Aritméticas:
		
	Nas expressões dos ciclos 'while' ou 'if' e nas operações aritméticas 
	só são aceites comparações ou operações entre inteiros ou 
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
		
		Numa expressão condicional não podemos ter a mesma variável com dois tipos possíveis 
		de retorno como está especificado nos exemplos de Yal.
		Ex:
			function m=f(a) 
			{
				if(a>10) {
					m=[a];
				} else {
					m=2;
				}
			}

Variáveis locais e atribuições:

	Um array não pode ser atribuido um inteiro.
	Um array para ser acedido pode estar declarado mas tem de ser inicializado.
	Um inteiro não pode:
		-ser atribuido a uma variavel tipo array.
		-ser atribuido a uma posição de um array não declarado.
		-ser atribuido a uma posição de um array declarado mas não inicializado.
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
		
	Num array o valor ou variável dada no tamanho tem de existir, 
	ser do tipo inteiro ou ser um array com um método que devolva um inteiro (p.ex 'size'),
	estar dentro dos limites e ser maior que zero.
	Ex:
		v=[10];
		a=1;
		
		b=[c]; <- erro
		b=[-2]; <- erro
		b=[v]; <- erro
		
		b=[v.size]; <- ok
		b=v; <- ok

	Numa atribuição, a variável esquerda não pode ser um método, ou seja,
	não pode ser atribuido um valor a um método de uma variável.
	Ex:
		a.size = 1 <- erro
		
	Todas as variáveis com ou sem indice são testadas, tanto para o indice 
	como para a variável, a validade. Ou seja, se existe e/ou se o tipo é compativel. 
	Se nao for diz qual a origem da variável que deu erro (parametro, retorno, local, global)
	Se uma variável contiver "." significa que esta a ser chamado um metodo
	(p/ex var.size) e portanto verifica se e array.
	
Modulos externos:

	É imprimido um aviso sempre que é chamado um método de um modulo externo,
	e é considerado que esse método devolve um inteiro.
	
	Por defeito, é considerado que todos os métodos da classe 'io' 
	devolvem um inteiro, com excepcao ao 'io.print' e 'io.println'
	que devolvem 'void'.


**REPRESENTAÇÃO INTERMÉDIA:

Após a construção da árvore, é impresso na consola os nós correspondentes com o respectivo valor,
em que os filhos são posicionados ligeiramente mais à frente da posição anterior dos seus pais, 
para poderem ser identificados com tal.

Exemplo:
O módulo seguinte:

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

**GERAÇÃO DE CÓDIGO:

Quanto à geração de código, após as fases anteriores terem sido executádas com sucesso, 
é gerado um ficheiro .j correspondente. Foi de seguida executado o jasmin 2.4 em linux 
nos ficheiros .j gerados pelo compilador obtendo o ficheiro .class correspondente. 
Comparando com os resultados dados, não foram identificados erros.

**OPTIMIZAÇÕES E ALOCAÇÃO DE REGISTOS:

Na parte das optimizações, o programa tenta ser o mais eficiente e com menor custo de memória possivel, 
para isso foram implementadas as instruções .limit stack e .limit locals, para melhor gerir a memória,
em que são efectivamente contadas as variáveis e respectivas instruções que afectam a stack.

A geração de código está separada por funções de modo a que o código seja mais fácil de interpretar
e também intuitivo. São feitas funções para alterar a stack e aproveitar registos da JVM 
de maneira a tornar o programa o mais optimizado possível.

Por exemplo, podiamos ler uma constante sempre com 'ldc', mas isso seria um provável desperdício.
Por isso se estiver entre 0 e 5 usamos o iconst_'valor' (iconst_2, por exemplo), se estiver entre -127 e 127
usa-se o bipush, senão o ldc. O mesmo acontece com os loads e stores. De acordo com a posição da variável
na stack, de 0 a 3, usamos o iload_'valor' ou o istore_'valor' sempre que possível. Também se tivermos
uma operação de incremento, tipo 'i=i+1', usamos o iinc. Numa análise ao código ou à documentação disponivel
isto poderá ser visto com clareza.

**GLOBALMENTE:

Foi construído um compilador robusto, feito a pensar em eficácia e num resultado óptimo.
As optimizações são feitas automaticamente sem nenhum comando adicional. Isto poderá ser um defeito do
compilador já que restringe e obriga o utilizador ao mesmo tipo de execução.
O jasmin não foi incorporado no compilador e tem de ser corrido à parte após a execução do nosso programa
e geração do ficheiro .j correspondente.
