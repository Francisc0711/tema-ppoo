package ebanking.storage;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Formats and reads CSV records: quoted fields, doubled quotes, embedded line breaks. */
final class CsvCodec {

    private CsvCodec() {
    }

    /** Joins the fields into one record, quoting where needed. No trailing newline. */
    static String format(List<String> fields) {
        StringBuilder record = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) {
                record.append(',');
            }
            String field = fields.get(i);
            if (field.contains(",") || field.contains("\"") || field.contains("\r") || field.contains("\n")) {
                record.append('"').append(field.replace("\"", "\"\"")).append('"');
            } else {
                record.append(field);
            }
        }
        return record.toString();
    }

    /** Reads one record; returns null at end of file. lines[0] grows by the physical lines read. */
    static List<String> readRecord(BufferedReader reader, int[] lines) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return null;
        }
        lines[0]++;
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; ; i++) {
            if (i == line.length()) {
                if (!quoted) {
                    break;
                }
                String next = reader.readLine();
                if (next == null) {
                    break;   // unterminated quote: the field count check fails later
                }
                lines[0]++;
                field.append('\n');
                line = next;
                i = -1;
                continue;
            }
            char c = line.charAt(i);
            if (quoted && c == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                field.append('"');
                i++;
            } else if (c == '"') {
                quoted = !quoted;
            } else if (c == ',' && !quoted) {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }
        fields.add(field.toString());
        return fields;
    }
}
