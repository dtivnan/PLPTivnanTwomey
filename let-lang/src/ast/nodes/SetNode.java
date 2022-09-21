package ast.nodes;

import environment.Environment;
import java.util.HashSet;
import java.util.Iterator;

/**
 * This node will construct a new set syntax node
 * @author liamtwomey
 */
public class SetNode extends SyntaxNode{

    private HashSet<TokenNode> set;
    /**
     * This constructs a SetNode object that takes in a HashSet of TokenNodes.
     * @param set the set of TokenNodes.
     */
    public SetNode(HashSet<TokenNode> set)
    {
        this.set = set;

    }
    @Override
    public Object evaluate(Environment env) {

        HashSet<Object> rSet = new HashSet<>();

        if(set.isEmpty()) //if the set is empty
        {
            return set;
        }
        Iterator<TokenNode> it = set.iterator();

        while(it.hasNext())// walk through the set and eval each element
        {
            TokenNode node = it.next();
            Object val = node.evaluate(env);//eval each token node
            rSet.add(val);//add them to the return set
        }
        return rSet;
    }

}
