package cecs544.metrics;

import java.util.*;

public class ProjectModel {
    public String projectName;
    public String creatorName;

    public String productName;
    public String comments;

    public String language;
    public FPState fpState;

    public static ProjectModel newEmpty(String projectName, String creatorName, String productName, String comments) {
        ProjectModel m = new ProjectModel();
        m.projectName = (projectName == null || projectName.isBlank()) ? "Untitled" : projectName;
        m.creatorName = (creatorName == null || creatorName.isBlank()) ? "Unknown" : creatorName;

        m.productName = (productName == null || productName.isBlank()) ? "Unnamed Product" : productName;
        m.comments = (comments == null) ? "" : comments;

        m.language = null;
        m.fpState = null;
        return m;
    }

    public String toJson() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("projectName", projectName);
        root.put("creatorName", creatorName);

        // NEW fields persisted
        root.put("productName", productName);
        root.put("comments", comments);

        root.put("language", language);
        root.put("fpState", (fpState != null) ? fpState.toMap() : null);

        return JsonMini.stringify(root);
    }

    @SuppressWarnings("unchecked")
    public static ProjectModel fromJson(String json) {
        Object parsed = JsonMini.parse(json);
        if (!(parsed instanceof Map<?, ?> raw)) {
            throw new IllegalArgumentException("Invalid project file.");
        }

        Map<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<?, ?> e : raw.entrySet()) {
            map.put(String.valueOf(e.getKey()), e.getValue());
        }

        ProjectModel m = new ProjectModel();

        Object pn = map.get("projectName");
        m.projectName = (pn instanceof String s && !s.isBlank()) ? s : "Untitled";

        Object cn = map.get("creatorName");
        m.creatorName = (cn instanceof String s && !s.isBlank()) ? s : "Unknown";

        Object pr = map.get("productName");
        m.productName = (pr instanceof String s && !s.isBlank()) ? s : "Unnamed Product";

        Object cm = map.get("comments");
        m.comments = (cm instanceof String s) ? s : "";

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
            s.language = (map.get("language") instanceof String str) ? str : null;
            s.counts = toIntArray(map.get("counts"), 5);
            s.complexities = toIntArray(map.get("complexities"), 5);
            s.vafValues = toIntArray(map.get("vafValues"), 14);
            s.totalWeighted = (map.get("totalWeighted") instanceof Number n) ? n.intValue() : 0;
            s.vafSum = (map.get("vafSum") instanceof Number n) ? n.intValue() : 0;
            s.fpFormatted = (map.get("fpFormatted") instanceof String str) ? str : "0.0";
            return s;
        }

        private static List<Integer> toList(int[] arr) {
            List<Integer> out = new ArrayList<>();
            for (int v : arr) out.add(v);
            return out;
        }

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