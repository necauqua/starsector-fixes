package dev.necauqua.scm;

import org.objectweb.asm.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.ProtectionDomain;
import java.util.jar.JarFile;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.objectweb.asm.Opcodes.*;

public final class Agent implements ClassFileTransformer {

    @Override
    public byte[] transform(Module module, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (!"com/fs/graphics/TextureLoader".equals(className)) {
            return classfileBuffer;
        }

        var reader = new ClassReader(classfileBuffer);
        var writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        var visitor = new ClassVisitor(ASM9, writer) {

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                var mv = super.visitMethod(access, name, descriptor, signature, exceptions);

                if (!"(Ljava/nio/ByteBuffer;Ljava/lang/String;)V".equals(descriptor)) {
                    return mv;
                }

                log("Patched that one method containing the DirectBuffer.cleaner");

//                if (param1 != `float`){
//                    oO0000.warn(String.format("Buffer for texture %s can not be destroyed", new Object[]{paramString}));
//                }

                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(
                    GETSTATIC,
                    className,
                    "float",
                    "Ljava/nio/ByteBuffer;"
                );
                var skip = new Label();
                mv.visitJumpInsn(IF_ACMPNE, skip);
                mv.visitInsn(RETURN);
                mv.visitLabel(skip);
                mv.visitFieldInsn(
                    GETSTATIC,
                    className,
                    "oO0000",
                    "Lorg/apache/log4j/Logger;"
                );
                mv.visitLdcInsn("Buffer for texture %s can not be destroyed");
                mv.visitInsn(ICONST_1);
                mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
                mv.visitInsn(DUP);
                mv.visitInsn(ICONST_0);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitInsn(AASTORE);

                mv.visitMethodInsn(
                    INVOKESTATIC,
                    "java/lang/String",
                    "format",
                    "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;",
                    false
                );
                mv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    "org/apache/log4j/Logger",
                    "warn",
                    "(Ljava/lang/Object;)V",
                    false
                );

                mv.visitInsn(RETURN);
                mv.visitMaxs(0, 0); // will be computed
                mv.visitEnd();

                return null;
            }
        };
        reader.accept(visitor, ClassReader.SKIP_FRAMES | ClassReader.EXPAND_FRAMES | ClassReader.SKIP_DEBUG);
        return writer.toByteArray();
    }

    public static void premain(String argument, Instrumentation instrumentation) {
        instrumentation.addTransformer(new Agent());

        // loading extra libs
        try (var file = new JarFile(Agent.class.getProtectionDomain().getCodeSource().getLocation().getFile())) {
            for (var entries = file.entries(); entries.hasMoreElements(); ) {
                var entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().startsWith("extraLibs/")) {
                    continue;
                }
                var name = entry.getName().substring(10);
                log("Found a packaged library " + name + ", extracting and adding to classpath");
                try {
                    var input = file.getInputStream(entry);

                    // not placing them in the starsector folder in case the game is installed in
                    // a read-only location, such as the nix store
                    var tempFile = Path.of(System.getProperty("java.io.tmpdir"), name);

                    // and also copying every time to avoid it being kind of a security hole I guess lol
                    Files.copy(input, tempFile, REPLACE_EXISTING);

                    instrumentation.appendToSystemClassLoaderSearch(new JarFile(tempFile.toFile()));
                } catch (IOException e) {
                    throw new RuntimeException("Failed to add " + name + " library", e);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read agent JAR file", e);
        }
    }

    private static void log(Object message) {
        System.out.println("[STARSECTOR-PATCHER] " + message);
    }
}
