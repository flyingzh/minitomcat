package server;

import java.io.*;

/**
 * 自定义类加载器
 */
public class MyClassLoader extends ClassLoader {

    //指定路径
    private String path;

    public MyClassLoader(String classPath) {
        path = classPath;
    }

    public Class<?> getMyClass() {
        File file = new File(path);
        if (!file.exists()) return null;

        FileInputStream in = null;
        ByteArrayOutputStream out;
        Class clazz = null;
        byte[] classData;

        try {
            in = new FileInputStream(file);
            out = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024 * 2];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }

            out.flush();
            classData = out.toByteArray();
            if (classData != null) {
                // 将class的字节码数组转换成Class字节码对象
                clazz = defineClass(null, classData, 0, classData.length);
            }
            return clazz;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

//    /**
//     * 重写findClass方法
//     *
//     * @param name 是我们这个类的全路径
//     * @return 字节码对象
//     */
//    @Override
//    protected Class<?> findClass(String name) {
//        Class clazz = null;
//
//        // 获取该class文件字节码数组
//        byte[] classData = getData();
//
//        if (classData != null) {
//            // 将class的字节码数组转换成Class类的实例
//            clazz = defineClass(name, classData, 0, classData.length);
//        }
//
//        return clazz;
//    }

//    /**
//     * 将class文件转化为字节码数组
//     *
//     * @return 字节数组
//     */
//    private byte[] getData() {
//        File file = new File(path);
//        if (!file.exists()) return null;
//
//        FileInputStream in = null;
//        ByteArrayOutputStream out;
//
//        try {
//            in = new FileInputStream(file);
//            out = new ByteArrayOutputStream();
//
//            byte[] bytes = new byte[1024 * 2];
//            int size;
//            while ((size = in.read(bytes)) != -1) {
//                out.write(bytes, 0, size);
//            }
//
//            out.flush();
//            in.close();
//            return out.toByteArray();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (in != null) in.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return null;
//    }
}