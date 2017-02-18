package online.pizzacrust.graphitemappings.mcs;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarFile;

import jdk.internal.org.objectweb.asm.Opcodes;
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
            for (MethodNode methodNode : classNode.methods) {
                AtomicBoolean captureNextNew = new AtomicBoolean(false);
                methodNode.instructions.iterator().forEachRemaining((insnNode) -> {
                    if (insnNode instanceof TypeInsnNode) {
                        TypeInsnNode typeInsnNode = (TypeInsnNode) insnNode;
                        if (typeInsnNode.getOpcode() == Opcodes.NEW && captureNextNew.get()) {
                            getMappings().putClass(Type.getType(typeInsnNode.desc)
                                    .getInternalName(), "net.minecraft.server.dedicated" +
                                    ".PropertyManager");
                            captureNextNew.set(false);
                        }
                    }
                    if (insnNode instanceof LdcInsnNode) {
                        LdcInsnNode ldcInsnNode = (LdcInsnNode) insnNode;
                        if (ldcInsnNode.cst instanceof String) {
                            String cst = (String) ldcInsnNode.cst;
                            if (cst.equals("Server console handler")) {
                                getMappings().putMethod(createObfMd(methodNode), createRemappedMd
                                        ("init", methodNode));
                            }
                            if (cst.equals("Loading properties")) {
                                captureNextNew.set(true);
                            }
                            if (cst.equals("eula.txt")) {
                                // next 2 instructions
                                AbstractInsnNode abstractInsnNode = insnNode.getNext().getNext();
                                if (abstractInsnNode instanceof MethodInsnNode) {
                                    MethodInsnNode methodInsnNode = (MethodInsnNode)
                                            abstractInsnNode;
                                    if (methodInsnNode.getOpcode() == Opcodes.INVOKESPECIAL) {
                                        getMappings().putClass(methodInsnNode.owner, "net" +
                                                ".minecraft.server.ServerEula");
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }));
    }

}
