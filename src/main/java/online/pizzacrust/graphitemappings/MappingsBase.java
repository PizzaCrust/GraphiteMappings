package online.pizzacrust.graphitemappings;

import com.google.common.collect.ImmutableList;

import net.techcable.srglib.JavaType;
import net.techcable.srglib.format.MappingsFormat;
import net.techcable.srglib.mappings.Mappings;
import net.techcable.srglib.mappings.MutableMappings;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import sun.misc.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class MappingsBase {

    private final MutableMappings mappings = MutableMappings.create();
    private final JarFile jarFile;

    public MutableMappings getMappings() {
        return mappings;
    }

    abstract String obfName();

    abstract void remap();

    public static Mappings chainClasses(List<MappingsBase> mappingsBases) {
        List<MutableMappings> mutableMappingss = new ArrayList<>();
        MutableMappings classMappings = MutableMappings.create();
        mappingsBases.forEach((mappingsBase -> {
            classMappings.putClass(JavaType.fromInternalName(mappingsBase.obfName().replace('.',
                    '/')),
                    JavaType
                    .fromInternalName
                    (mappingsBase
                    .getRemappedJvmName()));
            mappingsBase.remap();
            mutableMappingss.add(mappingsBase.getMappings());
        }));
        return Mappings.chain(ImmutableList.copyOf(mutableMappingss));
    }

    public static void writeClasses(List<MappingsBase> mappingsBases, File file) throws IOException {
        MappingsFormat.SEARGE_FORMAT.writeToFile(chainClasses(mappingsBases), file);
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