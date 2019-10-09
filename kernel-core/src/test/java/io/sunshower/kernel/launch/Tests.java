package io.sunshower.kernel.launch;

import lombok.val;

import java.io.File;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

public class Tests {

    public static File buildDirectory() {
        for (var file = current(); file != null; file = file.getParentFile()) {
            val check = new File(file, "build");
            if (check.exists() && check.isDirectory()) {
                return check.toPath().normalize().toFile();
            }
        }
        throw new NoSuchElementException("No build directory found for : " + new File(".").getAbsolutePath());
    }

    public static File rootDirectory() {
        File result = null;
        for (var file = current(); file != null; file = file.getParentFile()) {
            val check = new File(file, "build.gradle");
            if (check.exists() && check.isFile()) {
                result = check.getParentFile();
            } else {
                break;
            }
        }
        if (result != null) {
            return result;
        }
        throw new NoSuchElementException("Failed to resolve root directory");
    }


    private static File current() {
        return new File(".").getAbsoluteFile();
    }

    public static File projectOutput(String project, String type) {
        val projectBuild = project(project).toPath().resolve("build").toFile();
        if(!projectBuild.exists()) {
            throw new IllegalArgumentException("Error:  Project " + project + " either didn't exist or hasn't been build (have you added it is a dependency?)");
        }
        if(!projectBuild.isDirectory()) {
            throw new IllegalArgumentException("Error:  Project " + project + " isn't a directory--can't search");
        }
        val file = new File(projectBuild, "libs");

        val results = file.listFiles(t -> t.getPath().endsWith(type));
        if(results != null && results.length > 0) {
            return results[0];
        }
        throw new NoSuchElementException("Error--can't find project output");

    }

    public static File createTemp(String directory) {
        val tmp = new File(buildDirectory(), "temp");
        val result = new File(tmp, directory);
        result.mkdirs();
        result.deleteOnExit();
        return result;
    }

    public static File project(String project) {
        val  root     = rootDirectory();
        val  segments = project.split(":");
        File result   = root;
        for (val segment : segments) {
            result = new File(result, segment);
            if (!(result.exists() && result.isDirectory())) {
                throw new NoSuchElementException("Can't find project with path: "
                                                         + project + " in " + root.getAbsolutePath());
            }

        }
        return result;
    }
}
