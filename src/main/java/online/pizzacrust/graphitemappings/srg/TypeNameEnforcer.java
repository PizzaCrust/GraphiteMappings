package online.pizzacrust.graphitemappings.srg;

import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.MethodNode;

public class TypeNameEnforcer {

    private final String original;

    public TypeNameEnforcer(String original) {
        this.original = original;
    }

    public String getJvmStandard() {
        return original.replace('.', '/');
    }

    public String getReflectionStandard() {
        return original.replace('/', '.');
    }

    public static TypeNameEnforcer getReturnType(MethodNode methodNode) {
        Method method = new Method(methodNode.name, methodNode.desc);
        return new TypeNameEnforcer(method.getReturnType().getClassName());
    }

}
