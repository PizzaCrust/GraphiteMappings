package online.pizzacrust.graphitemappings.srg;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mappings {

    private final Map<String, String> classMappings = new HashMap<>();
    private final Map<FieldRef, FieldRef> fieldMappings = new HashMap<>();
    private final Map<MethodRef, MethodRef> methodMappings = new HashMap<>();

    public void putClass(String a, String b) {
        classMappings.put(a, b);
    }

    public static void main(String... args) throws Exception {
        System.out.println(Type.getObjectType("test/meow").getClassName());
        String descriptor = Type.getMethodDescriptor(Type.getObjectType("test/meow"));
        System.out.println(descriptor);
    }

    public void putMethod(MethodRef a, MethodRef b) {
        Method descriptor = new Method(b.getMethodName(), b.getMethodDesc());
        TypeNameEnforcer returnType = new TypeNameEnforcer(descriptor.getReturnType()
                .getClassName());
        if (classMappings.containsKey(returnType.getJvmStandard())) {
            returnType = new TypeNameEnforcer(classMappings.get(returnType.getJvmStandard()));
        }
        List<TypeNameEnforcer> parameterTypes = new ArrayList<>();
        for (Type type : descriptor.getArgumentTypes()) {
            if (classMappings.containsKey(new TypeNameEnforcer(type.getClassName()).getJvmStandard())) {
                parameterTypes.add(new TypeNameEnforcer(classMappings.get(new TypeNameEnforcer
                        (type.getClassName()).getJvmStandard())));
            } else {
                parameterTypes.add(new TypeNameEnforcer(type.getClassName()));
            }
        }
        List<Type> parameterASM = new ArrayList<>();
        Type[] parameters = parameterASM.toArray(new Type[parameterASM.size()]);
        parameterTypes.forEach((typeNameEnforcer -> parameterASM.add(Type.getObjectType(typeNameEnforcer.getJvmStandard()))));
        MethodRef newB = new MethodRef(b.getClassName(), b.getMethodName(), Type.getMethodDescriptor(Type
                .getObjectType(returnType
                .getJvmStandard()), parameters));
        methodMappings.put(a, newB);
    }

    public void putField(FieldRef a, FieldRef b) {
        fieldMappings.put(a, b);
    }

    public static Mappings chain(List<Mappings> mappingss) {
        Mappings central = new Mappings();
        mappingss.forEach((mappings -> {
            mappings.classMappings.forEach(central::putClass);
            mappings.fieldMappings.forEach(central::putField);
            mappings.methodMappings.forEach(central::putMethod);
        }));
        return central;
    }

    public List<String> lines() {
        List<String> lines = new ArrayList<>();
        classMappings.forEach((a, b) -> lines.add("CL: ".concat(a).concat(" ").concat(b)));
        fieldMappings.forEach((a, b) -> lines.add("FD: ".concat(a.toString()).concat(" ").concat(b.toString())));
        methodMappings.forEach((a, b) -> lines.add("MD: ".concat(a.toString()).concat(" ").concat
                (b.toString())));
        return lines;
    }

}
