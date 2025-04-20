package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RulesToSqlConverter {

    static class Rule {
        String ruleId;
        String department;  // New field for department
        Map<String, String> ruleCase = new HashMap<>();
        Map<String, String> ruleStep = new HashMap<>();
        String destination;
        String extraAct; // Optional

        public String toSqlInsert() {
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO FD_BUSINESS_RULES (rule_id, department, rule_case, rule_step, rule_destination, created_by, action) VALUES (");

            // Rule ID
            sql.append("'").append(ruleId).append("', ");

            // Department - first 3 characters of rule ID
            sql.append("'").append(department).append("', ");

            // Rule Case - Convert map to string representation
            StringBuilder ruleCaseStr = new StringBuilder();
            for (Map.Entry<String, String> entry : ruleCase.entrySet()) {
                if (ruleCaseStr.length() > 0) {
                    ruleCaseStr.append(", ");
                }
                ruleCaseStr.append(entry.getKey()).append("==").append(entry.getValue());
            }
            sql.append("'").append(ruleCaseStr).append("', ");

            // Rule Step - Convert map to string representation
            StringBuilder ruleStepStr = new StringBuilder();
            for (Map.Entry<String, String> entry : ruleStep.entrySet()) {
                if (ruleStepStr.length() > 0) {
                    ruleStepStr.append(", ");
                }
                ruleStepStr.append(entry.getKey()).append("==").append(entry.getValue());
            }
            sql.append("'").append(ruleStepStr).append("', ");

            // Destination
            sql.append("'").append(destination).append("', ");

            // created by
            sql.append("'").append("SONORA").append("', ");
            // Extra Act (could be NULL)
            if (extraAct != null) {
                sql.append("'").append(extraAct).append("'");
            } else {
                sql.append(" ");
            }


            sql.append(");");
            return sql.toString();
        }
    }

    public static void main(String[] args) {
/*        if (args.length < 2) {
            System.out.println("Usage: java RulesToSqlConverter <input_rules_file> <output_sql_file>");
            return;
        }*/

        String inputFile = "E:\\Workspace\\Rules\\test_rules.txt";
        String outputFile = "E:\\Workspace\\Rules\\test_rules.sql";

        try {
            List<Rule> rules = parseRulesFile(inputFile);
            writeSqlFile(rules, outputFile);
            System.out.println("Successfully converted " + rules.size() + " rules to SQL statements in " + outputFile);
        } catch (IOException e) {
            System.err.println("Error processing files: " + e.getMessage());
        }
    }

    private static List<Rule> parseRulesFile(String filePath) throws IOException {
        List<Rule> rules = new ArrayList<>();
        StringBuilder fileContent = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line).append("\n");
            }
        }

        String content = fileContent.toString();

        // Pattern to match each rule block
        Pattern rulePattern = Pattern.compile("rule\\s+\"([^\"]+)\"\\s+dialect\\s+\"mvel\"\\s+when\\s+(.*?)\\s+then\\s+(.*?)\\s+end",
                Pattern.DOTALL);
        Matcher ruleMatcher = rulePattern.matcher(content);

        while (ruleMatcher.find()) {
            Rule rule = new Rule();
            rule.ruleId = ruleMatcher.group(1);

            // Extract department (first 3 characters of rule ID)
            if (rule.ruleId.length() >= 3) {
                rule.department = rule.ruleId.substring(0, 3);
            } else {
                rule.department = rule.ruleId; // Fall back to the full ID if it's less than 3 chars
            }

            String whenBlock = ruleMatcher.group(2).trim();
            String thenBlock = ruleMatcher.group(3).trim();

            // Parse the when block for RuleCase and RuleStep
            parseWhenBlock(whenBlock, rule);

            // Parse the then block for destination and extra actions
            parseThenBlock(thenBlock, rule);

            rules.add(rule);
        }

        return rules;
    }

    private static void parseWhenBlock(String whenBlock, Rule rule) {
        // Pattern for RuleCase
        Pattern ruleCasePattern = Pattern.compile("RuleCase\\((.*?)\\)");
        Matcher ruleCaseMatcher = ruleCasePattern.matcher(whenBlock);

        if (ruleCaseMatcher.find()) {
            String ruleCaseContent = ruleCaseMatcher.group(1);
            String[] conditions = ruleCaseContent.split(",");

            for (String condition : conditions) {
                condition = condition.trim();
                if (condition.contains("==")) {
                    String[] parts = condition.split("==");
                    String key = parts[0].trim();
                    // Keep the quotes in the value
                    String value = parts[1].trim();
                    rule.ruleCase.put(key, value);
                }
            }
        }

        // Pattern for RuleStep
        Pattern ruleStepPattern = Pattern.compile("RuleStep\\((.*?)\\)");
        Matcher ruleStepMatcher = ruleStepPattern.matcher(whenBlock);

        if (ruleStepMatcher.find()) {
            String ruleStepContent = ruleStepMatcher.group(1);
            String[] conditions = ruleStepContent.split(",");

            for (String condition : conditions) {
                condition = condition.trim();
                if (condition.contains("==")) {
                    String[] parts = condition.split("==");
                    String key = parts[0].trim();
                    // Keep the quotes in the value
                    String value = parts[1].trim();
                    rule.ruleStep.put(key, value);
                }
            }
        }
    }

    private static void parseThenBlock(String thenBlock, Rule rule) {
        // Pattern for setDestination
        Pattern destinationPattern = Pattern.compile("setDestination\\(\"([^\"]+)\"\\)");
        Matcher destinationMatcher = destinationPattern.matcher(thenBlock);

        if (destinationMatcher.find()) {
            rule.destination = destinationMatcher.group(1);
        }

        // Pattern for possible extra action (setExtraAct)
        Pattern extraActPattern = Pattern.compile("setExtraAct\\(\"([^\"]+)\"\\)");
        Matcher extraActMatcher = extraActPattern.matcher(thenBlock);

        if (extraActMatcher.find()) {
            rule.extraAct = extraActMatcher.group(1);
        }
    }

    private static void writeSqlFile(List<Rule> rules, String outputPath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            // Write SQL file header
            writer.write("-- Generated SQL statements for FD_BUSINESS_RULES\n\n");

            // Write each rule as an SQL INSERT statement
            for (Rule rule : rules) {
                writer.write(rule.toSqlInsert());
                writer.newLine();
                writer.newLine();
            }
        }
    }
}


