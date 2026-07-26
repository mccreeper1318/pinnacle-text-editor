package org.pinnacle.texteditor.update;

import java.util.ArrayList;
import java.util.List;

final class VersionComparator {
    private VersionComparator() {
    }

    static boolean isNewer(String candidate, String current) {
        ParsedVersion left = parse(candidate);
        ParsedVersion right = parse(current);
        int count = Math.max(left.numbers.size(), right.numbers.size());
        for (int index = 0; index < count; index++) {
            int leftPart = index < left.numbers.size() ? left.numbers.get(index) : 0;
            int rightPart = index < right.numbers.size() ? right.numbers.get(index) : 0;
            if (leftPart != rightPart) {
                return leftPart > rightPart;
            }
        }
        if (left.prerelease == null && right.prerelease != null) {
            return true;
        }
        if (left.prerelease != null && right.prerelease == null) {
            return false;
        }
        if (left.prerelease == null) {
            return false;
        }
        return left.prerelease.compareToIgnoreCase(right.prerelease) > 0;
    }

    private static ParsedVersion parse(String version) {
        String clean = version == null ? "0" : version.trim();
        if (clean.startsWith("v") || clean.startsWith("V")) {
            clean = clean.substring(1);
        }
        String[] pieces = clean.split("-", 2);
        String[] numericPieces = pieces[0].split("\\.");
        List<Integer> numbers = new ArrayList<>();
        for (String piece : numericPieces) {
            String digits = piece.replaceAll("[^0-9].*$", "");
            numbers.add(digits.isEmpty() ? 0 : Integer.parseInt(digits));
        }
        String prerelease = pieces.length > 1 ? pieces[1] : null;
        return new ParsedVersion(numbers, prerelease);
    }

    private record ParsedVersion(List<Integer> numbers, String prerelease) {
    }
}
