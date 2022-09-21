package parser;

import java.util.LinkedList;
import lexer.Lexer;
import lexer.TokenType;
import lexer.Token;
import ast.SyntaxTree;
import ast.nodes.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Implements a generic super class for parsing files.
 * @author Zach Kissel
 */
public class Parser
{
  private Lexer lex;            // The lexer for the parser.
  private boolean errorFound;   // True if ther was a parser error.
  private boolean doTracing;    // True if we should run parser tracing.
  private Token nextTok;        // The current token being analyzed.

  /**
   * Constructs a new parser for the file {@code source} by
   * setting up lexer.
   * @param src the source code file to parse.
   * @throws FileNotFoundException if the file can not be found.
   */
  public Parser(File src) throws FileNotFoundException
  {
    lex = new Lexer(src);
    errorFound = false;
    doTracing = false;
  }

  /**
   * Construct a parser that parses the string {@code str}.
   * @param str the code to evaluate.
   */
  public Parser(String str)
  {
    lex = new Lexer(str);
    errorFound = false;
    doTracing = false;
  }

  /**
   * Turns tracing on an off.
   */
  public void toggleTracing()
  {
    doTracing = !doTracing;
  }

  /**
   * Determines if the program has any errors that would prevent
   * evaluation.
   * @return true if the program has syntax errors; otherwise, false.
   */
  public boolean hasError()
  {
    return errorFound;
  }

  /**
   * Parses the file according to the grammar.
   * @return the abstract syntax tree representing the parsed program.
   */
  public SyntaxTree parse()
  {
    SyntaxTree ast;

    nextToken();    // Get the first token.
    ast = new SyntaxTree(evalProg());   // Start processing at the root of the tree.

    if (nextTok.getType() != TokenType.EOF)
      logError("Parse error, unexpected token " + nextTok);
    return ast;
  }


  /************
   * Private Methods.
   *
   * It is important to remember that all of our non-terminal processing methods
   * maintain the invariant that each method leaves the next unprocessed token
   * in {@code nextTok}. This means each method can assume the value of
   * {@code nextTok} has not yet been processed when the method begins.
   ***********/

   /**
    * Method to handle the program non-terminal.
    *
    * <prog> -> <expr> { <expr> }
    */
    private SyntaxNode evalProg()
    {
      LinkedList<SyntaxNode> exprs = new LinkedList<>();

      trace("Enter <prog>");
      while (nextTok.getType() != TokenType.EOF)
        exprs.add(evalFun());
      trace("Exit <prog>");
      return new ProgNode(exprs);
    }

  /**
   * Method to hand the fun non-terminal.
   *
   * <fun> -> fun <id> <lexpr> | <expr>
   */
   private SyntaxNode evalFun()
   {
     // Function definition.

     if(nextTok.getType() == TokenType.SEMICOLON) // to handle comments at the start of the program
     {
         nextToken();
         String a = "";
         while(nextTok.getType()!= TokenType.SEMICOLON)//read to the end of the comment symbol
         {
             a = a+ " " +nextTok.getValue();//construct the comment string
             nextToken();
         }
         System.out.println("Comment:"+ a);
         nextToken();//output the comment
     }
     if (nextTok.getType() == TokenType.FUN)
     {
       nextToken();
       return handleFun();
     }
     else // Just an expression.
     {
       return evalExpr();
     }
   }

