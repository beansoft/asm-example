package com.github.beansoft.android.crashutil;
import java.util.Map;

/**
* 使用 Java 远程代码生成 ThreadDump. 适用于 JDK 1.5+.
* 参考: {@link Thread#getStackTrace()}
* {@link Throwable#getStackTrace()}
* @see StackTraceElement
* @author beansoft@126.com
* 转载请注明出处: beansoft.blogjava.net
*/
public class ThreadDumpBuilder {
    /**
     * 生成并返回 Thread Dump.
     * 转载请注明出处: beansoft.blogjava.net
     * @return
     */
    public static String build() {
        StringBuilder output = new StringBuilder(1000);
        for (Map.Entry stackTrace : Thread.getAllStackTraces().entrySet()) {
            appendThreadStackTrace(output, (Thread) stackTrace.getKey(),
                    (StackTraceElement[]) stackTrace.getValue(), false);
        }
        return output.toString();
    }

    /**
     * 生成并返回 Thread Dump.
     * 转载请注明出处: beansoft.blogjava.net
     * @return
     */
    public static String buildCurrentThreadStackTrace() {
        StringBuilder output = new StringBuilder(1000);
        for (Map.Entry stackTrace : Thread.getAllStackTraces().entrySet()) {
            appendThreadStackTrace(output, (Thread) stackTrace.getKey(),
                    (StackTraceElement[]) stackTrace.getValue(), true);
        }
        return output.toString();
    }

    /**
     * 处理并输出堆栈信息.
     * @param output
     *            输出内容
     * @param thread
     *            线程
     * @param stack
     *            线程堆栈
     */
    private static void appendThreadStackTrace(StringBuilder output, Thread thread,
            StackTraceElement[] stack, boolean onlyCurrentThread) {

        boolean shouldProcess = false;
        // 仅处理当前线程的堆栈信息
        if(onlyCurrentThread) {
            shouldProcess = thread.equals(Thread.currentThread());
        } else {
            shouldProcess = !thread.equals(Thread.currentThread());
        }

        if(shouldProcess) {
            output.append(thread).append("\n");
            if(onlyCurrentThread) {
                // 剪切堆栈
                boolean shouldPrint = false;
                for (StackTraceElement element : stack) {
                    String stackTrace = element.toString();
                    if(stackTrace.contains("com.github.beansoft.android.crashutil.ThreadDumpBuilder.buildCurrentThreadStackTrace")) {
                        shouldPrint = true;
                        continue;
                    }

                    if(shouldPrint) {
                        output.append("\t").append(element).append("\n");
                    }
                }
            } else {
                for (StackTraceElement element : stack) {
                        output.append("\t").append(element).append("\n");
                }
            }

        }
    }

}