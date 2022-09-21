package ast.nodes;

import lexer.Token;
import lexer.TokenType;
import environment.Environment;

/**
 * This node represents a lambda expression.
 * @author Zach Kissel
 */
 public class LambdaNode extends SyntaxNode
 {
   private Token var;
   private SyntaxNode expr;
   private ClosureNode closure;
   boolean isClosure = false;

   /**
    * Constructs a new function node which represents
    * a function declaration.
    * @param var the free variable in the expression.
    * @param expr the expression to excute.
    */
   public LambdaNode(Token var, SyntaxNode expr)
   {
     this.var = var;
     this.expr = expr;
   }

   /**
    * constructs a new function with a function declaration when there is a
    * closure.
    * @param var the free variable in the expression
    * @param expr the expression to execute
    * @param closure the closure containing an environment and lambda
    */
   public LambdaNode(Token var, SyntaxNode expr, ClosureNode closure)
   {
     this.var = var;
     this.expr = expr;
     this.closure = closure;
     isClosure = true;
   }
   /**
    * a method to test if there is a closure.
    * @return true if closure false if not.
    */
   public boolean isClosure()
   {
       return isClosure;
   }
   /**
    * a method to return the  expression
    * @return the expression
    */
   public SyntaxNode getExpr()
   {
       return expr;
   }

  /**
   * Get the parameter of the function.
   * @return a Token representing the parameter name.
   */
   public Token getVar()
   {
     return var;
   }

   /**
    * Evaluate the node.
    * @param env the executional environment we should evaluate the
    * node under.
    * @return the object representing the result of the evaluation.
    */
   public Object evaluate(Environment env)
   {
     if(isClosure)
     {
         env = (Environment)closure.evaluate(env); //get the environment from the closure and make it this env
         env.updateEnvironment(var, closure.getLambda()); //update the env with the closures lambda
         return closure.evaluate(env); //
     }
     else
        return expr.evaluate(env);
   }
 }