   /**
    * Method to handle the expression non-terminal
    *
    * <expr> -> let <id> := <expr> in <expr> |
    */
    private SyntaxNode evalExpr()
    {
        trace("Enter <expr>");
        SyntaxNode rexpr;
        TokenType op;
        SyntaxNode expr = null;

        // Handle let.
        if (nextTok.getType() == TokenType.LET)
        {
          nextToken();
          return handleLet();
        }

        // Handle function application.
        else if (nextTok.getType() == TokenType.APPLY)
        {
          nextToken();
          return handleApply();
        }

        // Handle conditionals.
        else if (nextTok.getType() == TokenType.IF)
        {
          nextToken();
          return handleIf();
        }
        else if(nextTok.getType() == TokenType.SWITCH) //if n-way selection is happening
        {
            nextToken();
            return handleSwitch(); //handle the switch statement
        }
        // Boolean not.
        else if (nextTok.getType() == TokenType.NOT)
        {
          op = nextTok.getType();
          nextToken();
          expr = evalRexpr();
          expr = new UnaryOpNode(expr, op);
        }
        else  // and/or.
        {
          expr = evalRexpr();

          while (nextTok.getType() == TokenType.AND ||
            nextTok.getType() == TokenType.OR)
          {
            op = nextTok.getType();
            nextToken();
            rexpr = evalRexpr();
            expr = new BinOpNode(expr, op, rexpr);
          }
        }

        trace("Exit <expr>");

        return expr;
    }


    /**
     * Handles relational expressions.
     * rexpr ->mexpr [ ( **<** | **>** | **>=** | **<=** | **=** ) mexpr ]
     * @return a SyntaxNode representing the relation expression.
     */
    private SyntaxNode evalRexpr()
    {
      SyntaxNode left = null;
      SyntaxNode right = null;
      TokenType op;

      left = evalMexpr();

      if (nextTok.getType() == TokenType.LT || nextTok.getType() == TokenType.LTE ||
          nextTok.getType() == TokenType.GT || nextTok.getType() == TokenType.GTE ||
          nextTok.getType() == TokenType.EQ || nextTok.getType() == TokenType.NEQ)
      {
          op = nextTok.getType();
          nextToken();
          right = evalMexpr();
          return new RelOpNode(left, op, right);
      }

      return left;
    }


    /**
     * Handles the start of the matematics expressions.
     * mexpr -> <term> {(+ | - ) <term>}
     * @return a SyntaxNode representing the expression.
     */
    private SyntaxNode evalMexpr()
    {
      SyntaxNode expr = null;
      SyntaxNode rterm = null;
      TokenType op;

      expr = evalTerm();

      while (nextTok.getType() == TokenType.ADD ||
        nextTok.getType() == TokenType.SUB)
      {
        op = nextTok.getType();
        nextToken();
        rterm = evalTerm();
        expr = new BinOpNode(expr, op, rterm);
      }

      return expr;
    }

    private SyntaxNode evalLambdaExpr()
    {
      Token var;
      SyntaxNode expr = null;
      boolean closure = false;
      ClosureNode c = null;
      if (nextTok.getType() == TokenType.ID)
      {
        var = nextTok;
        nextToken();
        if (nextTok.getType() == TokenType.TO)
        {
          nextToken();
          if(nextTok.getType() == TokenType.ID)// if there is a closure
          {
            closure = true;
            Token name = nextTok; //get the name of the other var
            nextToken();
            if(nextTok.getType() == TokenType.TO) //the assignment
            {
                nextToken();
                SyntaxNode expr2 = evalExpr(); //get the expression
                c = new ClosureNode(new LambdaNode(name,expr2)); //declare the closure node
            }
            return new LambdaNode(var,expr,c);
          }
          else
            expr = evalExpr();

          if(closure)//if there is a closure
            return new LambdaNode(var,expr,c); //create a lambdanode with the closure
          else
            return new LambdaNode(var, expr);

        }
        else
        {
          logError("Expected ~.");
        }
      }
      else
      {
        logError("Lambda expressions require parameters.");
      }
      return null;
    }

    /**
     * This method handles a function definition.
     * <id> <id> => <expr>
     * @return a function node.
     */
     private SyntaxNode handleFun()
     {
       Token funName;

       SyntaxNode lexpr;

       if (nextTok.getType() == TokenType.ID)
       {
         funName = nextTok;
         nextToken();
         lexpr = evalLambdaExpr();
         return new FunNode(funName, lexpr);
       }
       else
       {
         logError("Functions need names.");
       }
       return null;
     }

