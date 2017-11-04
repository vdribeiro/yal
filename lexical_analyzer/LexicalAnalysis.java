/**
   Class used for the lexical analysis.

   The Lexical Analysis creates the sequence of Tokens
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

public class LexicalAnalysis {

  public Token firstToken = null;

         /**
            Method to perform the lexical analysis
         */
       public boolean doAnalysis(java.io.BufferedReader in) {
              int nextState;
              int currentState = 1;

              StringBuffer image = new StringBuffer();

              Token previousToken = null;
              Token currentToken = null;

              try{

              int ch1 = in.read();
              // if you consider skip chars include:
              // while(skip(ch1)) ch1 = in.read();

              while(ch1 != (int) '\n' && ch1 != (int) '\r' ) {   // windows needs the two

                nextState = trans(currentState, ch1);
                // if you consider skip chars include:
                // while(skip(ch1)) ch1 = in.read();

                if(nextState==0 && finalState(currentState))  {
                  currentToken = new Token(image.toString(), currentState); // assuming the number of state identifies the type
                  nextState = 1; // start state of the Automata
                  image = new StringBuffer();
                  if(previousToken != null) previousToken.next = currentToken;
                  else firstToken = currentToken;
                } else {
                  image.append((char) ch1);
                  ch1 = in.read();
                }

                currentState = nextState;
                previousToken = currentToken;
              }

              if(finalState(currentState))  {
                  currentToken = new Token(image.toString(), currentState); // assuming the number of state identifies the type
                  if(previousToken != null) previousToken.next = currentToken;
                  else firstToken = currentToken;
                }
              } catch (IOException e) {
                // code for the exception should be here
              }

              if(finalState(currentState))
                return true;
              else
                return false;
       }

       /**
          The method that defines the DFA transtions.
          State code:
          IDENTIFIER = 2;
          OP = 3;
          SEMICOLON = 4;
          INTEGER = 5;
          START = 1;
          ERROR = 0;
       */
       public int trans(int State, int ch) {
         if (State == 1) {    // a switch would be better
           if(ch >= (int) 'a' && ch <= (int)'z') return 2;
           else if(ch == (int)';') return 4;
           else if(ch == (int)'+') return 3;
           else if(ch >= (int)'0' && ch <= (int)'9') return 5;
           else return 0;
         } else if (State == 2) {
           if(ch >= (int)'a' && ch <= (int)'z') return 2;
           else if(ch >= (int)'0' && ch <= (int)'9') return 2;
           else return 0;
         } else if (State == 5) {
           if(ch >= (int)'0' && ch <= (int)'9') return 5;
           else return 0;
         } else return 0;
       }

       /**
          Method that return if a given state is a final state or not.
       */
       public boolean finalState(int State) {
              // in the DFA the final states are: 2, 3, 4, and 5
              // 1 is the initial state and 0 is the error (dead) state

         if(State == 2 || State == 3 || State == 4 || State == 5) return true;  // a switch would be better
         else return false;
       }

       /**
          Method to skip some symbols in the input.
          In the case we want a parser skiping some special symbols.
          For this example we consider to skip ' ' and '\t'
       */
       public boolean skip(int ch1) {
         if(ch1 == (int) ' ' || ch1 == (int) '\t') return true;
         else return false;
       }

       /**
          Method to print the sequence of Tokens
       */
       public void print() {
        Token t = this.firstToken;
         while(t != null) {
           t.print();
           t = t.next;
         }
         System.out.println();
       }
}