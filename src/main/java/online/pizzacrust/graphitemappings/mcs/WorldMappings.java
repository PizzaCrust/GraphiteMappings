package online.pizzacrust.graphitemappings.mcs;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.util.Optional;
import java.util.jar.JarFile;

import online.pizzacrust.graphitemappings.MappingsBase;
import online.pizzacrust.graphitemappings.TypeMappings;

@TypeMappings("net.minecraft.world.World")
public class WorldMappings extends MappingsBase {

    private final WorldServerMappings worldServerMappings;

    public WorldMappings(JarFile jarFile, WorldServerMappings worldServerMappings) {
        super(jarFile);
        this.worldServerMappings = worldServerMappings;
    }

    @Override
    protected String obfName() {
        Optional<String> mappedName = worldServerMappings.getMappings().getObfuscatedClassName
                (getJavaType().getJvmStandard());
        if (mappedName.isPresent()) {
            return mappedName.get();
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    protected void remap() {
        findNode(getObfType().getJvmStandard()).ifPresent((classNode -> classNode.methods.forEach((methodNode -> {
            if (methodNode.name.equals("<init>")) {
                methodNode.instructions.iterator().forEachRemaining((insnNode) -> {
                    if (insnNode.getOpcode() == Opcodes.ANEWARRAY) {
                        if (insnNode instanceof TypeInsnNode) {
                            TypeInsnNode typeInsnNode = (TypeInsnNode) insnNode;
                            Type type = Type.getType(typeInsnNode.desc);
                            getMappings().putClass(type.getInternalName(), "net.minecraft.world" +
                                    ".IWorldEventListener");
                        }
                    }
                });
            }
        }))));
    }

}