    /**
     * This method handles function application.
     * apply <id> <expr>
     */
    private SyntaxNode handleApply()
    {
      Token fun;
      SyntaxNode expr;

      // Process a function identifier.
      if (nextTok.getType() == TokenType.ID)
      {
        fun = nextTok;
        nextToken();
        expr = evalExpr();
        return new ApplyNode(new TokenNode(fun), expr);
      }

      // Process a lambda expression.
      else if (nextTok.getType() == TokenType.LPAREN)
      {
        nextToken();
        SyntaxNode lexpr = evalLambdaExpr();
        if (nextTok.getType() != TokenType.RPAREN)
        {
          logError("Closing paren expected.");
          return null;
        }
        nextToken();
        expr = evalExpr();
        return new ApplyNode(lexpr, expr);
      }
      else
      {
        logError("Function name or lambda expressoin expected.");
      }
      return null;
    }

    /**
     * This method handles conditionals.
     * if <expr> then <expr> else <expr>
     */
    private SyntaxNode handleIf()
    {

      SyntaxNode cond;
      SyntaxNode trueBranch;
      SyntaxNode falseBranch;

      cond = evalExpr();

      if (nextTok.getType() == TokenType.THEN)
      {
        nextToken();
        trueBranch = evalExpr();
        if (nextTok.getType() == TokenType.ELSE)
        {
          nextToken();
          falseBranch = evalExpr();
          return new IfNode(cond, trueBranch, falseBranch);
        }
        else
        {
          logError("Else expected.");
        }
      }
      else
      {
        logError("Expected then.");
      }
      return null;
    }

    /**
     * This method handles a switch expression.
     * @return a switch node.
     */
    private SyntaxNode handleSwitch()
    {
        Token initialCase;
        LinkedList<CaseNode> caseList = new LinkedList<>(); //the list for cases
        SwitchNode sn = null;
        if(nextTok.getType() == TokenType.LPAREN)
        {
            nextToken();
            initialCase = nextTok; //get the test case
            nextToken();
            if(nextTok.getType() == TokenType.RPAREN)
            {
                nextToken();
                while(nextTok.getType() != TokenType.DEFAULT) //iterate through all other casese
                {
                    if(nextTok.getType() == TokenType.CASE)
                    {
                        nextToken();
                        if(nextTok.getType() == TokenType.LPAREN)
                        {
                            nextToken();
                            TokenNode cCase = new TokenNode(nextTok);//get the case
                            nextToken();
                            if(nextTok.getType() == TokenType.RPAREN)
                            {
                               nextToken();
                               SyntaxNode s = evalExpr(); //eval the expression for each case
                               caseList.add(new CaseNode(cCase, s));//add a new caseNode containing the case and expression to the list
                            }
                            else
                                logError("Error: right paren expected");
                        }
                        else
                            logError("Error: left parent expected");
                    }
                }//end of while loop to read cases
                if(nextTok.getType() == TokenType.DEFAULT)//handle default statements
                {
                    nextToken();
                    SyntaxNode temp = evalExpr(); //defaults only need the expression
                    sn = new SwitchNode(new TokenNode(initialCase), temp, caseList); //return the new switchNode
                    return sn;
                }
            }
            else
                logError("Error: right paren expected");
        }
        else
            logError("Error: left paren expected");
        return sn;
    }//end of handleSwitch

