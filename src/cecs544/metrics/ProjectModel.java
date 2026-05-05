package cecs544.metrics;

import java.util.*;

public class ProjectModel {
    public String projectName;
    public String creatorName;
    public String productName;
    public String comments;

    public String language;

    public List<FPPaneEntry> fpPanes = new ArrayList<>();
    public List<UCPPaneEntry> ucpPanes = new ArrayList<>();

    public static ProjectModel newEmpty(String projectName, String creatorName, String productName, String comments) {
        ProjectModel m = new ProjectModel();
        m.projectName = projectName;
        m.creatorName = creatorName;
        m.productName = productName;
        m.comments = comments == null ? "" : comments;
        m.language = null;
        m.fpPanes = new ArrayList<>();
        m.ucpPanes = new ArrayList<>();
        return m;
    }

    public String toJson() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("projectName", projectName);
        root.put("creatorName", creatorName);
        root.put("productName", productName);
        root.put("comments", comments);
        root.put("language", language);

        List<Object> fpPaneMaps = new ArrayList<>();
        for (FPPaneEntry p : fpPanes) {
            fpPaneMaps.add(p.toMap());
        }
        root.put("fpPanes", fpPaneMaps);

        List<Object> ucpPaneMaps = new ArrayList<>();
        for (UCPPaneEntry p : ucpPanes) {
            ucpPaneMaps.add(p.toMap());
        }
        root.put("ucpPanes", ucpPaneMaps);

        return JsonMini.stringify(root);
    }

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
        Object fpPanesObj = map.get("fpPanes");
        if (fpPanesObj instanceof List<?> list) {
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

        m.ucpPanes = new ArrayList<>();
        Object ucpPanesObj = map.get("ucpPanes");
        if (ucpPanesObj instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof Map<?, ?> paneRaw) {
                    Map<String, Object> paneMap = new LinkedHashMap<>();
                    for (Map.Entry<?, ?> e : paneRaw.entrySet()) {
                        paneMap.put(String.valueOf(e.getKey()), e.getValue());
                    }
                    m.ucpPanes.add(UCPPaneEntry.fromMap(paneMap));
                }
            }
        } else {
            Object legacyUcpObj = map.get("ucpState");
            if (legacyUcpObj instanceof Map<?, ?> rawUcp) {
                Map<String, Object> ucpMap = new LinkedHashMap<>();
                for (Map.Entry<?, ?> e : rawUcp.entrySet()) {
                    ucpMap.put(String.valueOf(e.getKey()), e.getValue());
                }
                UCPPaneEntry entry = new UCPPaneEntry();
                entry.tabName = (map.get("ucpTabName") instanceof String s) ? s : "Use Case Points";
                entry.state = UCPState.fromMap(ucpMap);
                m.ucpPanes.add(entry);
            }
        }

        return m;
    }

    public static class FPPaneEntry {
        public String tabName;
        public FPState state;

        public Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("tabName", tabName);
            m.put("state", state != null ? state.toMap() : null);
            return m;
        }

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

    public static class UCPPaneEntry {
        public String tabName;
        public UCPState state;

        public Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("tabName", tabName);
            m.put("state", state != null ? state.toMap() : null);
            return m;
        }

        public static UCPPaneEntry fromMap(Map<String, Object> map) {
            UCPPaneEntry e = new UCPPaneEntry();
            e.tabName = (map.get("tabName") instanceof String s) ? s : "Use Case Points";

            Object st = map.get("state");
            if (st instanceof Map<?, ?> raw) {
                Map<String, Object> sMap = new LinkedHashMap<>();
                for (Map.Entry<?, ?> en : raw.entrySet()) {
                    sMap.put(String.valueOf(en.getKey()), en.getValue());
                }
                e.state = UCPState.fromMap(sMap);
            } else {
                e.state = new UCPState();
            }
            return e;
        }
    }

    public static class FPState {
        public String language;
        public int[] counts = new int[5];
        public int[] complexities = new int[5];
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
    }

    public static class UCPState {
        public int[] actorCounts = new int[3];
        public int[] useCaseCounts = new int[3];
        public int[] technicalRatings = new int[13];
        public int[] environmentalRatings = new int[8];

        public String productivityFactor;
        public String locPerPm;
        public String locPerUcp;

        public String uaw;
        public String uucw;
        public String totalCount;
        public String tcf;
        public String ecf;
        public String totalUcp;
        public String estimatedHours;
        public String estimatedLoc;
        public String estimatedPm;

        public Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("actorCounts", toList(actorCounts));
            m.put("useCaseCounts", toList(useCaseCounts));
            m.put("technicalRatings", toList(technicalRatings));
            m.put("environmentalRatings", toList(environmentalRatings));
            m.put("productivityFactor", productivityFactor);
            m.put("locPerPm", locPerPm);
            m.put("locPerUcp", locPerUcp);
            m.put("uaw", uaw);
            m.put("uucw", uucw);
            m.put("totalCount", totalCount);
            m.put("tcf", tcf);
            m.put("ecf", ecf);
            m.put("totalUcp", totalUcp);
            m.put("estimatedHours", estimatedHours);
            m.put("estimatedLoc", estimatedLoc);
            m.put("estimatedPm", estimatedPm);
            return m;
        }

        public static UCPState fromMap(Map<String, Object> map) {
            UCPState s = new UCPState();
            s.actorCounts = toIntArray(map.get("actorCounts"), 3);
            s.useCaseCounts = toIntArray(map.get("useCaseCounts"), 3);
            s.technicalRatings = toIntArray(map.get("technicalRatings"), 13);
            s.environmentalRatings = toIntArray(map.get("environmentalRatings"), 8);
            s.productivityFactor = (map.get("productivityFactor") instanceof String str) ? str : "20";
            s.locPerPm = (map.get("locPerPm") instanceof String str) ? str : "700";
            s.locPerUcp = (map.get("locPerUcp") instanceof String str) ? str : "120";
            s.uaw = (map.get("uaw") instanceof String str) ? str : "";
            s.uucw = (map.get("uucw") instanceof String str) ? str : "";
            s.totalCount = (map.get("totalCount") instanceof String str) ? str : "";
            if (s.totalCount.isBlank() && map.get("uucp") instanceof String legacyCount) s.totalCount = legacyCount;
            s.tcf = (map.get("tcf") instanceof String str) ? str : "0.60";
            s.ecf = (map.get("ecf") instanceof String str) ? str : "1.40";
            s.totalUcp = (map.get("totalUcp") instanceof String str) ? str : "";
            s.estimatedHours = (map.get("estimatedHours") instanceof String str) ? str : "";
            s.estimatedLoc = (map.get("estimatedLoc") instanceof String str) ? str : "";
            s.estimatedPm = (map.get("estimatedPm") instanceof String str) ? str : "";
            return s;
        }
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
