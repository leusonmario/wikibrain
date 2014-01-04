package org.wikapidia.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Shilad Sen
 */
public class ResourceInstaller {
    public static String DEFAULT_RESOURCES[] = new String[] {
            "/wp-run.sh",
            "/wp-conf.sh",
    };
    public static String DEFAULT_DIR = ".";

    public static void usage() {
        System.err.println("Usage: java ResourceInstaller [output_dir resource1_name resource2_name...]");
        System.exit(1);
    }
    public static void main(String args[]) throws IOException {
        String outputDir = DEFAULT_DIR;
        String resources[] = DEFAULT_RESOURCES;

        if (args.length == 0) {
            // use default settings
        } else if (args.length == 1) {
            usage();
            return;
        } else {
            resources = ArrayUtils.subarray(args, 1, args.length);
        }

        if (!new File(outputDir).isDirectory()) {
            System.err.println("Output directory '" + outputDir + "' does not exist");
            usage();
            return;
        }

        for (String resource : resources) {
            File dest = FileUtils.getFile(outputDir, resource);
            InputStream is = ResourceInstaller.class.getResourceAsStream(resource);
            if (is == null) {
                System.err.println("Couldn't find resource " + resource);
                usage();
                return;
            }
            if (dest.isFile()) {
                for (int i = 1; i < 10000; i++) {
                    File backup = new File(dest + ".backup." + i);
                    if (!backup.exists()) {
                        FileUtils.moveFile(dest, backup);
                        System.err.println("backing up " + dest + " to " + backup);
                        break;
                    }
                }
            }
            FileUtils.copyInputStreamToFile(is, dest);
            System.err.println("creating file " + dest + " from resources");
            is.close();
        }
    }
}
