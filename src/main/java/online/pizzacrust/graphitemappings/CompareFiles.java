package online.pizzacrust.graphitemappings;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class CompareFiles {

    public static void main(String... args) throws Exception {
        List<String> lines = Files.readAllLines(new File(args[0]).toPath());
        List<String> lines2 = Files.readAllLines(new File(args[1]).toPath());
        if (lines.size() == lines2.size()) {
            System.out.println("Same size!");
        } else {
            System.out.println("Different size!");
        }
    }

    public static class CombinedRun {

        public static void main(String... args) throws Exception {
            long currentTime = System.currentTimeMillis();
            GraphiteMappings.main("server", "server.jar", "output.srg");
            GraphiteMappings.main("server server1.jar output1.srg".split(" "));
            CompareFiles.main("output.srg output1.srg".split(" "));
            System.out.println("Run configuration took: " + (System.currentTimeMillis() -
                    currentTime) / 1000 + " seconds.");
        }

    }

}
