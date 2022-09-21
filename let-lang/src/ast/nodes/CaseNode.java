package ast.nodes;

import environment.Environment;
import lexer.Token;

/**
 * This class creates a case node to handle the cases of a switch
 * @author liamtwomey
 */
public class CaseNode extends SyntaxNode {

    private TokenNode cond;
    private SyntaxNode branch;
    /**
     * this constructs a case node object which takes a tokenNoode as the test
     * condition and a SyntaxNode as the expression to evaluate.
     * @param cond the test condition
     * @param branch the expression to evaluate
     */
    public CaseNode(TokenNode cond, SyntaxNode branch)
    {
        this.cond = cond;
        this.branch = branch;
    }
   /**
    * this returns the token node.
    * @return cond, the token node
    */
    public TokenNode getTokenNode()
    {
        return cond;
    }

    /**
     * This simply returns the evaluation of the branch for a specific case.
     * @param env the environment to evaluate under
     * @return the evaluation of the branch
     */
    @Override
    public Object evaluate(Environment env) {
        return branch.evaluate(env);
    }

}