    /**
     * This method handles a let expression as well as let expression declaring
     * multiple variables.
     * <id> := <expr> in <expr>
     * @return a let node.
     */
    private SyntaxNode handleLet()
    {
        Token var = null;
        SyntaxNode varExpr;
        SyntaxNode expr;
        boolean multiLet = false;
        HashMap<Token, SyntaxNode> letMap = new HashMap<>(); //for mapping variables to their values

        trace("enter handleLet");

        // Handle the identifier.
        if (nextTok.getType() == TokenType.ID)
        {
          var = nextTok;
          nextToken();

          // Handle the assignemnt.
          if (nextTok.getType() == TokenType.ASSIGN)
          {
            nextToken();
            varExpr = evalExpr();
            if(nextTok.getType() == TokenType.ID)//if the let expression is declaring multiple variables
            {
                multiLet = true;//set true
                letMap.put(var, varExpr); // the initial var and expr to the map
                while(nextTok.getType() != TokenType.IN) //while the declarations are still happening
                {
                    if(nextTok.getType() == TokenType.ID)
                    {
                        Token var2 = nextTok; //get the new variable
                        nextToken();
                        SyntaxNode varExpr2 = null;
                        if(nextTok.getType() == TokenType.ASSIGN)
                        {
                            nextToken();
                            varExpr2 = evalExpr();//the variables value
                        }
                        letMap.put(var2, varExpr2);//put in map with var as key and expression as value
                    }
                    else
                        logError("error: varaible assignment expected");
                }
            }
            // Handle the in expr.
            if (nextTok.getType() == TokenType.IN)
            {
              nextToken();
              expr = evalExpr();
              if(multiLet)
                return new LetNode(letMap, expr);
              else
                return new LetNode(var, varExpr, expr);
            }
            else
            {
              logError("Let expression expected in, saw " + nextTok + ".");
            }
          }
          else
          {
            logError("Let expression missing assignment!");
          }

        }
        else
          logError("Let expression missing variable.");
        trace("exit handleLet");
        return null;
    }

    /**
     * Method to handle the listExpr non-terminal.
     *
     * <listExpr> -> list ( [ (id | num) {, (id | num)}] )
     */
     private SyntaxNode evalListExpr()
     {
       LinkedList<TokenNode> entries = new LinkedList<>();
       ListNode lst = null;

       trace("Enter <listExpr>");

       if (nextTok.getType() == TokenType.LIST)
       {
         nextToken();
         if (nextTok.getType() == TokenType.LPAREN)
         {
            nextToken();

            // We could have an empty list.
            if (nextTok.getType() == TokenType.RPAREN)
            {
              lst = new ListNode(entries);
              nextToken();
              return lst;
            }
            if (nextTok.getType() == TokenType.INT ||
                nextTok.getType() == TokenType.REAL ||
                nextTok.getType() == TokenType.ID)
                  entries.add(new TokenNode(nextTok));
            else {
                logError("Invalid list element.");
                return new ListNode(entries);
            }
            nextToken();
            while (nextTok.getType() == TokenType.COMMA)
            {
              nextToken();
              if (nextTok.getType() == TokenType.INT ||
                  nextTok.getType() == TokenType.REAL ||
                  nextTok.getType() == TokenType.ID)
                    entries.add(new TokenNode(nextTok));
              else {
                  logError("Invalid list element.");
                  return new ListNode(entries);
              }
              nextToken();
            }

            // Handle the end of the list.
            if (nextTok.getType() == TokenType.RPAREN)
            {
              lst = new ListNode(entries);
              nextToken();
            }
            else
            {
              logError("Invalid List");
            }
         }
         else
         {
            logError("Left paren expected.");
         }
       }
       else
       {
         logError("Unexpected list expression.");
       }

       trace("Exit <listExpr>");
       return lst;
     }

    /**
     * Method to handle the term non-terminal.
     *
     * <term> -> <factor> {( * | /) <factor>}
     */
     private SyntaxNode evalTerm()
     {
       SyntaxNode rfact;
       TokenType op;
       SyntaxNode term;

       trace("Enter <term>");
       term = evalFactor();

       while (nextTok.getType() == TokenType.MULT ||
           nextTok.getType() == TokenType.DIV ||
           nextTok.getType() == TokenType.CONCAT ||nextTok.getType() == TokenType.UNION
               || nextTok.getType() == TokenType.INTERSECT)
       {
         op = nextTok.getType();
         nextToken();
         rfact = evalFactor();
         term = new BinOpNode(term, op, rfact);
       }
       trace("Exit <term>");
       return term;
     }

