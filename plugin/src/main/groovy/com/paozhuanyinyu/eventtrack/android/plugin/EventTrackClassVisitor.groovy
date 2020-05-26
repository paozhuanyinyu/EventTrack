package com.paozhuanyinyu.eventtrack.android.plugin

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Handle
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

class EventTrackClassVisitor extends ClassVisitor implements Opcodes{
    private ClassVisitor classVisitor
    private String[] mInterfaces
    private HashMap<String, EventTrackMethodInfo> mLambdaMethodInfos = new HashMap<>()
    private final static String SDK_API_CLASS = "com/paozhuanyinyu/eventtrack/android/sdk/EventTrackHelper"
    EventTrackClassVisitor(ClassVisitor classVisitor) {
        super(Opcodes.ASM7, classVisitor)
        this.classVisitor = classVisitor
    }
    /**
     * 当扫描类时第一个会调用的方法
     * @param version JDK的版本，51代表JDK1.7, 52代表JDK1.8
     * @param access 类的修饰符
     * @param name 类的名称，格式是a/b/c/MyClass
     * @param signature 表示泛型信息
     * @param superName 当前类所继承的父类
     * @param interfaces 类所实现的接口列表
     */
    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces)
        mInterfaces = interfaces
        println("version: " + version + ";access: " + access + ";name: " + name + ";signature: " + signature + ";superName: " + superName + ";interfaces: " + interfaces)
    }

    /**
     * 访问方法
     * @param access 方法的修饰符
     * @param name 方法名
     * @param descriptor 方法签名：格式是(参数列表)返回值类型
     * @param signature 泛型相关的信息
     * @param exceptions 将会抛出的异常
     * @return
     */
    @Override
    MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
