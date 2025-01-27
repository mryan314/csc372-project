import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FuncCall {
	public boolean match;
	public String result;
	public String translated;
	public FuncInfo func = null;

	private String args = null;
	private HashMap<String, String> varTypes = null;
	private HashMap<String, FuncInfo> funcs = null;

	private Pattern func_name = Pattern.compile("^([a-zA-Z])+\\w*$");
	private Pattern func_call = Pattern.compile("^(.+)\\s*\\((.*)\\)\\s*$");
	private Pattern intVal = Pattern.compile("^\\d+$");
	private Pattern bool = Pattern.compile("^true$|^false$");
	private Pattern string = Pattern.compile("^\"[^\"]*\"$");
	private Pattern var = Pattern.compile("^[a-zA-Z][a-zA-z_0-9]*$");

	public FuncCall(HashMap<String, String> varTypes, HashMap<String, FuncInfo> funcs) {
		this.varTypes = varTypes;
		this.funcs = funcs;
	}


	/*
	 * Parses a function call in our language.
	 */
	public boolean parseCmd(String cmd) {
		Matcher m = func_call.matcher(cmd);
		match = false;
		result = "";
		translated = "";
		args = "";
		func = null;
		if (m.find()) {
			result += "<func_call>: " + cmd + "\n";
			String name = m.group(1).trim();
			match = parseName(name);
			match = match && parseArgs(m.group(2).trim());
		}
        else {
            result = "Failed to parse '" + cmd + "'. Invalid func_call expression.\n";
        }

		return match;
	}
	

	/*
	 * Parses a function name by checking if it is a valid name.
	 */
	private boolean parseName(String cmd) {
		Matcher m = func_name.matcher(cmd);
		if (m.find()) {
			func = funcs.get(cmd);
			if (func != null) { // method must exist to call
				result += "<func>: " + cmd + "\n";
				translated += cmd;
				return true;
			} else {
				result = "Failed to parse: { " + cmd + " } Function does not exist.\n";
			}
		} else {
			result = "Failed to parse: { " + cmd + " } Invalid function name.\n";
		}
		return false;
	}

	/*
	 * Parses and validates the arguments of a function call.
	 */
	private boolean parseArgs(String cmd) {
		boolean match = false;
		String[] argsArr;

		if (cmd.equals("")) // handles if no arguments given
			argsArr = new String[0];
		else
			argsArr = cmd.split(","); // splits arguments by commas

		if (argsArr.length == func.params.size()) {
			match = true;
			for (int i = 0; i < argsArr.length; i++) {
				String param = func.params.get(i);
				String argType = getType(argsArr[i]);
				if (!(func.paramTypes.get(param).equals(argType))) {
					result = "Failed to parse: '" + argsArr[i] + "'. Not a " + argType + " value.\n";
					return false;
				}
			}
			// updates values if all args are valid
			args = String.join(", ", argsArr);
			result += "<args>: " + args + "\n";
			translated += "(" + args + ")";
		}
		else {
			result = "Failed to parse: { " + cmd + " } contains too little or too many arguments\n";
			return false;
		}

		return match;
	}

    /*
     * Gets the type of an argument.
     */
    private String getType(String arg) {
		MultDiv md = new MultDiv(varTypes);
		Condition cond = new Condition(varTypes);

		if (md.parseCmd(arg) || intVal.matcher(arg).find()) {
			return "int";
		} else if (cond.parseCmd(arg) || bool.matcher(arg).find()) {
			return "boolean";
		} else if (string.matcher(arg).find()) {
			return "String";
		} else if (var.matcher(arg).find() && varTypes.containsKey(arg)) {
			return varTypes.get(arg);
		}

		return ""; 
	}
}
