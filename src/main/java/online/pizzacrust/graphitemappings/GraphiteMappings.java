package online.pizzacrust.graphitemappings;

import java.io.File;
import java.util.Arrays;
import java.util.jar.JarFile;

import online.pizzacrust.graphitemappings.mcs.DedicatedServerMappings;
import online.pizzacrust.graphitemappings.mcs.MinecraftServerMappings;

public class GraphiteMappings {

    public static void main(String... args) throws Exception {
        String clientOrServer = args[0];
        File jar = new File(args[1]);
        File output = new File(args[2]);
        JarFile jarFile = new JarFile(jar);
        if (clientOrServer.equals("server")) {
            System.out.println("Generating server mappings from JAR...");
            MinecraftServerMappings mcServerMappings = new MinecraftServerMappings(jarFile);
            DedicatedServerMappings dedicatedServerMappings = new DedicatedServerMappings
                    (jarFile, mcServerMappings);
            MappingsBase.writeClasses(Arrays.asList(mcServerMappings, dedicatedServerMappings), output);
        }
    }

}
