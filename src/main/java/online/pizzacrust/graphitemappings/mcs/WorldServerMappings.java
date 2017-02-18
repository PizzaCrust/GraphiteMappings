package online.pizzacrust.graphitemappings.mcs;

import java.util.Optional;
import java.util.jar.JarFile;

import online.pizzacrust.graphitemappings.MappingsBase;
import online.pizzacrust.graphitemappings.TypeMappings;
import online.pizzacrust.graphitemappings.srg.TypeNameEnforcer;

@TypeMappings("net.minecraft.world.WorldServer")
public class WorldServerMappings extends MappingsBase {

    private final MinecraftServerMappings minecraftServerMappings;

    public WorldServerMappings(JarFile jarFile, MinecraftServerMappings minecraftServerMappings) {
        super(jarFile);
        this.minecraftServerMappings = minecraftServerMappings;
    }

    @Override
    protected String obfName() {
        Optional<String> worldServerOpt = minecraftServerMappings.getMappings()
                .getObfuscatedClassName(getJavaType().getJvmStandard());
        if (worldServerOpt.isPresent()) {
            return worldServerOpt.get();
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    protected void remap() {
        findNode(getObfType().getJvmStandard()).ifPresent((classNode -> {
            if (!(new TypeNameEnforcer(classNode.superName).getReflectionStandard().equals("java.lang" +
                    ".Object"))) {
                getMappings().putClass(classNode.superName, "net.minecraft.world.World");
            }
        }));
    }

}
