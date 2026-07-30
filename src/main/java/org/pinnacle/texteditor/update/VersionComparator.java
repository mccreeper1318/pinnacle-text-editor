package org.pinnacle.texteditor.update;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class VersionComparator {
    private VersionComparator() {
    }

    static boolean isNewer(String candidate, String current) {
        return compare(candidate, current) > 0;
    }

    static int compare(String leftVersion, String rightVersion) {
        ParsedVersion left = parse(leftVersion);
        ParsedVersion right = parse(rightVersion);

        int count = Math.max(left.numbers.size(), right.numbers.size());
        for (int index = 0; index < count; index++) {
            BigInteger leftPart = index < left.numbers.size() ? left.numbers.get(index) : BigInteger.ZERO;
            BigInteger rightPart = index < right.numbers.size() ? right.numbers.get(index) : BigInteger.ZERO;
            int comparison = leftPart.compareTo(rightPart);
            if (comparison != 0) {
                return comparison;
            }
        }

        if (left.prerelease.isEmpty() && !right.prerelease.isEmpty()) {
            return 1;
        }
        if (!left.prerelease.isEmpty() && right.prerelease.isEmpty()) {
            return -1;
        }

        int prereleaseCount = Math.max(left.prerelease.size(), right.prerelease.size());
        for (int index = 0; index < prereleaseCount; index++) {
            if (index >= left.prerelease.size()) {
                return -1;
            }
            if (index >= right.prerelease.size()) {
                return 1;
            }

            String leftPart = left.prerelease.get(index);
            String rightPart = right.prerelease.get(index);
            boolean leftNumeric = leftPart.matches("\\d+");
            boolean rightNumeric = rightPart.matches("\\d+");

            int comparison;
            if (leftNumeric && rightNumeric) {
                comparison = new BigInteger(leftPart).compareTo(new BigInteger(rightPart));
            } else if (leftNumeric) {
                comparison = -1;
            } else if (rightNumeric) {
                comparison = 1;
            } else {
                comparison = leftPart.compareToIgnoreCase(rightPart);
            }
            if (comparison != 0) {
                return comparison;
            }
        }
        return 0;
    }

    static boolean isPrerelease(String version) {
        return !parse(version).prerelease.isEmpty();
    }

    static String normalize(String version) {
        String clean = version == null ? "0" : version.trim();
        if (clean.regionMatches(true, 0, "v.", 0, 2)) {
            clean = clean.substring(2);
        } else if (!clean.isEmpty() && (clean.charAt(0) == 'v' || clean.charAt(0) == 'V')) {
            clean = clean.substring(1);
        }
        while (clean.startsWith(".")) {
            clean = clean.substring(1);
        }
        return clean;
    }

    private static ParsedVersion parse(String version) {
        String clean = normalize(version);
        int metadata = clean.indexOf('+');
        if (metadata >= 0) {
            clean = clean.substring(0, metadata);
        }

        String[] pieces = clean.split("-", 2);
        String[] numericPieces = pieces[0].split("\\.");
        List<BigInteger> numbers = new ArrayList<>();
        for (String piece : numericPieces) {
            String digits = piece.replaceFirst("[^0-9].*$", "");
            numbers.add(digits.isEmpty() ? BigInteger.ZERO : new BigInteger(digits));
        }
        if (numbers.isEmpty()) {
            numbers.add(BigInteger.ZERO);
        }

        List<String> prerelease = new ArrayList<>();
        if (pieces.length > 1 && !pieces[1].isBlank()) {
            for (String identifier : pieces[1].toLowerCase(Locale.ROOT).split("[.-]")) {
                if (!identifier.isBlank()) {
                    prerelease.add(identifier);
                }
            }
        }
        return new ParsedVersion(numbers, prerelease);
    }

    private record ParsedVersion(List<BigInteger> numbers, List<String> prerelease) {
    }
}
