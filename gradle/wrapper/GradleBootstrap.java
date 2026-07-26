import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class GradleBootstrap {
    private static final String GRADLE_VERSION = "9.6.1";
    private static final String DISTRIBUTION_URL =
            "https://services.gradle.org/distributions/gradle-" + GRADLE_VERSION + "-bin.zip";
    private static final String DISTRIBUTION_SHA256 =
            "9c0f7faeeb306cb14e4279a3e084ca6b596894089a0638e68a07c945a32c9e14";

    private GradleBootstrap() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Project directory was not provided.");
            System.exit(1);
        }

        Path projectDirectory = Path.of(args[0]).toAbsolutePath().normalize();
        String[] gradleArguments = new String[Math.max(0, args.length - 1)];
        if (gradleArguments.length > 0) {
            System.arraycopy(args, 1, gradleArguments, 0, gradleArguments.length);
        }

        Path gradleHome = installGradleIfNeeded();
        Path executable = gradleHome.resolve(isWindows() ? "bin/gradle.bat" : "bin/gradle");

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command().add(executable.toString());
        processBuilder.command().addAll(java.util.List.of(gradleArguments));
        processBuilder.directory(projectDirectory.toFile());
        processBuilder.inheritIO();

        int exitCode = processBuilder.start().waitFor();
        System.exit(exitCode);
    }

    private static Path installGradleIfNeeded() throws Exception {
        Path cacheRoot = Path.of(System.getProperty("user.home"), ".gradle", "pinnacle-bootstrap");
        Path gradleHome = cacheRoot.resolve("gradle-" + GRADLE_VERSION);
        Path executable = gradleHome.resolve(isWindows() ? "bin/gradle.bat" : "bin/gradle");
        if (Files.isRegularFile(executable)) {
            return gradleHome;
        }

        Files.createDirectories(cacheRoot);
        Path archive = cacheRoot.resolve("gradle-" + GRADLE_VERSION + "-bin.zip");
        if (!Files.isRegularFile(archive) || !sha256(archive).equalsIgnoreCase(DISTRIBUTION_SHA256)) {
            Files.deleteIfExists(archive);
            download(archive);
        }

        if (!sha256(archive).equalsIgnoreCase(DISTRIBUTION_SHA256)) {
            throw new IOException("The downloaded Gradle archive failed checksum verification.");
        }

        Path temporaryDirectory = cacheRoot.resolve("gradle-" + GRADLE_VERSION + ".extracting");
        deleteRecursively(temporaryDirectory);
        Files.createDirectories(temporaryDirectory);
        unzip(archive, temporaryDirectory);

        Path extractedHome = temporaryDirectory.resolve("gradle-" + GRADLE_VERSION);
        if (!Files.isDirectory(extractedHome)) {
            throw new IOException("The Gradle archive did not contain the expected directory.");
        }

        deleteRecursively(gradleHome);
        Files.move(extractedHome, gradleHome, StandardCopyOption.REPLACE_EXISTING);
        deleteRecursively(temporaryDirectory);

        if (!isWindows()) {
            gradleHome.resolve("bin/gradle").toFile().setExecutable(true, false);
        }
        return gradleHome;
    }

    private static void download(Path destination) throws IOException, InterruptedException {
        System.out.println("Downloading Gradle " + GRADLE_VERSION + "...");
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(DISTRIBUTION_URL)).GET().build();
        Path temporaryFile = destination.resolveSibling(destination.getFileName() + ".part");
        Files.deleteIfExists(temporaryFile);

        HttpResponse<Path> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofFile(temporaryFile)
        );
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            Files.deleteIfExists(temporaryFile);
            throw new IOException("Gradle download failed with HTTP " + response.statusCode() + ".");
        }
        Files.move(temporaryFile, destination, StandardCopyOption.REPLACE_EXISTING);
    }

    private static void unzip(Path archive, Path destination) throws IOException {
        try (InputStream input = Files.newInputStream(archive);
             ZipInputStream zip = new ZipInputStream(input)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                Path output = destination.resolve(entry.getName()).normalize();
                if (!output.startsWith(destination)) {
                    throw new IOException("Unsafe path in Gradle archive: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(output);
                } else {
                    Files.createDirectories(output.getParent());
                    try (OutputStream fileOutput = Files.newOutputStream(output)) {
                        zip.transferTo(fileOutput);
                    }
                }
                zip.closeEntry();
            }
        }
    }

    private static String sha256(Path file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) >= 0) {
                digest.update(buffer, 0, read);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        try (var stream = Files.walk(path)) {
            for (Path item : stream.sorted(java.util.Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(item);
            }
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
