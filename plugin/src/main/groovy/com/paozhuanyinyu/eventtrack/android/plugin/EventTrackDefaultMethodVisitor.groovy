package com.paozhuanyinyu.eventtrack.android.plugin

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AdviceAdapter

class EventTrackDefaultMethodVisitor extends AdviceAdapter{
    protected EventTrackDefaultMethodVisitor(int api, MethodVisitor methodVisitor, int access, String name, String descriptor) {
        super(api, methodVisitor, access, name, descriptor)
    }
}