package com.andoter.asm_plugin.utils

import com.andoter.asm_plugin.AndExt
import com.andoter.asm_plugin.visitor.cv.AndExtensionInterceptor
import com.andoter.asm_plugin.visitor.mv.MethodReplaceBodyVisitor
import com.android.build.api.transform.*
import com.android.utils.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

internal object TransformHelper {
    // 配置信息
    var andExt: AndExt? = null

    /**
     * 遍历处理 Jar
     */
    fun transformJars(
        jarInput: JarInput,
        outputProvider: TransformOutputProvider,
        isIncremental: Boolean
    ) {
        val jarName = jarInput.name
        val status = jarInput.status
        val destFile = outputProvider.getContentLocation(
            jarName,
            jarInput.contentTypes,
            jarInput.scopes,
            Format.JAR
        )
        ADLog.info("TransformHelper[transformJars], jar = $jarName, status = $status, isIncremental = $isIncremental")
        if (isIncremental) {
            when (status) {
                Status.ADDED -> {
                    handleJarFile(jarInput, destFile)
                }
                Status.CHANGED -> {
                    handleJarFile(jarInput, destFile)
                }
                Status.REMOVED -> {
                    if (destFile.exists()) {
                        destFile.delete()
                    }
                }
                Status.NOTCHANGED -> {

                }
                else -> {
                }
            }
        } else {
            handleJarFile(jarInput, destFile)
        }
    }

    fun transformDirectory(
        directoryInput: DirectoryInput,
        outputProvider: TransformOutputProvider,
        isIncremental: Boolean
    ) {
        val sourceFile = directoryInput.file
        val name = sourceFile.name
        val destDir = outputProvider.getContentLocation(
            name,
            directoryInput.contentTypes,
            directoryInput.scopes,
            Format.DIRECTORY
        )
        ADLog.info("TransformHelper[transformDirectory], name = $name, sourceFile Path = ${sourceFile.absolutePath}, destFile Path = ${destDir.absolutePath}, isIncremental = $isIncremental")
        if (isIncremental) {
            val changeFiles = directoryInput.changedFiles
            for (changeFile in changeFiles) {
                val status = changeFile.value
                val inputFile = changeFile.key
                val destPath =
                    inputFile.absolutePath.replace(sourceFile.absolutePath, destDir.absolutePath)
                val destFile = File(destPath)
                ADLog.info("目录：$destPath，状态：$status")
                when (status) {
                    Status.NOTCHANGED -> {

                    }
                    Status.REMOVED -> {
                        if (destFile.exists()) {
                            destFile.delete()
                        }
                    }
                    Status.CHANGED, Status.ADDED -> {
                        handleDirectory(inputFile, destFile)
                    }
                    else -> {
                    }
                }
            }
        } else {
            // 首先全部拷贝，防止有后续处理异常导致文件的丢失
            FileUtils.copyDirectory(sourceFile, destDir)
            handleDirectory(sourceFile, destDir)
        }
    }

    private fun handleJarFile(jarInput: JarInput, destFile: File) {
        // 空的 jar 包不进行处理
        if (jarInput.file == null || jarInput.file.length() == 0L) {
            ADLog.info("handleJarFile, ${jarInput.file.absolutePath} is null")
            return
        }
        // 构建 JarFile 文件
        val modifyJar = JarFile(jarInput.file, false)
        // 创建目标文件流
        val jarOutputStream = JarOutputStream(FileOutputStream(destFile))
        val enumerations = modifyJar.entries()
        // 遍历 Jar 文件进行处理
        for (jarEntry in enumerations) {
            val inputStream = modifyJar.getInputStream(jarEntry)
            val entryName = jarEntry.name
            if (entryName.startsWith(".DSA") || entryName.endsWith(".SF")) {
                return
            }
            val tempEntry = JarEntry(entryName)
            jarOutputStream.putNextEntry(tempEntry)
            var modifyClassBytes: ByteArray? = null
            val destClassBytes = IOUtils.readBytes(inputStream)
            if (!jarEntry.isDirectory && entryName.endsWith(".class") && !entryName.startsWith("android")) {
                val className = entryName.replace("/", ".").replace(".class", "") // 读取类名, 忽略需要加工的类
                modifyClassBytes = destClassBytes?.let { modifyClass(it, className) }

            }

            if (modifyClassBytes != null) {
                jarOutputStream.write(modifyClassBytes)
            } else {
                jarOutputStream.write(destClassBytes!!)
            }
            jarOutputStream.flush()
            jarOutputStream.closeEntry()
        }
        jarOutputStream.close()
        modifyJar.close()
    }

