package ecommerce.catalogue.controller;

public class Criteria {
	
	private String field;
	
	private Input input;
	
	public Criteria() {
	}

	public Criteria(String field, Input input) {
		this.field = field;
		this.input = input;
	}

	public String getField() {
		return field;
	}

	public Input getInput() {
		return input;
	}
	
}

class Input {
	
	private String pattern;
	
	private Integer fuzzyLevel;
	
	public Input() {
	}

	public Input(String pattern) {
		this.pattern = pattern;
	}

	public Integer getFuzzyLevel() {
		return fuzzyLevel;
	}

	public void setFuzzyLevel(Integer fuzzyLevel) {
		this.fuzzyLevel = fuzzyLevel;
	}

	public String getPattern() {
		return pattern;
	}
	
}
