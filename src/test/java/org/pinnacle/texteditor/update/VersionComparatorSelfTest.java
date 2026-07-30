package org.pinnacle.texteditor.update;

public final class VersionComparatorSelfTest {
    private VersionComparatorSelfTest() {
    }

    public static void run() {
        require("0.2.2".equals(VersionComparator.normalize("v.0.2.2")), "v. prefix normalization failed");
        require("0.2.2".equals(VersionComparator.normalize("v0.2.2")), "v prefix normalization failed");
        require(VersionComparator.compare("0.3", "0.2.2") > 0, "new stable version comparison failed");
        require(VersionComparator.compare("0.3-beta.10", "0.3-beta.9") > 0,
                "numeric prerelease comparison failed");
        require(VersionComparator.compare("0.3", "0.3-beta.10") > 0,
                "stable release must sort after its prerelease");
        require(VersionComparator.compare("v.0.2.2", "0.2.2") == 0,
                "equivalent tag formats must compare equally");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
