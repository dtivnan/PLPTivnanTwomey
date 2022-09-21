package ast.nodes;

import lexer.Token;
import lexer.TokenType;
import environment.Environment;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * This node represents a binary operation.
 * @author Zach Kissel
 */
 public class BinOpNode extends SyntaxNode
 {
   private TokenType op;
   private SyntaxNode leftTerm;
   private SyntaxNode rightTerm;

   /**
    * Constructs a new binary operation syntax node.
    * @param lterm the left operand.
    * @param op the binary operation to perform.
    * @param rterm the right operand.
    */
    public BinOpNode(SyntaxNode lterm, TokenType op, SyntaxNode rterm)
    {
      this.op = op;
      this.leftTerm = lterm;
      this.rightTerm = rterm;
    }

    /**
     * Evaluate the node.
     * @param env the executional environment we should evaluate the
     * node under.
     * @return the object representing the result of the evaluation.
     */
     public Object evaluate(Environment env)
     {
        Object lval;
        Object rval;
        boolean useDouble = false;

        lval = leftTerm.evaluate(env);
        rval = rightTerm.evaluate(env);

        if (op == TokenType.CONCAT)
          return handleConcat(lval, rval);

        if(op == TokenType.UNION || op == TokenType.INTERSECT) //handle set operations
            return handleSetOp(lval,rval);

        if (lval == null || rval == null)
          return null;

        // Make sure the type is sound.
        if(!(lval instanceof Integer || lval instanceof Double || lval instanceof Boolean) &&
           !(rval instanceof Double || rval instanceof Integer || lval instanceof Boolean))
          return null;


        if (lval.getClass() !=  rval.getClass())
        {
          System.out.println("Error: mixed type expression.");
          return null;
        }
        if (lval instanceof Double)
          useDouble = true;

        // Perform the operation base on the type.
        switch(op)
        {
          case ADD:
            if (useDouble)
              return (Double) lval + (Double) rval;
            return (Integer) lval + (Integer) rval;
          case SUB:
            if (useDouble)
              return (Double) lval - (Double) rval;
            return (Integer) lval - (Integer) rval;
          case MULT:
            if (useDouble)
              return (Double) lval * (Double) rval;
            return (Integer)lval * (Integer) rval;
          case DIV:
            if (useDouble)
              return (Double) lval / (Double) rval;
            return (Integer) lval / (Integer) rval;
          case AND:
            return (Boolean) lval && (Boolean) rval;
          case OR:
            return (Boolean) lval || (Boolean) rval;
          default:
            return null;
        }

     }

     /**
      * Handles the concatenation operation returning the concatenated
      * form of the list.
      * @return the concatenation of the two lists.
      */
      @SuppressWarnings("unchecked")
      private Object handleConcat(Object lval, Object rval)
      {
        if (!(lval instanceof LinkedList) || !(rval instanceof LinkedList))
          return null;
        else
        {
          LinkedList leftList = (LinkedList)lval;
          LinkedList rightList = (LinkedList)rval;

          if (leftList.size() == 0)
            return rightList;
          else if (rightList.size() == 0)
            return leftList;
          else
          {
            if (leftList.getFirst().getClass() != rightList.getFirst().getClass())
            {
              System.out.println("Error: mixed type list not supported.");
              return null;
            }

            leftList.addAll(rightList);

            return leftList;
          }
        }
      }
      /**
       * Handles the union and intersection of two sets, returning a single set
       * that is either the union or intersection.
       * @param lval The left set.
       * @param rval the right set.
       * @return a set having either the union or intersection of both.
       */
      private Object handleSetOp(Object lval, Object rval)
      {
          HashSet<Object> returnSet = new HashSet<>();
          if(lval instanceof HashSet && rval instanceof HashSet)// make sure that sets are being used
          {
              HashSet<Object> leftSet = (HashSet<Object>) lval;
              HashSet<Object> rightSet = (HashSet<Object>) rval;
              if(leftSet.isEmpty() || rightSet.isEmpty()) //cannot perform on empty sets
              {
                  System.out.println("Error: sets must not be empty");
                  return null;
              }
              if(op == TokenType.UNION)
              {
                  returnSet.addAll(leftSet);
                  returnSet.addAll(rightSet);
                  return returnSet; //return the union of both sets
              }
              else if(op == TokenType.INTERSECT)
              {
                  returnSet = leftSet;
                  returnSet.retainAll(rightSet);
                  return returnSet; //return the intersection of both sets
              }
          }
          else
          {
              System.out.println("error: set expected");
          }
          return null;

      }
 }
