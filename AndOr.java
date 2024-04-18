import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AndOr {
	public boolean match;
	public String result = "";
	public String translated = "";
	
	//private AndOr andOrObject = new AndOr();
	private Condition cond = new Condition();
	
	Pattern var = Pattern.compile("^[a-zA-Z][a-zA-z_0-9]*$");
    
    Pattern andOr = Pattern.compile("^([\\s\\S]+)\\s*(and|or)\\s*([\\s\\S]+)$");
    Pattern not = Pattern.compile("(not)\\s*([\\S]+)$");
    Pattern notLiteral = Pattern.compile("^not$");
    Pattern bool = Pattern.compile("^true$|^false$");
    Pattern andOrLiteral = Pattern.compile("^and$|^or$");


    // interactive terminal version 
    public void run(String[] args) {
		Scanner in = new Scanner(System.in);
		System.out.print(">> ");
		String cmd = in.nextLine();
		while (!cmd.equals("exit")) {
			parseCmd(cmd);
			System.out.print(">> ");
			cmd = in.nextLine();
		}
		in.close();
    }

    public boolean parseCmd(String cmd) {
    	match = andOrExpr(cmd);
    	System.out.print(result);
        return match;
    }

    // parens currently not working for statements like not (true or false)
    // shows bool expr
    private boolean andOrExpr(String cmd) {
        Matcher m = andOr.matcher(cmd);
        boolean match = m.find();
        Matcher n = not.matcher(cmd);
        boolean match2 = n.find();
        
        if (match) {
            result += "<and_or>: " + cmd + "\n";
            String[] tokens = cmd.split("(?=and|or)|(?<=and|or)");
            if (tokens.length < 3) {
                result = "Failed to parse: { " + cmd.trim() + " } " + "is missing an and or or or not\n";
                return false;
            }
            int leftParen = 0;
            int rightParen = 0;
            for (String token : tokens) {
                token = token.trim();
                if (token.length() > 0 && token.substring(0, 1).equals("(")) {
                    leftParen++;
                    token = token.replace("(", "");
                    result += "<paren>: (\n";
                    translated += "(";
                    if (cond.parseCmd(token)) {
                    	result += cond.result;
                    	translated += cond.translated;
                    	printMsg(true, "<bool_expr>", token.trim(), "boolean expression");
                    	printMsg(true, "<not_expr>", token.trim(), "not expression");
                    	return true;
                    }
                }
                boolean right = false;
                if (token.length() > 0 && token.substring(token.length()-1).equals(")")) {
                    right = true;
                    rightParen++;
                    token = token.replace(")", "");
                }
               //if (bool(token)) {
                //	 printMsg(match, "<bool>", token.trim(), "boolean");
               // }
                else if (andOrLiteral(token)) {
                	printMsg(match, "<andOrLiteral>", token.trim(), "and/or literal");
                }
                else if (notExpr(token)) {
                	String[] tokens2 = token.split("(?<=not)");
                	printMsg(match, "<notLiteral>", "not", "not literal");
                	if (boolExpr(token.substring(3).trim())) {
                		if (bool(token.substring(3).trim())) {
                    		printMsg(match, "<bool>", token.substring(3).trim(), "boolean");
                    	}
                    	else if(variable(token.substring(3).trim())){
                    		printMsg(match, "<var>", token.substring(3).trim(), "variable");
                    	}
                    	//else {
                    		//printMsg(match, "<condition>", token.substring(3).trim(), "condition");
                    	//}
                    	printMsg(match, "<bool_expr>", token.substring(3).trim(), "boolean expression");
                	}
                	else {
                		andOrExpr(token.substring(3));
                	}
                	printMsg(match, "<not_expr>", token.trim(), "not expression");
                }
                //else if (variable(token)) {
                //	printMsg(match, "<var>", token.trim(), "variable");
                //}
                else if (boolExpr(token)) {
                	if (bool(token)) {
                		printMsg(match, "<bool>", token.trim(), "boolean");
                	}
                	else {
                		printMsg(match, "<var>", token.trim(), "variable");
                	}
                	printMsg(match, "<bool_expr>", token.trim(), "boolean expression");
                }
      
                else {
                    result = "Failed to parse: { " + token.trim() + " } " + "is not a recognized integer, variable, or arithmetic operator.\n";
                    //System.exit(0);
                    return false;
                }

                if (right) { result += "<paren>: )\n"; }
            }
            if (leftParen != rightParen) {
                result = "Failed to parse: { " + cmd + " } " + "is missing a \"(\" or \")\"\n";
                //System.exit(0);
                return false;
            }
        }
        else if (match2) {
        	result += "<not_expr>: " + cmd + "\n";
            String[] tokens = cmd.split("(?<=not)");
            //for(int i = 0; i < tokens.length; i++) {
            	//System.out.println(tokens[i]);
           //}
            if (tokens.length < 2) {
                result = "Failed to parse: { " + cmd.trim() + " } " + "is missing an and or or.\n";
                //System.exit(0);
                return false;
            }
            int leftParen = 0;
            int rightParen = 0;
            for (String token : tokens) {
                token = token.trim();
                if (token.length() > 0 && token.substring(0, 1).equals("(")) {
                    leftParen++;
                    token = token.replace("(", "");
                    result += "<paren>: (\n";
                }
                boolean right = false;
                if (token.length() > 0 && token.substring(token.length()-1).equals(")")) {
                    right = true;
                    rightParen++;
                    token = token.replace(")", "");
                }
               if (andOrLiteral(token)) {
                	printMsg(match2, "<andOrLiteral>", token.trim(), "and/or literal");
                }
                else if (notLiteral(token)) {
                	printMsg(match2, "<notLiteral>", token.trim(), "not literal");
                }
                else if (boolExpr(token)) {
                	if (bool(token)) {
                		printMsg(match2, "<bool>", token.trim(), "boolean");
                	}
                	else {
                		printMsg(match2, "<var>", token.trim(), "variable");
                	}
                	printMsg(match2, "<bool_expr>", token.trim(), "boolean expression");
                }
                else {
                    result = "Failed to parse: { " + token.trim() + " } " + "is not a recognized integer, variable, or arithmetic operator.\n";
                    //System.exit(0);
                    return false;
                }

                if (right) {result += "<paren>: )\n"; }
            }
            if (leftParen != rightParen) {
                result = "Failed to parse: { " + cmd + " } " + "is missing a \"(\" or \")\"\n";
                //System.exit(0);
                return false;
            }
        }
        else {
            result = "Failed to parse: {" + cmd + "} is not a valid and/or expression or subdivision.\n";
            //System.exit(0);
            return false;
        }
        
        return match;
    }
    
    private boolean notExpr(String cmd) {
		boolean match = false;
		Matcher m = not.matcher(cmd);
		if (m.find()) {
			match = true;
			match = match && notLiteral(m.group(1));
			match = (match && bool(m.group(2))) || (match && variable(m.group(2)));
		} 
		//printMsg(match, "<not_expr>", cmd, "not expression");
		return match;
	}

    private boolean variable(String cmd) {
        Matcher m = var.matcher(cmd);
        boolean match = m.find();
       
        return match;
    }
    
    private boolean bool(String cmd) {
    	Matcher m = bool.matcher(cmd);
    	boolean match = m.find();
    	//printMsg(match, "<bool>", cmd, "boolean");
    	return match;
    }
    
    private boolean andOrLiteral(String cmd) {
    	Matcher m = andOrLiteral.matcher(cmd);
    	boolean match = m.find();
    	//printMsg(match, "<andOrLiteral>", cmd, "and/or literal");
    	return match;
    }
    
    private boolean notLiteral(String cmd) {
    	Matcher m = notLiteral.matcher(cmd);
    	boolean match = m.find();
    	//printMsg(match, "<notLiteral>", cmd, "not literal");
    	return match;
    }
    
    private boolean boolExpr(String cmd) {
    	Matcher m = bool.matcher(cmd);
    	boolean match = m.find();
    	Matcher n = var.matcher(cmd);
    	boolean match2 = n.find();
    	//Condition cond = new Condition();
    	//boolean condBool = cond.parseCmd(cmd); 
    	return match || match2;// condBool;
    }
    
    private void printMsg(boolean match, String ntName, String cmd, String item) {
		if (match)
			result += ntName + ": " + cmd + "\n";
		else
			result = "Failed to parse: {" + cmd + "} is not a valid " + item + ".\n";
	}
    
    public String translate(String string) {
    	String result = "";
    	
    	
    	
    	return result;
    }
}
