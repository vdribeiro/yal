/**
   Class used for the syntatic analysis. It implements a top-down parser for a LL(1)
   grammar. The grammar consists of the following rules:
   S ::= E <SEMICOLON>
   E ::=  T [<OP> E]
   T ::= <IDENTIFIER> | <INTEGER>
   
   The Syntactic Analysis receives the sequence of Tokens produced by the Lexical Analysis
   according to the token definitions:
   IDENTIFIER = [a-z][a-z0-9]*
   INTEGER = [0-9]+
   OP =  "+"
   SEMICOLON = ";"

   Author: João M. P. Cardoso
   Institution: University of Porto, Faculty of Engineering, Porto, Portugal
   Date: January 2011
*/

import java.io.IOException;

public class SyntacticAnalysis {

  public Token t;

         /**
            Method to perform the lexical analysis
         */
       public boolean S() {
            boolean e_sucess = E();

            if(t == null) {
              System.out.println("Syntactic error: expected a \";\"");
              return false;
            }

            if(t.type == TokenTypes.SEMICOLON) {
                t = t.next; // consume
            } else {
              System.out.println("Syntactic error: expected a \";\"");
              return false;
            }

            // the code below could be put after retunring from S()
            if(t != null)  {
              System.out.println("Syntactic error: token after \";\"");
              return false;
            }

            return e_sucess;
       }

       public boolean E() {
            boolean t_success=T();

            if(t != null)
            if(t.type == TokenTypes.OP) {
                t = t.next; // consume
                return E();
            }

            return t_success;
       }

        public boolean T() {
            if(t.type == TokenTypes.INTEGER)
                t = t.next; // consume
            else if(t.type == TokenTypes.IDENTIFIER)
                t = t.next; // consume
            else {
              System.out.println("Syntatic error: expected a token of type IDENTIFIER or INTEGER");
              return false;
            }
            return true;
       }

}