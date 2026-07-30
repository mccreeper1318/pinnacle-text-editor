package org.pinnacle.texteditor;

import java.net.URI;

public final class AppInfo {
    public static final String NAME = "Pinnacle Text Editor";
    public static final String VERSION = resolveVersion();
    private static final String DEFAULT_UPDATE_REPOSITORY = "mccreeper1318/pinnacle-text-editor";

    private AppInfo() {
    }

    public static URI latestReleaseApi() {
        return URI.create("https://api.github.com/repos/" + updateRepository() + "/releases/latest");
    }

    public static URI releasesApi() {
        return URI.create("https://api.github.com/repos/" + updateRepository() + "/releases?per_page=30");
    }

    private static String updateRepository() {
        return System.getProperty(
                "pinnacle.update.repository",
                DEFAULT_UPDATE_REPOSITORY
        ).trim();
    }

    private static String resolveVersion() {
        String configuredVersion = System.getProperty("pinnacle.app.version");
        if (configuredVersion != null && !configuredVersion.isBlank()) {
            return configuredVersion.trim();
        }

        String packagedVersion = System.getProperty("jpackage.app-version");
        if (packagedVersion != null && !packagedVersion.isBlank()) {
            return packagedVersion.trim();
        }

        Package applicationPackage = AppInfo.class.getPackage();
        String manifestVersion = applicationPackage == null
                ? null
                : applicationPackage.getImplementationVersion();
        return manifestVersion == null || manifestVersion.isBlank()
                ? "0.3-beta.2"
                : manifestVersion.trim();
    }
}
