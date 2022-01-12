package com.andoter.asm_plugin.visitor.mv;

import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_NATIVE;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.RETURN;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
/**
 * 修复 FlutterSplashView 闪退的字节码辅助类.
 * 具体功能:
 * 将原始代码:
 * <pre>
 *       @Override
 *   protected void onRestoreInstanceState(Parcelable state) {
 *     SavedState savedState = (SavedState) state;
 *     super.onRestoreInstanceState(savedState.getSuperState());
 *     previousCompletedSplashIsolate = savedState.previousCompletedSplashIsolate;
 *     splashScreenState = savedState.splashScreenState;
 *   }
 * </pre>
 * 使用字节码改写替换为
 * <pre>
 *   @Override
 *   protected void onRestoreInstanceState(Parcelable state) {
 *        System.out.println("Patched FlutterSplashView onRestoreInstanceState(Parcelable) ");
 *        super.onRestoreInstanceState(state);
 *   }
 * </pre>
 *
 * @author beansoft@126.com
 * @date 2021-12-16
 */
public class MethodReplaceBodyFlutterSplashViewVisitor extends ClassVisitor {
    private String owner;
    private final String methodName;
    private final String methodDesc;

    public MethodReplaceBodyFlutterSplashViewVisitor(int api, ClassVisitor classVisitor, String methodName, String methodDesc) {
        super(api, classVisitor);
        this.methodName = methodName;
        this.methodDesc = methodDesc;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.owner = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (mv != null && methodName.equals(name) && methodDesc.equals(descriptor)) {
            boolean isAbstractMethod = (access & ACC_ABSTRACT) != 0;
            boolean isNativeMethod = (access & ACC_NATIVE) != 0;
            if (!isAbstractMethod && !isNativeMethod) {
                generateNewBody(mv, owner, access, name, descriptor);
                return null;
            }
        }
        return mv;
    }


    protected void generateNewBody(MethodVisitor mv, String owner, int methodAccess, String methodName, String methodDesc) {
        // (1) method argument types and return type
        Type t = Type.getType(methodDesc);
        Type[] argumentTypes = t.getArgumentTypes();
        Type returnType = t.getReturnType();


        // (2) compute the size of local variable and operand stack
        boolean isStaticMethod = ((methodAccess & Opcodes.ACC_STATIC) != 0);
        int localSize = isStaticMethod ? 0 : 1;
        for (Type argType : argumentTypes) {
            localSize += argType.getSize();
        }
        int stackSize = returnType.getSize();


        // (3) method body
//        mv.visitCode();
//        if (returnType.getSort() == Type.VOID) {
//            mv.visitInsn(RETURN);
//        }
//        else if (returnType.getSort() >= Type.BOOLEAN && returnType.getSort() <= Type.DOUBLE) {
//            mv.visitInsn(returnType.getOpcode(ICONST_1));
//            mv.visitInsn(returnType.getOpcode(IRETURN));
//        }
//        else {
//            mv.visitInsn(ACONST_NULL);
//            mv.visitInsn(ARETURN);
//        }
//        mv.visitMaxs(stackSize, localSize);
//        mv.visitEnd();

        mv.visitCode();
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn("Patched FlutterSplashView onRestoreInstanceState(Parcelable) ");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESPECIAL, "android/widget/FrameLayout", "onRestoreInstanceState", "(Landroid/os/Parcelable;)V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }
}
