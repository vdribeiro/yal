/**
   This class will be used to store tokens.
   Author: João M. P. Cardoso
   Institution: University of Porto, Faculty of Engineering, Porto, Portugal
   Date: January 2011
*/

public class Token {
       // the kind of the token
       public int type;
       // a String to store the value of the token
       public String image;
       // the next token in the sequence
       public Token next=null;

       public Token(String image, int type) {
         this.image = image;
         this.type = type;
       }

       public void print() {
         System.out.print(TokenTypes.name[type]+"("+image+") ");
       }
}