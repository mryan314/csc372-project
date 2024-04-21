import java.util.HashMap;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForLoops {
    private String loopRegex = "\\s*loop\\(([^,{}]+)(?:,([^{}]+))?\\)\\s*\\{(.+)\\}\\s*";
    private Pattern loopPattern = Pattern.compile(loopRegex, Pattern.DOTALL);
    private Pattern intVal = Pattern.compile("^\\d+$");
	private Pattern var = Pattern.compile("^[a-zA-Z][a-zA-z_0-9]*$");
    private MultDiv multDiv1 = new MultDiv();
    private MultDiv multDiv2 = new MultDiv();
    private Condition condition = new Condition();
    private Line line = null;
    private HashMap<String,String> varTypes;
    private HashMap<String,FuncInfo> funcs;
    
    public boolean match;
    public String result = "";
    public String translated = "";

    public ForLoops() {
        line = new Line();
    }

    public ForLoops(HashMap<String,String> varTypes, HashMap<String,FuncInfo> funcs) {
        this.varTypes = varTypes;
        this.funcs = funcs;
        line = new Line(varTypes, funcs);
    }


    public boolean parseCmd(String cmd) {
        match = translateLoop(cmd);
        return match;
    }

    public boolean translateLoop(String input) {
        Matcher matcher = loopPattern.matcher(input);
        if (matcher.matches()) {
            String firstExpression = matcher.group(1).trim();
            String secondExpression = matcher.group(2) != null ? matcher.group(2).trim() : null;
            String block = matcher.group(3).trim();

            if (secondExpression != null) {
                Matcher v1 = var.matcher(firstExpression);
                Matcher v2 = var.matcher(secondExpression);
                Matcher i1 = intVal.matcher(firstExpression);
                Matcher i2 = intVal.matcher(secondExpression);
                if (multDiv1.parseCmd(firstExpression) && multDiv2.parseCmd(secondExpression)) {
                    result += "<loop>: loop(" + firstExpression + ", " + secondExpression + ") {";
                    result += multDiv1.result + multDiv2.result;
                    translated += "for (int i=" + multDiv1.translated + "; i<" + multDiv2.translated + "; i++) {\n";
                }
                else if ((v1.find() && v2.find()) || (v1.find() && i2.find()) || (i1.find() && v2.find()) || (i1.find() && i2.find())) {
                    result += "<loop>: loop(" + firstExpression + ", " + secondExpression + ") {";
                    result += "<loop_val>: " + firstExpression + "\n<loop_val>: " + secondExpression;
                    translated += "for (int i_=" + firstExpression + "; i_<" + secondExpression + "; i_++) {\n";
                }
                else {
                    result = "Failed to parse: { " + input.trim() + " } " + "is not a recognized loop definition.\n";
                    translated = "";
                    return false;
                }
            } else {
                Matcher v = var.matcher(firstExpression);
                Matcher i = intVal.matcher(firstExpression);
                if (condition.parseCmd(firstExpression)) {
                    result += "<loop>: loop(" + firstExpression + ") {";
                    result += condition.result;
                    translated += "while (" + condition.translated + ") {\n";
                }
                else if (multDiv1.parseCmd(firstExpression)) {
                    result += "<loop>: loop(" + firstExpression + ") {";
                    result += condition.result;
                    translated += "for (int i=0; i<" + multDiv1.translated + "; i++) {\n";
                }
                else if (v.find()) {
                    result += "<loop>: loop(" + firstExpression + ") {";
                    result += "<var>: " + firstExpression;
                    translated += "while (" + firstExpression + "!= 0) {\n";
                }
                else if (i.find()) {
                    result += "<loop>: loop(" + firstExpression + ") {";
                    result += "<int>: " + firstExpression;
                    translated += "for (int i=0; i<" + firstExpression + "; i++) {\n";
                }
                else {
                    result = "Failed to parse: { " + input.trim() + " } " + "is not a recognized loop definition.\n";
                    translated = "";
                    return false;
                }
            }

            result += "<block>: ";
            String[] lines = block.split("\n");
            int i=0;
            while (i < lines.length) {
                if (lines[i].contains("loop(")) {
					int loopBlocklen = findBlock(i+1, lines);
                    String loopBlock = buildBlock(i, loopBlocklen, lines);
					ForLoops loop = new ForLoops(varTypes, funcs);
					if (loop.parseCmd(loopBlock)) {
                        result += loop.result;
                        translated += loop.translated;
					}
                    i = loopBlocklen + 1;
				}
				else if (lines[i].contains("if ")) {
					int ifElseBlocklen = findBlock(i+1, lines);
                    String ifElseBlock = buildBlock(i, ifElseBlocklen, lines);
					CondExpr condExpr = new CondExpr(varTypes,funcs);
					if (condExpr.parseCmd(ifElseBlock)) {
                        result += condExpr.result;
                        translated += condExpr.translated;
					}
                    i = ifElseBlocklen + 1;
				}
                else {
                    line.parseCmd(lines[i]);
                    if (!line.match) {
                        result = line.result;
                        return false;
                    }
                    result += line.result;
                    translated += line.translated;
                    i += 1;
                }
            }

            result += "\n";
            translated +="}\n";
            return true;
        } else {
            result = "Failed to parse: {" + input + "} is not a valid loop expression.";
            return false;
        }
    }

    public int findBlock(int index, String[] in) {
		Stack<String> stack = new Stack<>();
		stack.push("{");

		while (!stack.empty()) {
			String cur = in[index];
			if (cur.contains("{")) {
				stack.push("{");
			}
			if (cur.contains("}")) {
				stack.pop();
			}
            index += 1;

			if (cur.contains("}") && index+1 < in.length && in[index+1].contains("else")) {
				index += findBlock(index, in);
			}
		}
		return index-1;
	}

    public String buildBlock(int start, int end, String[] list) {
        result = "";
        for (int i=start; i<= end; i++) {
            result += list[i];
        }

        return result;
    }

}
