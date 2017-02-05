package com.apache.service;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Created by Mishin737 on 18.01.2017.
 * Reading Manifest file
 */
public class ManifestReader {
    // These hold version information.
    private String version;
    private String timestamp;

    private final Logger logger;
    private final Class<?> clazz;

    /**
     * A class constructor here, passing all the parameters
     *
     * @param logger current logger
     * @param clazz  name of current class
     */
    public ManifestReader(Logger logger, Class<?> clazz) {
        this.logger = logger;
        this.clazz = clazz;
        readVersionParameters();
    }

    /**
     * Read pom variables from the manifest file
     */
    private void readVersionParameters() {
        if (StringUtils.isBlank(this.version)) {
            String className = clazz.getSimpleName() + ".class";
            String classPath = clazz.getResource(className).toString();
            String manifestPath;
            String partPathToManifest = "/META-INF/MANIFEST.MF";
            if (!classPath.startsWith("jar")) {
                // Class not from JAR
                String relativePath = clazz.getName().replace('.', File.separatorChar) + ".class";
                String classFolder = classPath.substring(0, classPath.length() - relativePath.length() - 1);
                manifestPath = classFolder + partPathToManifest;
            } else {
                manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + partPathToManifest;
            }
            logger.debug(String.format("manifestPath={%s}", manifestPath));
            this.version = readVariableFromManifest(manifestPath, "Implementation-Version");
            this.timestamp = readVariableFromManifest(manifestPath, "Build-Time");
        }
    }

    private String readVariableFromManifest(String manifestPath, String pomVariable) {
        Manifest manifest;
        try {
            manifest = new Manifest(new URL(manifestPath).openStream());
            Attributes attrs = manifest.getMainAttributes();

            String manifestVariable = attrs.getValue(pomVariable);
            logger.debug(String.format("Read {%s}: {%s}", pomVariable, manifestVariable));

            return manifestVariable;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return StringUtils.EMPTY;
    }

    public String getVersion() {
        return version;
    }

    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Writes a standard service startup message to the log.
     */
    private static void writeStartupMessage(Logger log) {
        ManifestReader manifest = new ManifestReader(log, ManifestReader.class);
        final StringBuffer buffer = new StringBuffer();
        buffer.append("\t\t Version: ");
        buffer.append(" " + manifest.getVersion());
        buffer.append("\n");
        buffer.append("\t\t Date built: ");
        buffer.append(" " + manifest.getTimestamp());
        buffer.append("\n");
        log.info(buffer.toString());
    }


    public static void main(String[] args) {
        Logger log = Logger.getLogger(ManifestReader.class.getClass().getName());
        writeStartupMessage(log);
    }
}
