package org.pinnacle.texteditor.update;

import org.pinnacle.texteditor.AppInfo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class UpdateService {
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(20);

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public CompletableFuture<Optional<UpdateInfo>> checkForUpdate() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder(AppInfo.latestReleaseApi())
                        .timeout(REQUEST_TIMEOUT)
                        .header("Accept", "application/vnd.github+json")
                        .header("X-GitHub-Api-Version", "2022-11-28")
                        .header("User-Agent", "Pinnacle-Text-Editor/" + AppInfo.VERSION)
                        .GET()
                        .build();
                HttpResponse<String> response = client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
                );
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new IOException("GitHub returned HTTP " + response.statusCode());
                }
                return parseRelease(response.body());
            } catch (IOException exception) {
                throw new UpdateException("Unable to check for updates", exception);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new UpdateException("Update check interrupted", exception);
            }
        });
    }

    public CompletableFuture<Path> download(UpdateInfo update) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path updateDirectory = Path.of(
                        System.getProperty("user.home", "."),
                        ".cache",
                        "pinnacle-text-editor",
                        "updates"
                );
                Files.createDirectories(updateDirectory);
                Path destination = updateDirectory.resolve(update.assetName());
                Files.deleteIfExists(destination);

                HttpRequest request = HttpRequest.newBuilder(update.downloadUri())
                        .timeout(Duration.ofMinutes(5))
                        .header("Accept", "application/octet-stream")
                        .header("User-Agent", "Pinnacle-Text-Editor/" + AppInfo.VERSION)
                        .GET()
                        .build();
                HttpResponse<Path> response = client.send(
                        request,
                        HttpResponse.BodyHandlers.ofFile(destination)
                );
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    Files.deleteIfExists(destination);
                    throw new IOException("Download returned HTTP " + response.statusCode());
                }

                String actual = sha256(destination);
                if (!actual.equalsIgnoreCase(update.sha256())) {
                    Files.deleteIfExists(destination);
                    throw new IOException("Downloaded update failed SHA-256 verification");
                }
                return destination;
            } catch (IOException exception) {
                throw new UpdateException("Unable to download the update", exception);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new UpdateException("Update download interrupted", exception);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private Optional<UpdateInfo> parseRelease(String json) throws IOException {
        Object rootValue;
        try {
            rootValue = SimpleJson.parse(json);
        } catch (RuntimeException exception) {
            throw new IOException("Invalid release response", exception);
        }
        if (!(rootValue instanceof Map<?, ?> root)) {
            throw new IOException("Release response was not an object");
        }

        String tag = stringValue(root.get("tag_name"));
        if (tag == null || !VersionComparator.isNewer(tag, AppInfo.VERSION)) {
            return Optional.empty();
        }
        Object assetsValue = root.get("assets");
        if (!(assetsValue instanceof List<?> assets)) {
            throw new IOException("Release contains no assets");
        }

        for (Object assetValue : assets) {
            if (!(assetValue instanceof Map<?, ?> asset)) {
                continue;
            }
            String name = stringValue(asset.get("name"));
            String url = stringValue(asset.get("browser_download_url"));
            String digest = stringValue(asset.get("digest"));
            if (!isLinuxDeb(name) || url == null || digest == null) {
                continue;
            }
            String sha256 = digest.toLowerCase(Locale.ROOT).startsWith("sha256:")
                    ? digest.substring("sha256:".length())
                    : null;
            if (sha256 == null || !sha256.matches("[0-9a-fA-F]{64}")) {
                continue;
            }
            return Optional.of(new UpdateInfo(
                    normalizeVersion(tag),
                    name,
                    URI.create(url),
                    sha256
            ));
        }
        throw new IOException("The latest release has no verified Linux .deb asset");
    }

    private boolean isLinuxDeb(String name) {
        if (name == null) {
            return false;
        }
        String lower = name.toLowerCase(Locale.ROOT);
        String architecture = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        boolean supportedArchitecture = architecture.equals("amd64") || architecture.equals("x86_64");
        return supportedArchitecture
                && lower.endsWith(".deb")
                && lower.contains("pinnacle")
                && lower.contains("text");
    }

    private String normalizeVersion(String value) {
        String trimmed = value.trim();
        return trimmed.startsWith("v") || trimmed.startsWith("V")
                ? trimmed.substring(1)
                : trimmed;
    }

    private String stringValue(Object value) {
        return value instanceof String string ? string : null;
    }

    private String sha256(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (var input = Files.newInputStream(file)) {
                byte[] buffer = new byte[64 * 1024];
                int read;
                while ((read = input.read(buffer)) >= 0) {
                    if (read > 0) {
                        digest.update(buffer, 0, read);
                    }
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public static final class UpdateException extends RuntimeException {
        public UpdateException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
