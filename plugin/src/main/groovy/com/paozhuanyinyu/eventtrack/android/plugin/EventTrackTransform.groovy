package com.paozhuanyinyu.eventtrack.android.plugin

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import groovy.io.FileType
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

class EventTrackTransform extends Transform{
    Project project;
    EventTrackTransform(Project project) {
        this.project = project
    }

    @Override
    String getName() {
        return "EventTrack"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        if(!isIncremental()){
            transformInvocation.getOutputProvider().deleteAll()
        }
        println("################## EventTrack ##################")
        transformInvocation.getInputs().each { TransformInput input ->
            //遍历class文件
            input.directoryInputs.each { DirectoryInput directoryInput ->
                def dest = transformInvocation.getOutputProvider().getContentLocation(directoryInput.name,directoryInput.contentTypes,directoryInput.scopes, Format.DIRECTORY)
                File dir = directoryInput.file
                if(dir){
                    HashMap<String, File> modifyMap = new HashMap<>()
                    dir.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/){
                        File classFile ->
                            if(EventTrackClassModifier.isShouldModify(classFile.name)){
                                File modified = EventTrackClassModifier.modifyClassFile(dir,classFile,transformInvocation.context.getTemporaryDir())
                                if(modified != null){
                                    String ke = classFile.absolutePath.replace(dir.getAbsolutePath(), "")
                                    modifyMap.put(ke, modified)
                                }
                            }
                    }
                    FileUtils.copyDirectory(directoryInput.file,dest)
                    modifyMap.entrySet().each {
                        Map.Entry<String,File> en ->
                            File target = new File(dest.absolutePath + en.getKey())
                            if(target.exists()){
                                target.delete()
                            }
                            FileUtils.copyFile(en.getValue(),target)
                            en.getValue().delete()
                    }
                }


            }
            //遍历jar包
            input.jarInputs.each { JarInput jarInput->
                String destName = jarInput.file.name
                def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath).substring(0,8)
                if(destName.endsWith(".jar")){
                    destName = destName.substring(0, destName.length() - 4)
                }
                File dest = transformInvocation.getOutputProvider().getContentLocation(destName + "_" + hexName,jarInput.contentTypes,jarInput.scopes,Format.JAR)
                def modifiedJar = EventTrackClassModifier.modifyJar(jarInput.file, transformInvocation.getContext().getTemporaryDir(),true)
                if(modifiedJar == null){
                    modifiedJar = jarInput.file
                }
                FileUtils.copyFile(modifiedJar,dest)
                modifiedJar.delete()
            }
        }
        println("################## EventTrack ##################")
    }
}