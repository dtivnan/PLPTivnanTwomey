package ast.nodes;

import environment.Environment;
import java.util.LinkedList;
import lexer.Token;

/**
 * This class creates a switch node to handle n-way selection
 * @author liamtwomey
 */
public class SwitchNode extends SyntaxNode{

    private TokenNode testCase;
    private LinkedList<CaseNode> caseList = new LinkedList<>();
    private SyntaxNode defaultCase;
    /**
     *
     * @param testCase
     * @param defaultCase
     * @param caseList
     */
    public SwitchNode(TokenNode testCase, SyntaxNode defaultCase,LinkedList<CaseNode> caseList)
    {
        this.caseList = caseList;
        this.testCase = testCase;
        this.defaultCase = defaultCase;
    }
    /**
     * This method evaluates the switch statement. The linked list of cases
     * is walked through and for each caseNode, the tokenNode containing the case
     * number is compared against the original test case, if they match the loop
     * is broken and the branch of the corresponding case is evaluated
     * and returned.
     * @param env the environment to evaluate under
     * @return the evaluation of the expression which matches the initial test case.
     */
    @Override
    public Object evaluate(Environment env) {

        Object firstCase = testCase.evaluate(env);
        CaseNode trueCase = null;
        if(!(firstCase instanceof Integer)) //switchs only allow ints as test cases
        {
            System.out.println("Error: only ints allowed for switch statements");
            return null;
        }
        int fCase = (int) firstCase;
        int size = caseList.size(); //get the size of the list to walk through it
        boolean caseFound = false;//set casefound to false for the loop
        for(int x=0;(x < size) && (caseFound != true); x++) //while there are still elements and the case hasnt been found
        {
            CaseNode temp = caseList.get(x);//get the case node
            TokenNode tTemp = temp.getTokenNode();//get its tokenNode
            if(tTemp.evaluate(env) instanceof Integer)
            {
                int tempCase = (int) tTemp.evaluate(env);//check if it matches
                if(tempCase == fCase)
                {
                    trueCase = temp;//get the correct caseNode
                    caseFound = true;//so the loop can break on the next iteration
                }
            }
            else
            {
                System.out.println("Error: only ints allowed for switch statements");
            }
        }//end of for loop
        if(caseFound != true)
            return defaultCase.evaluate(env);//return the default if case is not found
        else
        {
            return trueCase.evaluate(env);
        }
    }//end of evaluate

}
