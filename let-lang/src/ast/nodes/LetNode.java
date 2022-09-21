package ast.nodes;

import lexer.Token;
import lexer.TokenType;
import environment.Environment;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This node represents a let expression.
 * @author Zach Kissel
 */
 public class LetNode extends SyntaxNode
 {
   private Token var;
   private SyntaxNode varExpr;
   private SyntaxNode expr;
   private  HashMap<Token, SyntaxNode> letMap;
   private boolean multiLet = false;

   /**
    * Constructs a new binary operation syntax node.
    * @param var the variable identifier.
    * @param varExpr the expression that give the varaible value.
    * @param expr the expression that uses the variables value.
    */
    public LetNode(Token var, SyntaxNode varExpr, SyntaxNode expr)
    {
      this.var = var;
      this.varExpr = varExpr;
      this.expr = expr;
    }

    /**
     * Constructs a let node to handle multiple values
     * @param letMap a map of tokens and expressions
     * @param expr the final expression to evaluate
     */
    public LetNode(HashMap<Token, SyntaxNode> letMap, SyntaxNode expr)
    {
      this.letMap = letMap;
      this.expr = expr;
      multiLet = true;
    }

    /**
     * Evaluate the node.
     * @param env the executional environment we should evaluate the
     * node under.
     * @return the object representing the result of the evaluation.
     */
     public Object evaluate(Environment env)
     {
         Object value = null;
         if(multiLet)
         {
             //iterate through the hashmap and for each varVal check to make sure
             //its one of the allowed values, if it is add that and its key to
             //the env
             //finally evaluate the expression
             Iterator<Entry<Token, SyntaxNode>> it = letMap.entrySet().iterator();
             while(it.hasNext())
             {
                Map.Entry mapObject = (Map.Entry) it.next(); //here get each individual thing and add it to eh environment
                Token x = (Token) mapObject.getKey();
                SyntaxNode y = (SyntaxNode) mapObject.getValue();
                Object val = y.evaluate(env);
                if (val instanceof Integer || val instanceof Double ||
                val instanceof LinkedList || val instanceof HashSet)
                    env.updateEnvironment(x, val);
                else
                    System.out.println("Failed to add " + x + "with  value " + val.getClass());
             }
             //when there is nothing left in the iterator
             //eval expr
             value = expr.evaluate(env);
             return value;
         }
         else
         {
            Object varVal = varExpr.evaluate(env);

            if (varVal instanceof Integer || varVal instanceof Double ||
                varVal instanceof LinkedList ||varVal instanceof HashSet)
            env.updateEnvironment(var, varVal);
            else
                System.out.println("Failed to add " + var + "with  value " + varVal.getClass());
            value = expr.evaluate(env);
            return value;
         }

     }
 }
