package org.example.Object;

public class RuleExecutionResult {
    private String ruleName;
    private String destination;

    private String extraAct;

    public RuleExecutionResult(String ruleName, String destination,String  extraAct) {
        this.ruleName = ruleName;
        this.destination = destination;
        this.extraAct = extraAct;
    }

    public String getRuleName() {
        return ruleName;
    }

    public String getExtraAct() {
        return extraAct;
    }

    public String getDestination() {
        return destination;
    }
}