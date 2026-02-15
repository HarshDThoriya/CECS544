package cecs544.metrics;

import java.util.*;

public class ProjectModel {
    public String projectName;
    public String creatorName;
    public String language; // current global language (optional)
    public FPState fpState; // Iteration 1 stores just FP state

    public static ProjectModel newEmpty(String projectName, String creatorName) {
        ProjectModel m = new ProjectModel();
        m.projectName = (projectName == null || projectName.isBlank()) ? "Untitled" : projectName;
        m.creatorName = (creatorName == null || creatorName.isBlank()) ? "Unknown" : creatorName;
        m.language = null;
        m.fpState = null;
        return m;
    }

    public String toJson() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("projectName", projectName);
        root.put("creatorName", creatorName);
        root.put("language", language);

        if (fpState != null) {
            root.put("fpState", fpState.toMap());
        } else {
            root.put("fpState", null);
        }

        return JsonMini.stringify(root);
    }

    @SuppressWarnings("unchecked")
    public static ProjectModel fromJson(String json) {
        Object parsed = JsonMini.parse(json);
        if (!(parsed instanceof Map<?, ?> raw)) {
            throw new IllegalArgumentException("Invalid project file.");
        }

        // Convert to Map<String, Object> safely
        Map<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<?, ?> e : raw.entrySet()) {
            map.put(String.valueOf(e.getKey()), e.getValue());
        }

        ProjectModel m = new ProjectModel();

        Object pn = map.get("projectName");
        m.projectName = (pn instanceof String s && !s.isBlank()) ? s : "Untitled";

        Object cn = map.get("creatorName");
        m.creatorName = (cn instanceof String s && !s.isBlank()) ? s : "Unknown";

        Object lang = map.get("language");
        m.language = (lang instanceof String s && !s.isBlank()) ? s : null;

        Object fps = map.get("fpState");
        if (fps instanceof Map<?, ?> fpRaw) {
            Map<String, Object> fpMap = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : fpRaw.entrySet()) {
                fpMap.put(String.valueOf(e.getKey()), e.getValue());
            }
            m.fpState = FPState.fromMap(fpMap);
        } else {
            m.fpState = null;
        }

        return m;
    }


    public static class FPState {
        public String language;
        public int[] counts = new int[5];
        public int[] complexities = new int[5]; // 0 simple, 1 avg, 2 complex
        public int[] vafValues = new int[14];
        public int totalWeighted;
        public int vafSum;
        public String fpFormatted;

        public Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("language", language);
            m.put("counts", toList(counts));
            m.put("complexities", toList(complexities));
            m.put("vafValues", toList(vafValues));
            m.put("totalWeighted", totalWeighted);
            m.put("vafSum", vafSum);
            m.put("fpFormatted", fpFormatted);
            return m;
        }

        public static FPState fromMap(Map<String, Object> map) {
            FPState s = new FPState();
            s.language = (String) map.get("language");
            s.counts = toIntArray(map.get("counts"), 5);
            s.complexities = toIntArray(map.get("complexities"), 5);
            s.vafValues = toIntArray(map.get("vafValues"), 14);
            s.totalWeighted = ((Number) map.getOrDefault("totalWeighted", 0)).intValue();
            s.vafSum = ((Number) map.getOrDefault("vafSum", 0)).intValue();
            s.fpFormatted = (String) map.getOrDefault("fpFormatted", "0.0");
            return s;
        }

        private static List<Integer> toList(int[] arr) {
            List<Integer> out = new ArrayList<>();
            for (int v : arr) out.add(v);
            return out;
        }

        @SuppressWarnings("unchecked")
        private static int[] toIntArray(Object o, int n) {
            int[] out = new int[n];
            if (o instanceof List<?> list) {
                for (int i = 0; i < Math.min(n, list.size()); i++) {
                    Object v = list.get(i);
                    if (v instanceof Number num) out[i] = num.intValue();
                }
            }
            return out;
        }
    }
}
