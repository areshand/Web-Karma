package edu.isi.karma.cleaning;

import java.util.HashMap;

import edu.isi.karma.cleaning.grammartree.Program;

public class ProgramRule {
	public static final String defaultclasslabel = "attr_0";
	public HashMap<String, InterpreterType> rules = new HashMap<String, InterpreterType>();
	public HashMap<String, String> strRules = new HashMap<String, String>();
	public PartitionClassifierType pClassifier;
	public static Interpretor itInterpretor;
	public String signString = "";
	public static final String IDENTITY = "substr(value,'START','END')";
	public boolean nullRule = false;
	public Program program = null;
	
	public ProgramRule(Program prog) {
		program = prog;
		this.pClassifier = prog.classifier;
		initInterpretor();
	}

	public ProgramRule(String rule) {
		initInterpretor();
		InterpreterType worker = itInterpretor.create(rule);
		rules.put("attr_0", worker);

	}

	public String transform(String value) {
		InterpreterType worker= this.getRuleForValue(value);
		String s2 = worker.execute(value);
		return s2;
	}

	public void initInterpretor() {
		if (itInterpretor == null)
			itInterpretor = new Interpretor();
	}

	public InterpreterType getRuleForValue(String value) {
		String c = getClassForValue(value);

		return getWorkerForClass(c);
	}

	public String getClassForValue(String value) {
		String labelString = defaultclasslabel;
		if (value.length() == 0)
			return labelString;
		if (pClassifier != null) {
			labelString = pClassifier.getLabel(value);
		}
		return labelString;

	}

	public InterpreterType getWorkerForClass(String category) {
		if (category != "") {
			InterpreterType rule = this.rules.get(category);
			return rule;
		} else {
			return rules.values().iterator().next();
		}
	}

	public void updateClassworker(String category, String newRule) {
		this.rules.remove(category);
		this.strRules.remove(category);
		addRule(category, newRule);

	}

	public void addRule(String partition, String rule) {
		InterpreterType worker = itInterpretor.create(rule);
		this.signString += rule;
		rules.put(partition, worker);
		strRules.put(partition, rule);
	}

	public String getStringRule(String par) {
		return this.strRules.get(par);
	}

	public String toString() {
		String res = "";
		for (String key : strRules.keySet()) {
			res += String.format("%s:%s\n", key, strRules.get(key));
		}
		return res;
	}

	public static void main(String[] args) {

	}
}
