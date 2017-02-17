package online.pizzacrust.graphitemappings.mcs;

import org.objectweb.asm.tree.ClassNode;

import java.util.List;
import java.util.Optional;
import java.util.jar.JarFile;

import online.pizzacrust.graphitemappings.MappingsBase;
import online.pizzacrust.graphitemappings.TypeMappings;

@TypeMappings("net.minecraft.server.dedicated.DedicatedServer")
public class DedicatedServerMappings extends MappingsBase {

    private final MinecraftServerMappings minecraftServerMappings;

    public DedicatedServerMappings(JarFile jarFile, MinecraftServerMappings minecraftServerMappings) {
        super(jarFile);
        this.minecraftServerMappings = minecraftServerMappings;
    }

    @Override
    protected String obfName() {
        Optional<String> dedicatedServerOpt = minecraftServerMappings.getMappings().getObfuscatedClassName
                (getJavaType()
                .getJvmStandard());
        if (dedicatedServerOpt.isPresent()) {
            return dedicatedServerOpt.get();
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    protected void remap() {
        findNode(getObfType().getJvmStandard()).ifPresent((classNode -> {
            List<ClassNode> interfaces = interfacesOf(classNode);
            if (interfaces.size() == 1) {
                final boolean[] already = {false};
                interfaces.forEach((interfaceNode) -> {
                    if (!already[0]) {
                        getMappings().putClass(interfaceNode.name, "net.minecraft.network.rcon" +
                                        ".IServer");
                        already[0] = true;
                    }
                });
            }
        }));
    }

}