    private fun handleDirectory(sourceFile: File, destDir: File) {
        val files = sourceFile.listFiles { dir, name ->
            val realFile = File(dir, name)
            if (realFile.isDirectory) {
                true
            } else {
                name!!.endsWith(".class")
            }
        }

        for (file in files!!) {
            try {
                val destFile = File(destDir, file.name)
                if (file.isDirectory) {
                    handleDirectory(file, destFile)
                } else {
                    val fileInputStream = FileInputStream(file)
                    val sourceBytes = IOUtils.readBytes(fileInputStream)
                    var modifyBytes: ByteArray? = null
                    ADLog.info("handleDirectory file = ${file.absolutePath}")

                    var fileName:String = file.absolutePath
                    if (File.separator != "/") {
                        fileName = fileName.replace("\\\\", "/")
                    }

                    fileName = path2ClassName(fileName)

                    if (!file.name.contains("BuildConfig") ) {
                        modifyBytes = modifyClass(sourceBytes!!, fileName)
                    }
                    if (modifyBytes != null) {
                        val destPath = destFile.absolutePath
                        destFile.delete()
                        IOUtils.byte2File(destPath, modifyBytes)
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun  path2ClassName( pathName : String):String {
        return pathName.replace(File.separator, ".").replace(".class", "")
    }

    private fun modifyClass(sourceBytes: ByteArray, className : String): ByteArray? {

        // 忽略不处理的类
        if(className.endsWith("com.sensorsdata.asm_example.TelephonyManagerProxy")) {
            return sourceBytes;
        }

        try {
            val classReader = ClassReader(sourceBytes)
            val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
            var classVisitor:ClassVisitor = AndExtensionInterceptor(Opcodes.ASM8, classWriter, andExt)

            if(className.endsWith("com.sensorsdata.asm_example.MainActivity")) {
                ADLog.error("Only MethodEmptyBodyVisitor on com.sensorsdata.asm_example.MainActivity")
                classVisitor =
                    MethodReplaceBodyVisitor(
                        Opcodes.ASM8, classVisitor, "onRestoreInstanceState",
                        "(Landroid/os/Bundle;)V"
                    )
            }

//            if(super.className.equals("com/sensorsdata/asm_example/MainActivity") && name == "onRestoreInstanceState"
//                && descriptor == "(Landroid/os/Bundle;)V" ) {
//                ADLog.error("Only with MainActivity.onRestoreInstanceState(Landroid/os/Bundle;)V add TryCatchInterceptor")
//                methodVisitor =
//                    MethodEmptyBodyVisitor(super.api, methodVisitor, "verify", "(Ljava/lang/String;Ljava/lang/String;)V")
//            }
//            var classVisitor = MethodReplaceInvokeVisitor(Opcodes.ASM8, classWriter,
//                "java/lang/Math", "max", "(II)I",
//                Opcodes.INVOKESTATIC, "java/lang/Math", "min", "(II)I")
//            classVisitor = MethodReplaceInvokeVisitor(Opcodes.ASM8, classVisitor,
//                "android/telephony/TelephonyManager", "getNetworkType", "()I",
//                Opcodes.INVOKESTATIC, "com/sensorsdata/asm_example/TelephonyManagerProxy", "getNetworkType",
//                "(Landroid/telephony/TelephonyManager;)I")
//            AndExtensionInterceptor(Opcodes.ASM8, classWriter, andExt)
            classReader.accept(classVisitor, ClassReader.SKIP_DEBUG)
            return classWriter.toByteArray()
        } catch (exception: Exception) {
            ADLog.info("modify class exception = ${exception.printStackTrace()}")
        }
        return null
    }
}