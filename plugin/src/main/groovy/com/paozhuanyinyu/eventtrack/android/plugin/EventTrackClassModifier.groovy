package com.paozhuanyinyu.eventtrack.android.plugin

import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.regex.Matcher
import java.util.zip.ZipEntry

class EventTrackClassModifier{
    private static HashSet<String> exclude = new HashSet<>()
    static{
        exclude = new HashSet<>()
        exclude.add('androidx.appcompat')
        exclude.add('com.paozhuanyinyu.eventtrack.android.sdk')
    }
    protected static File modifyJar(File jarFile, File tempDir, boolean nameHex){
        def file = new JarFile(jarFile)
        def hexName = ""
        if(nameHex){
            hexName = DigestUtils.md5Hex(jarFile.absolutePath).substring(0, 8)
        }
        def outputJar = new File(tempDir, hexName + jarFile.name)
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputJar))
        Enumeration enumeration = file.entries()
        while(enumeration.hasMoreElements()){
            JarEntry jarEntry = (JarEntry) enumeration.nextElement()
            InputStream inputStream = null
            try {
                inputStream = file.getInputStream(jarEntry)
            } catch (Exception e) {
                return null
            }
            String entryName = jarEntry.getName()
            String className
            ZipEntry zipEntry = new ZipEntry(entryName)
            jarOutputStream.putNextEntry(zipEntry)
            byte[] modifiedClassBytes = null
            byte[] sourceClassBytes = IOUtils.toByteArray(inputStream)
            if(entryName.endsWith(".class")){
                className = entryName.replace(Matcher.quoteReplacement(File.separator),".").replace(".class","")
                if(isShouldModify(className)){
                    modifiedClassBytes = modifyClass(sourceClassBytes)
                }
            }
            if(modifiedClassBytes == null){
                modifiedClassBytes = sourceClassBytes
            }
            jarOutputStream.write(modifiedClassBytes)
            jarOutputStream.closeEntry()
        }
        jarOutputStream.close()
        file.close()
        return outputJar
    }
    private static byte[] modifyClass(byte[] srcClass){
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)
        ClassVisitor classVisitor = new EventTrackClassVisitor(classWriter);
        ClassReader classReader = new ClassReader(srcClass)
        classReader.accept(classVisitor, ClassReader.SKIP_FRAMES)
        return classWriter.toByteArray()
    }
    protected static boolean isShouldModify(String className){
        Iterator<String> iterator = exclude.iterator()
        while (iterator.hasNext()) {
            String packageName = iterator.next()
            if (className.startsWith(packageName)) {
                return false
            }
        }

        if (className.contains('R$') ||
                className.contains('R2$') ||
                className.contains('R.class') ||
                className.contains('R2.class') ||
                className.contains('BuildConfig.class')) {
            return false
        }

        return true
    }
    protected static File modifyClassFile(File dir, File classFile, File tempDir){
        File modified = null
        try {
            String className = path2ClassName(classFile.absolutePath.replace(dir.absolutePath + File.separator,""))
            byte[] sourceClassBytes = IOUtils.toByteArray(new FileInputStream(classFile))
            byte[] modifyClassBytes = modifyClass(sourceClassBytes)
            if(modifyClassBytes){
                modified = new File(tempDir,className.replace('.','') + '.class')
                if(modified.exists()){
                    modified.delete()
                }
                modified.createNewFile()
                new FileOutputStream(modified).write(modifyClassBytes)
            }
        } catch(Exception e){
            e.printStackTrace()
            modified = classFile
        }
        return modified
    }
    private static String path2ClassName(String pathName){
        pathName.replace(File.separator, ".").replace(".class","")
    }
}