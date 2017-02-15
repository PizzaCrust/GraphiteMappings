package online.pizzacrust.graphitemappings;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import sun.misc.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import lombok.RequiredArgsConstructor;
import online.pizzacrust.graphitemappings.srg.Mappings;
import online.pizzacrust.graphitemappings.srg.TypeNameEnforcer;

@RequiredArgsConstructor
public abstract class MappingsBase {

    private final Mappings mappings = new Mappings();
    private final JarFile jarFile;

    public Mappings getMappings() {
        return mappings;
    }

    abstract String obfName();

    abstract void remap();

    public static Mappings chainClasses(List<MappingsBase> mappingsBases) {
        List<Mappings> mutableMappingss = new ArrayList<>();
        Mappings classMappings = new Mappings();
        mutableMappingss.add(classMappings);
        mappingsBases.forEach((mappingsBase -> {
            classMappings.putClass(mappingsBase.getObfType().getJvmStandard(), mappingsBase
                    .getJavaType().getJvmStandard());
            mappingsBase.remap();
            mutableMappingss.add(mappingsBase.getMappings());
        }));
        return Mappings.chain(mutableMappingss);
    }

    public TypeNameEnforcer getJavaType() {
        //return JavaType.fromInternalName(this.getRemappedJvmName());
        return new TypeNameEnforcer(this.getRemappedJvmName());
    }

    public TypeNameEnforcer getObfType() {
        //return JavaType.fromInternalName(obfName().replace('.', '/'));
        return new TypeNameEnforcer(obfName());
    }

    public static void writeClasses(List<MappingsBase> mappingsBases, File file) throws IOException {
        List<String> lines = chainClasses(mappingsBases).lines();
        Files.write(file.toPath(), lines, StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.CREATE);
    }

    public String getRemappedReflectName() {
        if (this.getClass().isAnnotationPresent(TypeMappings.class)) {
            return this.getClass().getAnnotation(TypeMappings.class).value();
        }
        throw new RuntimeException("No annotation present!");
    }

    public String getRemappedJvmName() {
        return getRemappedReflectName().replace('.', '/');
    }

    private List<ClassNode> indexJar() {
        ArrayList<ClassNode> classNodes = new ArrayList<ClassNode>();
        Enumeration<JarEntry> enumeration = jarFile.entries();
        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = enumeration.nextElement();
            if (jarEntry != null) {
                if (jarEntry.getName().endsWith(".class")) {
                    try {
                        byte[] classBytes = IOUtils.readFully(jarFile.getInputStream(jarEntry), -1,
                                false);
                        ClassReader classReader = new ClassReader(classBytes);
                        ClassNode classNode = new ClassNode();
                        classReader.accept(classNode, 0);
                        classNodes.add(classNode);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return classNodes;
    }

    public Optional<ClassNode> findNode(String reflectName) {
        String jvmName = reflectName.replace('.', '/');
        for (ClassNode classNode : indexJar()) {
            if (classNode.name.equals(jvmName)) {
                return Optional.of(classNode);
            }
        }
        return Optional.empty();
    }

}