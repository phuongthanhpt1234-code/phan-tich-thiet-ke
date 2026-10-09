package com.bookstore.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Đọc/ghi CSV (UTF-8 có BOM để Excel mở không lỗi tiếng Việt). */
public final class CsvUtil {
    private CsvUtil() {}

    public static List<String[]> read(File f) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line; boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { line = line.replace("\uFEFF", ""); first = false; }
                if (!line.isBlank()) rows.add(parseLine(line));
            }
        }
        return rows;
    }

    public static void write(File f, List<String[]> rows) throws IOException {
        try (Writer w = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)) {
            w.write('\uFEFF');
            for (String[] r : rows) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < r.length; i++) {
                    if (i > 0) sb.append(',');
                    sb.append(escape(r[i]));
                }
                w.write(sb.append("\r\n").toString());
            }
        }
    }

    static String escape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }

    static String[] parseLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (quoted) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') { cur.append('"'); i++; }
                    else quoted = false;
                } else cur.append(ch);
            } else if (ch == '"') quoted = true;
            else if (ch == ',') { out.add(cur.toString().trim()); cur.setLength(0); }
            else cur.append(ch);
        }
        out.add(cur.toString().trim());
        return out.toArray(new String[0]);
    }
}
