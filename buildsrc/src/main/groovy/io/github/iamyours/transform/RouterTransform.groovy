package io.github.iamyours.transform

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

public class RouterTransform extends Transform {
    private static final String DEFAULT_NAME = "RouterTransform"
    Project project

    RouterTransform(Project project) {
        this.project = project
    }

    @Override
    String getName() {
        return DEFAULT_NAME
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    def routeMap = [:]
    static final ROUTE_NAME = "io.github.iamyours:router-api:"

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        def sTime = System.currentTimeMillis()
        def inputs = transformInvocation.inputs
        def routeJarInput
        def outputProvider = transformInvocation.outputProvider
        outputProvider.deleteAll()
        for (TransformInput input : inputs) {
            for (DirectoryInput dirInput : input.directoryInputs) {
                readClassWithPath(dirInput.file)
                File dest = outputProvider.getContentLocation(dirInput.name,
                        dirInput.contentTypes,
                        dirInput.scopes,
                        Format.DIRECTORY)
                FileUtils.copyDirectory(dirInput.file, dest)
            }
            for (JarInput jarInput : input.jarInputs) {
                if (jarInput.name.startsWith(ROUTE_NAME)) {
                    routeJarInput = jarInput
                }
                if (jarInput.scopes.contains(QualifiedContent.Scope.SUB_PROJECTS)) {//module library
                    //从module中获取注解信息
                    readClassWithJar(jarInput)
                }
                copyFile(jarInput, outputProvider)
            }
        }
        def eTime = System.currentTimeMillis()
        println("route map:" + routeMap)
        insertCodeIntoJar(routeJarInput, transformInvocation.outputProvider)

        println("===========route transform finished:" + (eTime - sTime))
    }


    void copyFile(JarInput jarInput, TransformOutputProvider outputProvider) {
        def dest = getDestFile(jarInput, outputProvider)
        FileUtils.copyFile(jarInput.file, dest)
    }

    static File getDestFile(JarInput jarInput, TransformOutputProvider outputProvider) {
        def destName = jarInput.name
        // 重名名输出文件,因为可能同名,会覆盖
        def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath)
        if (destName.endsWith(".jar")) {
            destName = destName.substring(0, destName.length() - 4)
        }
        // 获得输出文件
        File dest = outputProvider.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
        return dest
    }

    /**
     * 插入代码
     * @param jarFile
     */
    void insertCodeIntoJar(JarInput jarInput, TransformOutputProvider out) {
        File jarFile = jarInput.file
        def tmp = new File(jarFile.getParent(), jarFile.name + ".tmp")
        if (tmp.exists()) tmp.delete()
        def file = new JarFile(jarFile)
        def dest = getDestFile(jarInput, out)
        Enumeration enumeration = file.entries()
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(tmp))
        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = enumeration.nextElement()
            String entryName = jarEntry.name
            ZipEntry zipEntry = new ZipEntry(entryName)
            InputStream is = file.getInputStream(jarEntry)
            jos.putNextEntry(zipEntry)
            if (isRouteMapClass(entryName)) {
                jos.write(hackRouteMap(jarFile))
            } else {
                jos.write(IOUtils.toByteArray(is))
            }
            is.close()
            jos.closeEntry()
        }
        jos.close()
        file.close()
        FileUtils.copyFile(tmp, dest)
    }

    private static final String ROUTE_MAP_CLASS_NAME = "io.github.iamyours.router.RouteMap"
    private static
    final String ROUTE_MAP_CLASS_FILE_NAME = ROUTE_MAP_CLASS_NAME.replaceAll("\\.", "/") + ".class"

    private boolean isRouteMapClass(String entryName) {
        return ROUTE_MAP_CLASS_FILE_NAME == entryName
    }

    private byte[] hackRouteMap(File jarFile) {
        ClassPool pool = ClassPool.getDefault()
        pool.insertClassPath(jarFile.absolutePath)
        CtClass ctClass = pool.get(ROUTE_MAP_CLASS_NAME)
        CtMethod method = ctClass.getDeclaredMethod("loadInto")
        StringBuffer code = new StringBuffer("{")
        for (String key : routeMap.keySet()) {
            String value = routeMap[key]
            code.append("\$1.put(\"" + key + "\",\"" + value + "\");")
        }
        code.append("}")
        method.setBody(code.toString())
        byte[] bytes = ctClass.toBytecode()
        ctClass.stopPruning(true)
        ctClass.defrost()
        return bytes
    }

    //从目录中读取class
    void readClassWithPath(File dir) {
        def root = dir.absolutePath
        dir.eachFileRecurse { File file ->
            def filePath = file.absolutePath
            if (!filePath.endsWith(".class")) return
            def className = getClassName(root, filePath)
            if (isSystemClass(className)) return
            addRouteMap(filePath, className)
        }
    }

    //从jar中读取class
    void readClassWithJar(JarInput jarInput) {
        JarFile jarFile = new JarFile(jarInput.file)
        Enumeration<JarEntry> enumeration = jarFile.entries()
        while (enumeration.hasMoreElements()) {
            JarEntry entry = enumeration.nextElement()
            String entryName = entry.getName()
            if (!entryName.endsWith(".class")) continue
            String className = entryName.substring(0, entryName.length() - 6).replaceAll("/", ".")
            InputStream is = jarFile.getInputStream(entry)
            addRouteMap(is, className)
        }
    }
    static final ANNOTATION_DESC = "Lio/github/iamyours/router/annotation/Route;"
    /**
     * 从class中获取Route注解信息
     * @param filePath
     */
    void addRouteMap(String filePath, String className) {
        addRouteMap(new FileInputStream(new File(filePath)), className)
    }

    void addRouteMap(InputStream is, String className) {
        ClassReader reader = new ClassReader(is)
        ClassNode node = new ClassNode()
        reader.accept(node, 1)
        def list = node.invisibleAnnotations
        for (AnnotationNode an : list) {
            if (ANNOTATION_DESC == an.desc) {
                def path = an.values[1]
                routeMap[path] = className
                break
            }
        }
    }

    //默认排除
    static final DEFAULT_EXCLUDE = [
            '^android\\..*',
            '^androidx\\..*',
            '.*\\.R$',
            '.*\\.R\\$.*$',
            '.*\\.BuildConfig$',
    ]

    //获取类名
    String getClassName(String root, String classPath) {
        return classPath.substring(root.length() + 1, classPath.length() - 6)
                .replaceAll("/", ".")       // unix/linux
                .replaceAll("\\\\", ".")    //windows
    }

    boolean isSystemClass(String fileName) {
        for (def exclude : DEFAULT_EXCLUDE) {
            if (fileName.matches(exclude)) return true
        }
        return false
    }


}