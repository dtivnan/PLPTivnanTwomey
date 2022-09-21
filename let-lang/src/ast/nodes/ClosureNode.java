package ast.nodes;

import environment.Environment;
import lexer.Token;

/**
 * This creates a node to handle closures and their environments.
 * @author liamtwomey
 */
public class ClosureNode extends SyntaxNode {

    private SyntaxNode lambda;
    private Environment lamEnv;
    /**
     * This takes in a syntaxNode which represents a lambda expression
     * @param lambda the lambda expression which is in the closure.
     */
    public ClosureNode(SyntaxNode lambda)
    {
        this.lambda = lambda;
    }
    /**
     * This method returns the environment of the closure.
     * @return the closures environment.
     */
    public Environment getEnv()
    {
        return lamEnv;
    }
    /**
     * this method sets the environment of the closure
     * @param e the environment that it will be set to
     */
    public void SetEnv(Environment e)
    {
        this.lamEnv = e;
    }
    /**
     * this method returns the lambda expression associated with the closure
     * @return the SyntaxNode lambda
     */
    public SyntaxNode getLambda()
    {
        return lambda;
    }

    /**
     * the evaluate method of closureNode simply returns a copy of its environment
     * @param env the environment
     * @return a copy of the environment
     */
    @Override
    public Object evaluate(Environment env) {

        return env.copy();
    }

}
