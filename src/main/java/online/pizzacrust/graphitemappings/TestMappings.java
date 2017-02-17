package online.pizzacrust.graphitemappings;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.jar.JarFile;

import online.pizzacrust.graphitemappings.srg.MethodRef;
import online.pizzacrust.graphitemappings.srg.TypeNameEnforcer;

public class TestMappings {

    public static void main(String... args) throws Exception {
        MappingsBase.writeClasses(Arrays.asList(new TestMappingsJava()), new File(args[0]));
    }

    public static String oops() {
        return "oops";
    }

    public static String yes() {
        return "yes";
    }

    @TypeMappings("RemappedTest")
    public static class TestMappingsJava extends MappingsBase {

        public TestMappingsJava() throws IOException {
            super(new JarFile(new File(TestMappingsJava.class.getProtectionDomain().getCodeSource()
                    .getLocation
                    ().getPath())));
        }

        @Override
        protected String obfName() {
            return TestMappings.class.getName();
        }

        @Override
        protected void remap() {
            Optional<ClassNode> classNode = findNode(TestMappings.class.getName());
            classNode.ifPresent((cn) -> {
                for (MethodNode method : cn.methods) {
                    TypeNameEnforcer returnType = TypeNameEnforcer.getReturnType(method);
                    if (returnType.getJvmStandard().equals("java/lang/String")) {
                        method.instructions.iterator().forEachRemaining((insnNode) -> {
                            if (insnNode instanceof LdcInsnNode) {
                                LdcInsnNode ldcInsnNode = (LdcInsnNode) insnNode;
                                String payload = (String) ldcInsnNode.cst;
                                if (!payload.equals("oops")) {
                                    MethodRef methodRef = createObfMd(method);
                                    MethodRef methodRef1 = createRemappedMd("correct", method);
                                    getMappings().putMethod(methodRef, methodRef1);
                                }
                            }
                        });
                    }
                }
            });
        }

    }

}