     /**
      * Method to handle the factor non-terminal.
      *
      * <factor> -> <id> | <int> | <real> | ( <expr> )
      *             | list ( [ (id | num) {, (id | num)}])
      *             | true | false
      */
      private SyntaxNode evalFactor()
      {
        trace("Enter <factor>");
        SyntaxNode fact = null;

        if (nextTok.getType() == TokenType.ID ||
            nextTok.getType() == TokenType.INT ||
            nextTok.getType() == TokenType.REAL ||
            nextTok.getType() == TokenType.TRUE ||
            nextTok.getType() == TokenType.FALSE)
        {
            fact = new TokenNode(nextTok);
            nextToken();
        }
        else if (nextTok.getType() == TokenType.LIST)
        {
          return evalListExpr();
        }
        else if(nextTok.getType() == TokenType.SET) //if a set is being declared
        {
            return evalSet();//handle the set construction
        }
        else if (nextTok.getType() == TokenType.LST_HD)
        {
          nextToken();
          fact = evalFactor();
          return new HeadNode(fact);
        }
        else if (nextTok.getType() == TokenType.LST_TL)
        {
          nextToken();
          fact = evalFactor();
          return new TailNode(fact);
        }
        else if (nextTok.getType() == TokenType.LPAREN)
        {
          nextToken();
          fact = evalExpr();

          if (nextTok.getType() == TokenType.RPAREN)
            nextToken();
          else
            logError("Expected \")\" received " + nextTok +".");
        }
        else
        {
          logError("Unexpected token " + nextTok);

          // Recover from poorly formed expression.
          // if (nextTok.getType() == TokenType.RPAREN)
          //   nextToken();
        }

        trace("Exit <factor>");
        return fact;
      }

      /**
       * This method handles the creation of SetNode objects.
       * @return a SetNode object containing a set
       */
      public SyntaxNode evalSet()
      {
          SetNode s = null;
          HashSet<TokenNode> set = new HashSet<>();//where the individual objects of the sets will be stored
          if(nextTok.getType() == TokenType.SET)
          {
              nextToken();
              if(nextTok.getType() == TokenType.LBRACKET) //sets are contained in brackets
              {
                  nextToken();
                  if(nextTok.getType() == TokenType.RBRACKET) //if the set is empty
                  {
                      s = new SetNode(set);
                      nextToken();
                      return s;
                  }
                  if(nextTok.getType() == TokenType.INT || nextTok.getType() == //read first element of set
                          TokenType.REAL || nextTok.getType() == TokenType.ID)
                  {
                      set.add(new TokenNode(nextTok)); //add it to the set
                  }
                  else
                  {
                      logError("invalid set elements");
                      return new SetNode(set);
                  }
                  nextToken();
                  while(nextTok.getType() == TokenType.COMMA) //read through whole set
                  {
                      nextToken();
                      if(nextTok.getType() == TokenType.INT || nextTok.getType() ==
                          TokenType.REAL || nextTok.getType() == TokenType.ID)
                      {
                          set.add(new TokenNode(nextTok)); //add to set
                      }
                      else
                      {
                          logError("invalid set elements");
                          return new SetNode(set);
                      }
                     nextToken();
                  }// end of while loop
                  if(nextTok.getType() == TokenType.RBRACKET)//sets end with right brackets
                  {
                      s = new SetNode(set);
                      nextToken();
                  }
                  else
                      logError("Invalid set");
              }
              else //end of if LBRACKET
                  logError("left bracket expected");
          }//end of it SET
          else
              logError("Set expression expected");
          return s;
      }//end of evalSet

  /**
   * Logs an error to the console.
   * @param msg the error message to dispaly.
   */
   private void logError(String msg)
   {
     System.err.println("Error (" + lex.getLineNumber() + "): " + msg);
     errorFound = true;
   }

   /**
    * This prints a message to the screen on if {@code doTracing} is
    * true.
    * @param msg the message to display to the screen.
    */
    private void trace(String msg)
    {
      if (doTracing)
        System.out.println(msg);
    }

    /**
     * Gets the next token from the lexer potentially logging that
     * token to the screen.
     */
    private void nextToken()
    {
      nextTok = lex.nextToken();

      if (doTracing)
        System.out.println("nextToken: " + nextTok);

    }

}
