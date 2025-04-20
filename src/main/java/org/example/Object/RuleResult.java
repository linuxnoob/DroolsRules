package org.example.Object;

public class RuleResult {
    private String ruleName;
    private String destination;

    private String extraAct;

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getRuleName() {
        return ruleName;
    }

    public String getExtraAct() {
        return extraAct;
    }

    public void setExtraAct(String extraAct) {
        this.extraAct = extraAct;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String toString(){
        return "RuleResult [rulename=" + ruleName + ", destination =" + destination + ", extraAct=" + extraAct + "]";
    }

    public String toTxt(){
        return "        RuleResult result = new RuleResult();result.setDestination(\"" + getDestination().replaceAll("\"","") +"\");insert(result)" + "\n";
    }
}