package online.pizzacrust.graphitemappings.mcs;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarFile;

import jdk.internal.org.objectweb.asm.Opcodes;
import online.pizzacrust.graphitemappings.MappingsBase;
import online.pizzacrust.graphitemappings.TypeMappings;

@TypeMappings("net.minecraft.server.MinecraftServer")
public class MinecraftServerMappings extends MappingsBase {

    public MinecraftServerMappings(JarFile jarFile) {
        super(jarFile);
    }

    @Override
    protected String obfName() {
        return "net.minecraft.server.MinecraftServer";
    }

    private boolean containsMethod(ClassNode newNode, MethodNode oldNode) {
        for (MethodNode methodNode : newNode.methods) {
            //System.out.println(methodNode.name.equals(oldNode.name) + " " + methodNode.name + "
            // " +
            //        "vs " + oldNode.name);
            if (methodNode.name.equals(oldNode.name) && methodNode.desc.equals(oldNode.desc)) {
                return true;
    }
}
        return false;
    }

    private boolean equalsList(List list, List list1) {
        list.removeAll(list1);
        return list.size() == 0;
    }

    @Override
    protected void remap() {
        Optional<ClassNode> minecraftServer = findNode("net.minecraft.server.MinecraftServer");
        if (!minecraftServer.isPresent()) {
            throw new RuntimeException("This is not the server!");
        }
        ClassNode mcs = minecraftServer.get();
        for (FieldNode fieldNode : mcs.fields) {
            String className = Type.getType(fieldNode.desc).getClassName();
            if (className.equals("org.apache.logging.log4j.Logger")) {
                getMappings().putField(createObfFd(fieldNode), createRemappedFd("logger"));
            }
        }
        for (MethodNode methodNode : mcs.methods) {
            Method descriptor = new Method(methodNode.name, methodNode.desc);
            if (methodNode.name.equals("main")) {
                if (descriptor.getArgumentTypes().length >= 1) {
                    Type argument = descriptor.getArgumentTypes()[0];
                    if (argument.getClassName().equals("java.lang.String[]")) {
                        final boolean[] mapped = {false};
                        methodNode.instructions.iterator().forEachRemaining((insnNode) -> {
                            if (insnNode instanceof MethodInsnNode) {
                                MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
                                if (methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                                    Method insnMethod = new Method(methodInsnNode.name,
                                            methodInsnNode.desc);
                                    if (insnMethod.getArgumentTypes().length == 0 && insnMethod
                                            .getReturnType().equals(Type.VOID_TYPE)) {
                                        if (!mapped[0]) {
                                            getMappings().putClass(methodInsnNode.owner, "net" +
                                                    ".minecraft.server.dedicated.DedicatedServer");
                                            mapped[0] = true;
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            }
            if (descriptor.getReturnType().getClassName().equals("java.lang.String")) {
                // trying to getMinecraftServerVersion
                methodNode.instructions.iterator().forEachRemaining((instruction) -> {
                    if (instruction instanceof LdcInsnNode) {
                        LdcInsnNode ldcInsnNode = (LdcInsnNode) instruction;
                        if (ldcInsnNode.cst instanceof String) {
                            String constant = (String) ldcInsnNode.cst;
                            if (!(constant.equals("vanilla"))) {
                                if (constant.equals("Server")) {
                                    // this is getName() from ICommandSender
                                    getMappings().putMethod(createObfMd(methodNode),
                                            createRemappedMd("getName", methodNode));
                                    //find the parent class
                                    interfacesOf(mcs).forEach((parentInterface) -> {
                                        if (containsMethod(parentInterface, methodNode)) {
                                            // this means it is ICommandSender
                                            getMappings().putClass(parentInterface.name, "net" +
                                                    ".minecraft.server.command.ICommandSender");
                                        }
                                    });
                                } else {
                                    // this is getMinecraftVersion()
                                    getMappings().putMethod(createObfMd(methodNode),
                                            createRemappedMd("getMinecraftVersion", methodNode));
                                }
                            }
                        }
                    }
                });
            }
        }
    }

}
