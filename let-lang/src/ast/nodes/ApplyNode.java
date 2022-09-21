package ast.nodes;

import lexer.Token;
import lexer.TokenType;
import environment.Environment;

/**
 * This node represents the unary op node.
 * @author Zach Kissel
 */
 public class ApplyNode extends SyntaxNode
 {
   private SyntaxNode func;
   private SyntaxNode arg;

   /**
    * Constructs a new node that represents function application.
    * @param func the function to apply.
    * @param arg the argument to apply the function to.
    */
   public ApplyNode(SyntaxNode func,  SyntaxNode arg)
   {
     this.func = func;
     this.arg = arg;
   }

   /**
    * Evaluate the node.
    * @param env the executional environment we should evaluate the
    * node under.
    * @return the object representing the result of the evaluation.
    */
   public Object evaluate(Environment env)
   {
     SyntaxNode node = null;
     LambdaNode lexp;

     // If our function is a TokenNode we should evaluate it as it
     // may be an identifier.
     if (func instanceof TokenNode)
     {
        node = (SyntaxNode)func.evaluate(env);
     }

     // Make sure we have a function to apply.
     if (!(node instanceof FunNode) && !(func instanceof LambdaNode))
     {
       System.out.println("Apply not given a function.");
       return null;
     }

     // Get the lambda expression to evaluate.
     if (node instanceof FunNode)
     {
       FunNode function = (FunNode) node;
       lexp = (LambdaNode)function.getLambdaExpression();
     }
     else
      lexp = (LambdaNode)func;

     if(lexp.isClosure) //if the lambda has a closure
     {
         env.updateEnvironment(lexp.getVar(),lexp.getExpr()); //update the environment with the lambdas expression
     }

      // Bind the parameter to the argument and evaluate the function call.
     env.updateEnvironment(lexp.getVar(), arg.evaluate(env));


     return lexp.evaluate(env);
   }
 }
