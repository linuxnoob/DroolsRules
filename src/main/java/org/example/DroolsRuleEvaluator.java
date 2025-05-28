package org.example;



import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Object.RuleCase;
import org.example.Object.RuleExecutionResult;
import org.example.Object.RuleResult;
import org.example.Object.RuleStep;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class DroolsRuleEvaluator {
    public static void main(String[] args) {
        try {

            System.setProperty("mvel.strict", "false");
            String jsonInput = "{\n" +
                    "  \"GROUPID\" : \"GRP_UNI_MED\",\n" +
                    "  \"ACTIVITY\" : \"COM_START\",\n" +
                    "  \"MEDICAL_SKIP_APPROVAL\" : \"YES\"\n" +
                    "}";

            Map<String, String> params = parseJsonInput(jsonInput);
            RuleExecutionResult result = evaluateRules(params);
            System.out.println(createJsonResponse(result));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static RuleExecutionResult evaluateRules(Map<String, String> parameters) {
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kfs = kieServices.newKieFileSystem();

        try (InputStream drlStream = DroolsRuleEvaluator.class.getClassLoader().getResourceAsStream("rules.drl")) {
            if (drlStream == null) {
                throw new RuntimeException("rules.drl not found in classpath");
            }
            String drlContent = new String(drlStream.readAllBytes());
            kfs.write("src/main/resources/rules.drl", drlContent);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load rules.drl", e);
        }

        KieBuilder builder = kieServices.newKieBuilder(kfs);
        builder.buildAll();

        if (builder.getResults().hasMessages(Message.Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n" + builder.getResults().toString());
        }

        KieModule kieModule = builder.getKieModule();
        KieContainer kContainer = kieServices.newKieContainer(kieModule.getReleaseId());
        KieSession session = kContainer.newKieSession();

        try {
            // Prepare input facts
            RuleCase ruleCase = new RuleCase();
            ruleCase.setGROUPID(parameters.getOrDefault("GROUPID", ""));
            ruleCase.setACTIVITY(parameters.getOrDefault("ACTIVITY", ""));

            RuleStep ruleStep = new RuleStep();
            ruleStep.setMEDICAL_SKIP_APPROVAL(parameters.getOrDefault("MEDICAL_SKIP_APPROVAL", ""));

            List<RuleResult> resultList = new ArrayList<>();
            session.setGlobal("resultList", resultList);

            session.insert(ruleCase);
            session.insert(ruleStep);
            session.fireAllRules();

            if (!resultList.isEmpty()) {
                RuleResult res = resultList.get(0); // use first match
                return new RuleExecutionResult(res.getRuleName(), res.getDestination(), res.getExtraAct());
            } else {
                return new RuleExecutionResult("UNKNOWN", "NO_MATCH", null);
            }
        } finally {
            session.dispose();
        }
    }

    private static Map<String, String> parseJsonInput(String jsonInput) {
        try {
            return new ObjectMapper().readValue(jsonInput, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse input JSON", e);
        }
    }

    private static String createJsonResponse(RuleExecutionResult result) {
        try {
            return new ObjectMapper().writeValueAsString(result);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create JSON output", e);
        }
    }
}
