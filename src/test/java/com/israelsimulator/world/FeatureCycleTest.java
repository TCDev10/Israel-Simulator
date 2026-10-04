package com.israelsimulator.world;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class FeatureCycleTest {

    record FeatureData(int featureIndex, int step, String feature) implements Comparable<FeatureData> {
        @Override
        public int compareTo(FeatureData o) {
            int c = Integer.compare(this.step, o.step);
            return c != 0 ? c : Integer.compare(this.featureIndex, o.featureIndex);
        }
    }

    @Test
    @DisplayName("Verify no feature order cycles exist across all custom biomes")
    void verifyNoFeatureOrderCycles() {
        String[] biomes = {
            "mediterranean_coast",
            "israeli_agriculture",
            "urban_area",
            "jerusalem",
            "judean_desert",
            "dead_sea"
        };

        Map<String, List<List<String>>> biomeFeatures = new LinkedHashMap<>();
        Gson gson = new Gson();

        for (String b : biomes) {
            InputStream is = getClass().getResourceAsStream("/data/israel_simulator/worldgen/biome/" + b + ".json");
            JsonObject obj = gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), JsonObject.class);
            JsonArray featuresArr = obj.getAsJsonArray("features");
            List<List<String>> steps = new ArrayList<>();
            for (JsonElement stepElem : featuresArr) {
                List<String> stepFeatures = new ArrayList<>();
                for (JsonElement fElem : stepElem.getAsJsonArray()) {
                    stepFeatures.add(fElem.getAsString());
                }
                steps.add(stepFeatures);
            }
            biomeFeatures.put(b, steps);
        }

        Map<String, Integer> featureIndex = new HashMap<>();
        int nextIndex = 0;
        Map<FeatureData, Set<FeatureData>> edges = new TreeMap<>();

        for (var entry : biomeFeatures.entrySet()) {
            List<FeatureData> featureList = new ArrayList<>();
            List<List<String>> steps = entry.getValue();
            for (int step = 0; step < steps.size(); step++) {
                for (String f : steps.get(step)) {
                    int idx;
                    if (!featureIndex.containsKey(f)) {
                        idx = nextIndex++;
                        featureIndex.put(f, idx);
                    } else {
                        idx = featureIndex.get(f);
                    }
                    featureList.add(new FeatureData(idx, step, f));
                }
            }

            for (int i = 0; i < featureList.size(); i++) {
                Set<FeatureData> data = edges.computeIfAbsent(featureList.get(i), k -> new TreeSet<>());
                if (i < featureList.size() - 1) {
                    data.add(featureList.get(i + 1));
                }
            }
        }

        Set<FeatureData> visited = new HashSet<>();
        Set<FeatureData> stack = new LinkedHashSet<>();

        boolean cycleFound = false;
        StringBuilder cycleReport = new StringBuilder();
        for (FeatureData node : edges.keySet()) {
            if (findCycle(node, edges, visited, stack)) {
                cycleFound = true;
                cycleReport.append("Cycle detected:\n");
                for (FeatureData fd : stack) {
                    cycleReport.append("  Step ").append(fd.step()).append(": ").append(fd.feature()).append("\n");
                }
                break;
            }
        }

        assertFalse(cycleFound, "Feature order cycle found in biomes: " + cycleReport);
    }

    private boolean findCycle(FeatureData node, Map<FeatureData, Set<FeatureData>> edges, Set<FeatureData> visited, Set<FeatureData> stack) {
        if (stack.contains(node)) {
            return true;
        }
        if (visited.contains(node)) {
            return false;
        }

        visited.add(node);
        stack.add(node);

        Set<FeatureData> neighbors = edges.getOrDefault(node, Collections.emptySet());
        for (FeatureData neighbor : neighbors) {
            if (findCycle(neighbor, edges, visited, stack)) {
                return true;
            }
        }

        stack.remove(node);
        return false;
    }
}
