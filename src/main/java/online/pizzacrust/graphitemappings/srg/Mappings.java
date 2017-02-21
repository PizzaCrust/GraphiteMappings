package online.pizzacrust.graphitemappings.srg;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static online.pizzacrust.graphitemappings.MappingsBase.isPrimitiveType;

public class Mappings {

    private final Map<String, String> classMappings = new HashMap<>();
    private final Map<FieldRef, FieldRef> fieldMappings = new HashMap<>();
    private final Map<MethodRef, MethodRef> methodMappings = new HashMap<>();

    public void putClass(String a, String b) {
        classMappings.put(new TypeNameEnforcer(a).getJvmStandard(), new TypeNameEnforcer(b).getJvmStandard());
    }

    public static void main(String... args) throws Exception {
        System.out.println(Type.getObjectType("test/meow").getClassName());
        String descriptor = Type.getMethodDescriptor(Type.getObjectType("test/meow"));
        System.out.println(descriptor);
        System.out.println(createDescriptor(new TypeNameEnforcer("boolean"), Collections.emptyList()));
    }

    public Optional<String> getObfuscatedClassName(String remappedName) {
        for (Map.Entry<String, String> entry : this.classMappings.entrySet()) {
            if (entry.getValue().equals(new TypeNameEnforcer(remappedName).getJvmStandard())) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    private static String transformPrimitive(TypeNameEnforcer typeNameEnforcer) {
        if (typeNameEnforcer.getJvmStandard().equals("boolean")) {
            return "Z";
        }
        if (typeNameEnforcer.getJvmStandard().equals("integer")) {
            return "I";
        }
        if (typeNameEnforcer.getJvmStandard().equals("double")) {
            return "D";
        }
        if (typeNameEnforcer.getJvmStandard().equals("float")) {
            return "F";
        }
        if (typeNameEnforcer.getJvmStandard().equals("char")) {
            return "C";
        }
        if (typeNameEnforcer.getJvmStandard().equals("long")) {
            return "J";
        }
        if (typeNameEnforcer.getJvmStandard().equals("byte")) {
            return "B";
        }
        if (typeNameEnforcer.getJvmStandard().equals("short")) {
            return "S";
        }
        return typeNameEnforcer.getJvmStandard();
    }

    public static String createDescriptor(TypeNameEnforcer returnType, List<TypeNameEnforcer>
            typeNameEnforcers) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('(');
        typeNameEnforcers.forEach((typeNameEnforcer -> {
            if (isPrimitiveType(typeNameEnforcer.getJvmStandard())) {
                stringBuilder.append(transformPrimitive(typeNameEnforcer));
            } else {
                stringBuilder.append('L');
                stringBuilder.append(typeNameEnforcer.getJvmStandard());
                stringBuilder.append(';');
            }
        }));
        stringBuilder.append(')');
        if (isPrimitiveType(returnType.getJvmStandard())) {
            stringBuilder.append(transformPrimitive(returnType));
        } else {
            stringBuilder.append('L');
            stringBuilder.append(returnType.getJvmStandard());
            stringBuilder.append(';');
        }
        return stringBuilder.toString();
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
        MethodRef newB = new MethodRef(b.getClassName(), b.getMethodName(), createDescriptor
                (returnType, parameterTypes));
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
