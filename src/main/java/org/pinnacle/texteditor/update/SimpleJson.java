package org.pinnacle.texteditor.update;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class SimpleJson {
    private final String input;
    private int index;

    private SimpleJson(String input) {
        this.input = input;
    }

    static Object parse(String input) {
        SimpleJson parser = new SimpleJson(input);
        Object value = parser.readValue();
        parser.skipWhitespace();
        if (parser.index != input.length()) {
            throw new IllegalArgumentException("Unexpected JSON content at " + parser.index);
        }
        return value;
    }

    private Object readValue() {
        skipWhitespace();
        if (index >= input.length()) {
            throw new IllegalArgumentException("Unexpected end of JSON");
        }
        return switch (input.charAt(index)) {
            case '{' -> readObject();
            case '[' -> readArray();
            case '"' -> readString();
            case 't' -> readLiteral("true", Boolean.TRUE);
            case 'f' -> readLiteral("false", Boolean.FALSE);
            case 'n' -> readLiteral("null", null);
            default -> readNumber();
        };
    }

    private Map<String, Object> readObject() {
        expect('{');
        Map<String, Object> result = new LinkedHashMap<>();
        skipWhitespace();
        if (peek('}')) {
            index++;
            return result;
        }
        while (true) {
            skipWhitespace();
            String key = readString();
            skipWhitespace();
            expect(':');
            result.put(key, readValue());
            skipWhitespace();
            if (peek('}')) {
                index++;
                return result;
            }
            expect(',');
        }
    }

    private List<Object> readArray() {
        expect('[');
        List<Object> result = new ArrayList<>();
        skipWhitespace();
        if (peek(']')) {
            index++;
            return result;
        }
        while (true) {
            result.add(readValue());
            skipWhitespace();
            if (peek(']')) {
                index++;
                return result;
            }
            expect(',');
        }
    }

    private String readString() {
        expect('"');
        StringBuilder result = new StringBuilder();
        while (index < input.length()) {
            char current = input.charAt(index++);
            if (current == '"') {
                return result.toString();
            }
            if (current != '\\') {
                result.append(current);
                continue;
            }
            if (index >= input.length()) {
                throw new IllegalArgumentException("Bad JSON escape");
            }
            char escaped = input.charAt(index++);
            switch (escaped) {
                case '"', '\\', '/' -> result.append(escaped);
                case 'b' -> result.append('\b');
                case 'f' -> result.append('\f');
                case 'n' -> result.append('\n');
                case 'r' -> result.append('\r');
                case 't' -> result.append('\t');
                case 'u' -> result.append(readUnicode());
                default -> throw new IllegalArgumentException("Bad JSON escape: " + escaped);
            }
        }
        throw new IllegalArgumentException("Unterminated JSON string");
    }

    private char readUnicode() {
        if (index + 4 > input.length()) {
            throw new IllegalArgumentException("Bad unicode escape");
        }
        int value = Integer.parseInt(input.substring(index, index + 4), 16);
        index += 4;
        return (char) value;
    }

    private Object readNumber() {
        int start = index;
        if (peek('-')) {
            index++;
        }
        while (index < input.length() && Character.isDigit(input.charAt(index))) {
            index++;
        }
        if (peek('.')) {
            index++;
            while (index < input.length() && Character.isDigit(input.charAt(index))) {
                index++;
            }
        }
        if (peek('e') || peek('E')) {
            index++;
            if (peek('+') || peek('-')) {
                index++;
            }
            while (index < input.length() && Character.isDigit(input.charAt(index))) {
                index++;
            }
        }
        String number = input.substring(start, index);
        if (number.isBlank()) {
            throw new IllegalArgumentException("Expected JSON value at " + start);
        }
        try {
            if (number.contains(".") || number.contains("e") || number.contains("E")) {
                return Double.parseDouble(number);
            }
            return Long.parseLong(number);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid JSON number: " + number, exception);
        }
    }

    private Object readLiteral(String literal, Object value) {
        if (!input.startsWith(literal, index)) {
            throw new IllegalArgumentException("Expected " + literal + " at " + index);
        }
        index += literal.length();
        return value;
    }

    private void expect(char expected) {
        skipWhitespace();
        if (index >= input.length() || input.charAt(index) != expected) {
            throw new IllegalArgumentException("Expected '" + expected + "' at " + index);
        }
        index++;
    }

    private boolean peek(char value) {
        return index < input.length() && input.charAt(index) == value;
    }

    private void skipWhitespace() {
        while (index < input.length() && Character.isWhitespace(input.charAt(index))) {
            index++;
        }
    }
}
