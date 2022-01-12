package com.andoter.asm_plugin.visitor.cv

import com.andoter.asm_plugin.AndExt
import com.andoter.asm_plugin.utils.ADLog
import com.andoter.asm_plugin.utils.AccessCodeUtils
import com.andoter.asm_plugin.visitor.BaseClassInterceptor
import com.andoter.asm_plugin.visitor.mv.DeleteLogInterceptor
import com.andoter.asm_plugin.visitor.mv.MethodReplaceInvokeInterceptor
import com.andoter.asm_plugin.visitor.mv.PrintLogInterceptor
import com.andoter.asm_plugin.visitor.mv.TryCatchInterceptor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor


internal class AndExtensionInterceptor(api: Int, classVisitor: ClassVisitor?, var andExt: AndExt?) : BaseClassInterceptor(api, classVisitor) {

    /**
     * [Andoter]:开始访问【类】，name = com/sensorsdata/asm_example/MainActivity, superName = androidx/appcompat/app/AppCompatActivity, version = 51, access = ACC_PUBLIC ACC_TRANSITIVE
    [Andoter]:开始访问方法： name = <init>, access = ACC_PUBLIC , descriptor = ()V
     */
    override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        ADLog.info("开始访问方法： name = $name, access = ${AccessCodeUtils.accessCode2String(access)}, descriptor = $descriptor")
        var methodVisitor:MethodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions)
        if (andExt!!.printLog) {
            ADLog.error("PrintLogInterceptor")
            methodVisitor = PrintLogInterceptor(className, methodVisitor, access, name, descriptor)
        }
        if (andExt!!.deleteLog) {
            ADLog.error("DeleteLogInterceptor")
            methodVisitor = DeleteLogInterceptor(methodVisitor, access, name, descriptor)
        }

        if (andExt!!.tryCatch) {
            if(super.className.equals("com/sensorsdata/asm_example/MainActivity") && name == "onRestoreInstanceState"
                && descriptor == "(Landroid/os/Bundle;)V" ) {
                ADLog.error("Only with MainActivity.onRestoreInstanceState(Landroid/os/Bundle;)V add TryCatchInterceptor")
                methodVisitor = TryCatchInterceptor(methodVisitor, access, name, descriptor)
            }
        }

        ADLog.error("MethodReplaceInvokeInterceptor")
        methodVisitor = MethodReplaceInvokeInterceptor(methodVisitor, access, name, descriptor)


        return methodVisitor
    }
}