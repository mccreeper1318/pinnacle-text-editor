package org.pinnacle.texteditor;

import java.net.URI;

public final class AppInfo {
    public static final String NAME = "Pinnacle Text Editor";
    public static final String VERSION = resolveVersion();
    private static final String DEFAULT_UPDATE_REPOSITORY = "mccreeper1318/pinnacle-text-editor";

    private AppInfo() {
    }

    public static URI latestReleaseApi() {
        String repository = System.getProperty(
                "pinnacle.update.repository",
                DEFAULT_UPDATE_REPOSITORY
        ).trim();
        return URI.create("https://api.github.com/repos/" + repository + "/releases/latest");
    }

    private static String resolveVersion() {
        String packagedVersion = System.getProperty("jpackage.app-version");
        if (packagedVersion != null && !packagedVersion.isBlank()) {
            return packagedVersion.trim();
        }
        Package applicationPackage = AppInfo.class.getPackage();
        String manifestVersion = applicationPackage == null
                ? null
                : applicationPackage.getImplementationVersion();
        return manifestVersion == null || manifestVersion.isBlank()
                ? "0.2.2-dev"
                : manifestVersion.trim();
    }
}
