/**
   Main class of the parser.

   Author: João M. P. Cardoso
   Institution: University of Porto, Faculty of Engineering, Porto, Portugal
   Date: January 2011
*/

/*
  This is a first parser example. 
  It outputs simple errors neither referring the line and column of the errors nor
  the expected token.
*/

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class FirstParser  {

   public static void main(String args[]){

         BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

         // Lexical analysis
         LexicalAnalysis lex = new LexicalAnalysis();
         boolean sucessLex=lex.doAnalysis(in);
         if(sucessLex)
              System.out.println("Lexical analysis ended with sucess!");
         else {
              System.out.println("Lexical error!");
              return;
         }
         lex.print();

         // Syntactic Analysis
         SyntacticAnalysis syn = new SyntacticAnalysis();
         syn.t = lex.firstToken;
         boolean successSyn = syn.S();
         if(successSyn)
            System.out.println("Syntactical analysis ended with sucess!");
         else
            System.out.println("Syntactical analysis ended without sucess!");

    }
}