//        println("access: " + access + ";name: " + name + ";descroptor: " + descriptor + ";signature: " + signature + ";exception: " + exceptions)
        MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        String nameDesc = name + descriptor
        methodVisitor = new EventTrackDefaultMethodVisitor(Opcodes.ASM7,methodVisitor,access,name,descriptor){
            private boolean isEventTrackViewClickAnnotation = false
            /**
             * 访问结束
             */
            @Override
            void visitEnd() {
                super.visitEnd()
                if(mLambdaMethodInfos.containsKey(nameDesc)){
                    mLambdaMethodInfos.remove(nameDesc)
                }
            }
            /**
             * 访问动态化指令，例如lambda表达式
             * @param name1
             * @param desc1
             * @param bootstrapMethodHandle
             * @param bootstrapMethodArguments
             */
            @Override
            void visitInvokeDynamicInsn(String name1, String desc1, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
                super.visitInvokeDynamicInsn(name1, desc1, bootstrapMethodHandle, bootstrapMethodArguments)
//                String desc2 = (String)bootstrapMethodArguments[0]
//                println("name1: " + name1 + ";desc1: " + desc1 + ";desc2: " + desc2)
//                println("type: " + Type.getReturnType(desc1).getDescriptor())
//                Handle it = bootstrapMethodArguments[1]
//                if(it != null){
//                    println("it.name: " + it.name + ";desc: " + it.desc)
//                }
                try{
                    String desc2 = bootstrapMethodArguments[0]
                    EventTrackMethodInfo eventTrackMethodInfo = EventTrackLambdaConfig.LAMBDA_METHODS.get(Type.getReturnType(desc1).getDescriptor() + name1 + desc2)
                    if(eventTrackMethodInfo != null){
                        Handle it = (Handle)bootstrapMethodArguments[1]
                        mLambdaMethodInfos.put(it.name + it.desc, eventTrackMethodInfo)
                    }
                }catch(Exception e){
                    e.printStackTrace()
                }
            }
            /**
             * 访问方法的注解
             * @param s
             * @param b
             * @return
             */
            @Override
            AnnotationVisitor visitAnnotation(String s, boolean b) {
                if(s == 'Lcom/paozhuanyinyu/eventtrack/android/sdk/EventTrackViewOnClick;'){
                    isEventTrackViewClickAnnotation = true
                    println("isEventTrackViewClickAnnotation: " + isEventTrackViewClickAnnotation)
                }
                return super.visitAnnotation(s, b)
            }
            /**
             * 方法退出
             * @param opcode
             */
            @Override
            protected void onMethodExit(int opcode) {
                super.onMethodExit(opcode)
                EventTrackMethodInfo eventTrackMethodInfo = mLambdaMethodInfos.get(nameDesc)
                if(eventTrackMethodInfo != null){
                    Type[] types = Type.getArgumentTypes(eventTrackMethodInfo.desc)
                    int length = types.length
                    Type[] lambdaTypes = Type.getArgumentTypes(descriptor)
                    int paramStart = lambdaTypes.length - length
                    if(paramStart < 0){
                        return
                    }else{
                        for(int i = 0; i < length; i++){
                            if(lambdaTypes[paramStart + i].descriptor != types[i].descriptor){
                                return
                            }
                        }
                    }
                    boolean isStaticMethod = EventTrackUtils.isStatic(access)
                    if(!isStaticMethod){
                        if(eventTrackMethodInfo.desc == '(Landroid/view/MenuItem;)Z'){
                            methodVisitor.visitVarInsn(ALOAD, 0)
                            methodVisitor.visitVarInsn(ALOAD, getVisitPosition(lambdaTypes,paramStart,isStaticMethod))
                            methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS, eventTrackMethodInfo.agentName,'(Ljava/lang/Object;Landroid/view/MenuItem;)V', false)
                            return
                        }
                    }
                    for(int i = paramStart; i < paramStart + eventTrackMethodInfo.paramCount; i++){
                        methodVisitor.visitVarInsn(eventTrackMethodInfo.opcodes.get(i - paramStart),getVisitPosition(lambdaTypes, i, isStaticMethod))
                    }
                    methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS, eventTrackMethodInfo.agentName, eventTrackMethodInfo.agentDesc,false)
                    return
                }

                if(isEventTrackViewClickAnnotation && descriptor == '(Landroid/view/View;)V'){
                    methodVisitor.visitVarInsn(ALOAD,1)
                    methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS,"trackViewOnClick","(Landroid/view/View;)V",false)
                    return
                }
                if(nameDesc == 'onContextItemSelected(Landroid/view/MenuItem;)Z' || nameDesc == 'onOptionsItemSelected(Landroid/view/MenuItem;)Z'){
                    methodVisitor.visitVarInsn(ALOAD,0)
                    methodVisitor.visitVarInsn(ALOAD,1)
                    methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS,"trackViewOnClick","(Ljava/lang/Object;Landroid/view/MenuItem;)V",false)
                }
                if(mInterfaces != null && mInterfaces.length > 0){
                    if(mInterfaces.contains('android/view/View$OnClickListener') && nameDesc == 'onClick(Landroid/view/View;)V'){
                        methodVisitor.visitVarInsn(ALOAD,1)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS,"trackViewOnClick","(Landroid/view/View;)V",false)
                    }else if(mInterfaces.contains('android/content/DialogInterface$OnClickListener') && nameDesc == 'onClick(Landroid/content/DialogInterface;I)V'){
                        methodVisitor.visitVarInsn(ALOAD,1)
                        methodVisitor.visitVarInsn(ILOAD,2)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS,"trackViewOnClick","(Landroid/content/DialogInterface;I)V",false)
                    }else if(mInterfaces.contains('android/content/DialogInterface$OnMultiChoiceClickListener') && nameDesc == 'onClick(Landroid/content/DialogInterface;IZ)V'){
                        methodVisitor.visitVarInsn(ALOAD,1)
                        methodVisitor.visitVarInsn(ILOAD,2)
                        methodVisitor.visitVarInsn(ILOAD,3)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS,"trackViewOnClick","(Landroid/content/DialogInterface;IZ)V",false)
                    }else if(mInterfaces.contains('android/widget/CompoundButton$OnCheckedChangeListener') && nameDesc == 'onCheckedChanged(Landroid/widget/CompoundButton;Z)V'){
                        methodVisitor.visitVarInsn(ALOAD,1)
                        methodVisitor.visitVarInsn(ILOAD,2)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS,"trackViewOnClick","(Landroid/widget/CompoundButton;Z)V",false)
                    }else if(mInterfaces.contains('android/widget/RatingBar$OnRatingBarChangeListener') && nameDesc == 'onRatingChanged(Landroid/widget/RatingBar;FZ)V'){
                        methodVisitor.visitVarInsn(ALOAD,1)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS,"trackViewOnClick","(Landroid/view/View;)V",false)
                    }else if(mInterfaces.contains('android/widget/SeekBar$OnSeekBarChangeListener') && nameDesc == 'onStopTrackingTouch(Landroid/widget/SeekBar;)V'){
                        methodVisitor.visitVarInsn(ALOAD,1)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS,"trackViewOnClick","(Landroid/view/View;)V",false)
                    }else if(mInterfaces.contains('android/widget/AdapterView$OnItemSelectedListener') && nameDesc == 'onItemSeleted(Landroid/widget/AdapterView;Landroid/view/View;IJ)V'){
                        methodVisitor.visitVarInsn(ALOAD,1)
                        methodVisitor.visitVarInsn(ALOAD,2)
                        methodVisitor.visitVarInsn(ILOAD,3)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS,"trackViewOnClick","(Landroid/widget/AdapterView;Landroid/view/View;I)V",false)
                    }else if(mInterfaces.contains('android/widget/TabHost$OnTabChangeListener') && nameDesc == 'onTabChanged(Ljava/lang/String;)V'){
                        methodVisitor.visitVarInsn(ALOAD,1)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS,"trackTabHost","(Ljava/lang/String;)V",false)
                    }else if(mInterfaces.contains('android/widget/AdapterView$OnItemClickListener') && nameDesc == 'onItemClick(Landroid/widget/AdapterView;Landroid/view/View;IJ)V'){
                        methodVisitor.visitVarInsn(ALOAD,1)
                        methodVisitor.visitVarInsn(ALOAD,2)
                        methodVisitor.visitVarInsn(ILOAD,3)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS,"trackViewOnClick","(Landroid/widget/AdapterView;Landroid/view/View;I)V",false)
                    }else if(mInterfaces.contains('android/widget/ExpandableListView$OnGroupClickListener') && nameDesc == 'onGroupClick(Landroid/widget/ExpandableListView;Landroid/view/View;IJ)Z'){
                        methodVisitor.visitVarInsn(ALOAD,1)
                        methodVisitor.visitVarInsn(ALOAD,2)
                        methodVisitor.visitVarInsn(ILOAD,3)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS,"trackExpandableListViewGroupOnClick","(Landroid/widget/ExpandableListView;Landroid/view/View;I)V",false)
                    }else if(mInterfaces.contains('android/widget/ExpandableListView$OnChildClickListener') && nameDesc == 'onChildClick(Landroid/widget/ExpandableListView;Landroid/view/View;IIJ)Z'){
                        methodVisitor.visitVarInsn(ALOAD,1)
                        methodVisitor.visitVarInsn(ALOAD,2)
                        methodVisitor.visitVarInsn(ILOAD,3)
                        methodVisitor.visitVarInsn(ILOAD,4)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS,"trackExpandableListViewChildOnClick","(Landroid/widget/ExpandableListView;Landroid/view/View;II)V",false)
                    }
                }
            }
        }
        return methodVisitor;
    }

    int getVisitPosition(Type[] types, int index, boolean isStaticMethod){
        if(types == null || index < 0 || index >= types.length){
            throw new Error("getVisitPosition error")
        }
        if(index == 0){
            return isStaticMethod ? 0 : 1
        }else{
            return getVisitPosition(types, index -1 ,isStaticMethod) + types[index-1].getSize()
        }
    }
}