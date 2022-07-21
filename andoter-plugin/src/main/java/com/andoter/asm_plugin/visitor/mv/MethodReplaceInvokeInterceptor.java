package com.andoter.asm_plugin.visitor.mv;

import com.andoter.asm_plugin.utils.ADLog;
import com.andoter.asm_plugin.utils.AccessCodeUtils;
import com.andoter.asm_plugin.visitor.PluginConstant;

import org.objectweb.asm.MethodVisitor;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * 方法替换拦截器.
 */
public class MethodReplaceInvokeInterceptor extends AdviceAdapter {

    List<ReplaceMethodEntry> replaceMethodList = new ArrayList<>();

    public MethodReplaceInvokeInterceptor(@Nullable MethodVisitor methodVisitor, int access, @Nullable String name, @Nullable String descriptor) {
        super(PluginConstant.INSTANCE.getASM_VERSION(), methodVisitor, access, name, descriptor);
        replaceMethodList.add(new ReplaceMethodEntry("android/telephony/TelephonyManager",
                "getNetworkType",
                "()I",
                Opcodes.INVOKESTATIC,
                "com/sensorsdata/asm_example/TelephonyManagerProxy",
                "getNetworkType",
                "(Landroid/telephony/TelephonyManager;)I"));
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        for (ReplaceMethodEntry entry : replaceMethodList) {
            if (entry.oldOwner.equals(owner) && entry.oldMethodName.equals(name) && entry.oldMethodDesc.equals(descriptor)) {
                ADLog.INSTANCE.info("Will Replace:" + entry.toString());
                // 注意，最后一个参数是false，会不会太武断呢？
                super.visitMethodInsn(entry.newOpcode, entry.newOwner, entry.newMethodName, entry.newMethodDesc, false);
                return;
            }
        }
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);

    }

//    public void visitMethodInsn(int opcodeAndSource, @Nullable String owner, @Nullable String name, @Nullable String descriptor, boolean isInterface) {
//        if ( (Opcodes.ACC_STATIC & opcodeAndSource) != 0 && Objects.equals(owner, "android/util/Log") && (Objects.equals(name, "d") || name == "i" || name == "e" || name == "w" || name == "v")
//                && Objects.equals(descriptor, "(Ljava/lang/String;Ljava/lang/String;)I")
//        )  {            visitMethodInsn(opcodeAndSource, owner, name, descriptor, isInterface);
//        } else {
//            this.mv.visitMethodInsn(184, "java/lang/System", "nanoTime", "()J", false);
//        }
//    }

    static class ReplaceMethodEntry {
        String oldOwner, oldMethodName, oldMethodDesc, newOwner, newMethodName, newMethodDesc;
        int newOpcode;

        public ReplaceMethodEntry() {
        }

        public ReplaceMethodEntry(String oldOwner, String oldMethodName, String oldMethodDesc, int newOpcode, String newOwner, String newMethodName, String newMethodDesc) {
            this.oldOwner = oldOwner;
            this.oldMethodName = oldMethodName;
            this.oldMethodDesc = oldMethodDesc;
            this.newOwner = newOwner;
            this.newMethodName = newMethodName;
            this.newMethodDesc = newMethodDesc;
            this.newOpcode = newOpcode;
        }

        String code2String() {
            switch (newOpcode) {
                case Opcodes.INVOKESTATIC:
                    return "static";
                default:
                    return "UNKNOWN";
            }
        }

        @Override
        public String toString() {
            return "ReplaceMethodEntry= " +
                    oldOwner + '.' +
                    oldMethodName + ' ' + oldMethodDesc +
                    " ====> '" +

                    code2String() + " " + newOwner + '.' +
                    newMethodName + ' ' + newMethodDesc
                    ;
        }
    }
}