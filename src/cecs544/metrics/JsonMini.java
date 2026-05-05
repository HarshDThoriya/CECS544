package cecs544.metrics;

import java.util.*;

/**
 * Minimal JSON parser/stringifier for:
 * - objects (Map<String,Object>)
 * - arrays (List<Object>)
 * - strings, numbers, booleans, null
 *
 * Good enough for the .ms project file in Iteration 1.
 */
public class JsonMini {

    public static String stringify(Object obj) {
        StringBuilder sb = new StringBuilder();
        writeValue(sb, obj);
        return sb.toString();
    }

    private static void writeValue(StringBuilder sb, Object v) {
        if (v == null) {
            sb.append("null");
        } else if (v instanceof String s) {
            sb.append('"').append(escape(s)).append('"');
        } else if (v instanceof Number || v instanceof Boolean) {
            sb.append(v.toString());
        } else if (v instanceof Map<?, ?> map) {
            sb.append("{");
            boolean first = true;
            for (var entry : map.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append('"').append(escape(String.valueOf(entry.getKey()))).append("\":");
                writeValue(sb, entry.getValue());
            }
            sb.append("}");
        } else if (v instanceof List<?> list) {
            sb.append("[");
            boolean first = true;
            for (Object item : list) {
                if (!first) sb.append(",");
                first = false;
                writeValue(sb, item);
            }
            sb.append("]");
        } else {
            // fallback
            sb.append('"').append(escape(v.toString())).append('"');
        }
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static Object parse(String json) {
        return new Parser(json).parseValue();
    }

    private static class Parser {
        private final String s;
        private int i = 0;

        Parser(String s) { this.s = s; }

        Object parseValue() {
            skipWs();
            if (i >= s.length()) throw new IllegalArgumentException("Unexpected end of JSON");

            char c = s.charAt(i);
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            if (c == 't' || c == 'f') return parseBoolean();
            if (c == 'n') return parseNull();
            if (c == '-' || Character.isDigit(c)) return parseNumber();

            throw new IllegalArgumentException("Unexpected char: " + c);
        }

        Map<String, Object> parseObject() {
            expect('{');
            skipWs();
            Map<String, Object> map = new LinkedHashMap<>();
            if (peek('}')) { i++; return map; }

            while (true) {
                skipWs();
                String key = parseString();
                skipWs();
                expect(':');
                Object val = parseValue();
                map.put(key, val);
                skipWs();
                if (peek('}')) { i++; break; }
                expect(',');
            }
            return map;
        }

        List<Object> parseArray() {
            expect('[');
            skipWs();
            List<Object> list = new ArrayList<>();
            if (peek(']')) { i++; return list; }

            while (true) {
                Object v = parseValue();
                list.add(v);
                skipWs();
                if (peek(']')) { i++; break; }
                expect(',');
            }
            return list;
        }

        String parseString() {
            expect('"');
            StringBuilder out = new StringBuilder();
            while (i < s.length()) {
                char c = s.charAt(i++);
                if (c == '"') break;
                if (c == '\\') {
                    if (i >= s.length()) throw new IllegalArgumentException("Bad escape");
                    char e = s.charAt(i++);
                    switch (e) {
                        case '"': out.append('"'); break;
                        case '\\': out.append('\\'); break;
                        case 'n': out.append('\n'); break;
                        case 'r': out.append('\r'); break;
                        case 't': out.append('\t'); break;
                        default: out.append(e); break;
                    }
                } else {
                    out.append(c);
                }
            }
            return out.toString();
        }

        Boolean parseBoolean() {
            if (s.startsWith("true", i)) { i += 4; return true; }
            if (s.startsWith("false", i)) { i += 5; return false; }
            throw new IllegalArgumentException("Bad boolean");
        }

        Object parseNull() {
            if (s.startsWith("null", i)) { i += 4; return null; }
            throw new IllegalArgumentException("Bad null");
        }

        Number parseNumber() {
            int start = i;
            if (s.charAt(i) == '-') i++;
            while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
            if (i < s.length() && s.charAt(i) == '.') {
                i++;
                while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
            }
            String num = s.substring(start, i);
            if (num.contains(".")) return Double.parseDouble(num);
            return Long.parseLong(num);
        }

        void skipWs() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        }

        boolean peek(char c) {
            return i < s.length() && s.charAt(i) == c;
        }

        void expect(char c) {
            skipWs();
            if (i >= s.length() || s.charAt(i) != c) {
                throw new IllegalArgumentException("Expected '" + c + "'");
            }
            i++;
        }
    }
}
