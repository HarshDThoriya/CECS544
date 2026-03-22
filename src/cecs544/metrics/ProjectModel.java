package cecs544.metrics;

import java.util.*;

public class ProjectModel {
    public String projectName;
    public String creatorName;
    public String productName;
    public String comments;

    public String language; // current global language (optional)

    // MULTI-PANE support (required by step 28: both panes restored)
    public List<FPPaneEntry> fpPanes = new ArrayList<>();

    public static ProjectModel newEmpty(String projectName, String creatorName, String productName, String comments) {
        ProjectModel m = new ProjectModel();
        m.projectName = projectName;
        m.creatorName = creatorName;
        m.productName = productName;
        m.comments = comments == null ? "" : comments;
        m.language = null;
        m.fpPanes = new ArrayList<>();
        return m;
    }

    public String toJson() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("projectName", projectName);
        root.put("creatorName", creatorName);
        root.put("productName", productName);
        root.put("comments", comments);
        root.put("language", language);

        List<Object> panes = new ArrayList<>();
        for (FPPaneEntry p : fpPanes) {
            panes.add(p.toMap());
        }
        root.put("fpPanes", panes);

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

        m.projectName = (map.get("projectName") instanceof String s) ? s : "Untitled";
        m.creatorName = (map.get("creatorName") instanceof String s) ? s : "";
        m.productName = (map.get("productName") instanceof String s) ? s : "";
        m.comments = (map.get("comments") instanceof String s) ? s : "";
        m.language = (map.get("language") instanceof String s) ? s : null;

        m.fpPanes = new ArrayList<>();
        Object panesObj = map.get("fpPanes");
        if (panesObj instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof Map<?, ?> paneRaw) {
                    Map<String, Object> paneMap = new LinkedHashMap<>();
                    for (Map.Entry<?, ?> e : paneRaw.entrySet()) {
                        paneMap.put(String.valueOf(e.getKey()), e.getValue());
                    }
                    m.fpPanes.add(FPPaneEntry.fromMap(paneMap));
                }
            }
        }

        return m;
    }

    // ---------- FP Pane entry ----------
    public static class FPPaneEntry {
        public String tabName;
        public FPState state;

        public Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("tabName", tabName);
            m.put("state", state != null ? state.toMap() : null);
            return m;
        }

        @SuppressWarnings("unchecked")
        public static FPPaneEntry fromMap(Map<String, Object> map) {
            FPPaneEntry e = new FPPaneEntry();
            e.tabName = (map.get("tabName") instanceof String s) ? s : "Function Points";

            Object st = map.get("state");
            if (st instanceof Map<?, ?> raw) {
                Map<String, Object> sMap = new LinkedHashMap<>();
                for (Map.Entry<?, ?> en : raw.entrySet()) {
                    sMap.put(String.valueOf(en.getKey()), en.getValue());
                }
                e.state = FPState.fromMap(sMap);
            } else {
                e.state = new FPState();
            }
            return e;
        }
    }

    // ---------- FP State ----------
    public static class FPState {
        public String language;
        public int[] counts = new int[5];
        public int[] complexities = new int[5]; // 0 simple, 1 avg, 2 complex
        public int[] vafValues = new int[14];

        public int totalWeighted;
        public int vafSum;
        public String fpFormatted;
        public String codeSizeFormatted;

        public Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("language", language);
            m.put("counts", toList(counts));
            m.put("complexities", toList(complexities));
            m.put("vafValues", toList(vafValues));
            m.put("totalWeighted", totalWeighted);
            m.put("vafSum", vafSum);
            m.put("fpFormatted", fpFormatted);
            m.put("codeSizeFormatted", codeSizeFormatted);
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
            s.fpFormatted = (map.get("fpFormatted") instanceof String str) ? str : "";
            s.codeSizeFormatted = (map.get("codeSizeFormatted") instanceof String str) ? str : "";
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