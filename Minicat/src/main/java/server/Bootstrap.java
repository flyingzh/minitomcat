package server;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 *  开发Minicat V4.0，在已有Minicat基础上进⼀步扩展，
 *  模拟出webapps部署效果 磁盘上放置⼀个webapps⽬录，
 *  webapps中可以有多个项⽬，⽐如demo1,demo2,demo3...
 *  具体的项⽬⽐如demo1中有serlvet（也即为：servlet是属于具体某⼀个项⽬的servlet），
 *  这样的话在 Minicat初始化配置加载，以及根据请求url查找对应serlvet时都需要进⼀步处理
 */
public class Bootstrap {

    //指定端口号
    private int port;

    private Map<String,HttpServlet> servletMap = new HashMap<String, HttpServlet>();

    private SAXReader saxReader = null;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public SAXReader getSaxReader() {
        return saxReader;
    }

    public void setSaxReader(SAXReader saxReader) {
        this.saxReader = saxReader;
    }

    public static void main(String[] args) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.setSaxReader(new SAXReader());
        bootstrap.start();
    }


    /**
     *  获取线程池
     * @return
     */
    private ThreadPoolExecutor getThreadPoolExecutor(){
        int corePoolSize = 10;
        int maximumPoolSize = 20;
        long keepAliveTime = 100;
        TimeUnit unit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(50);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize,maximumPoolSize,keepAliveTime,unit,workQueue,threadFactory,handler);
        return executor;
    }


    /**
     * 启动监听
     */
    public void start(){
//        loadWebServlet();
        loadServlet();
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true){
                Socket socket = serverSocket.accept();
                RequestProcessor processor = new RequestProcessor(socket,servletMap);
                getThreadPoolExecutor().execute(processor);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     *  解析server.xml
     */
    private void loadServlet() {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("server.xml");
        try {
            Document document = saxReader.read(resourceAsStream);
            Element rootElement = document.getRootElement();
            List<Element> list = rootElement.selectNodes("//Connector");
            //获取端口号
            String port = list.get(0).attributeValue("port");
            setPort(Integer.parseInt(port));

            List<Element> hostList = rootElement.selectNodes("//Host");
            Element hostElement = hostList.get(0);
            //获取域名：localhost
            String baseHostName = hostElement.attributeValue("name");
            //获取项目地址
            String baseAppPath = hostElement.attributeValue("appBase");

            loadWebServlet(baseAppPath);



        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }


    /**
     * 加载解析web.xml，初始化Servlet
     */
    private void loadWebServlet(String path) {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("web.xml");
        SAXReader saxReader = new SAXReader();

        try {
            Document document = saxReader.read(resourceAsStream);
            Element rootElement = document.getRootElement();
            List<Element> selectNodes = rootElement.selectNodes("//servlet");
            for (int i = 0; i < selectNodes.size(); i++) {
                Element element =  selectNodes.get(i);
                processElement(element,rootElement,path);
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化servlet到servletMap
     * @param element
     * @param rootElement
     */
    private void processElement(Element element,Element rootElement,String path) {
        try {
            Element servletNameElement = (Element) element.selectSingleNode("servlet-name");
            String servletName = servletNameElement.getStringValue();

            String basePath = path+ File.separator+servletName+File.separator+"server";
            File baseFile = new File(basePath);
            File file = baseFile.listFiles()[0];
            String name = file.getName();
            String absolutePath  = file.getAbsolutePath();

            MyClassLoader classLoader = new MyClassLoader(absolutePath);
            Class<?> myClass = classLoader.getMyClass();
            HttpServlet instance = (HttpServlet)myClass.newInstance();

            Element servletClassElement = (Element) element.selectSingleNode("servlet-class");
            String servletClass = servletClassElement.getStringValue();
            // 根据servlet-name的值找到url-pattern
            Element servletMapping = (Element) rootElement.selectSingleNode("/web-app/servlet-mapping[servlet-name='" + servletName + "']");
            String urlPattern = servletMapping.selectSingleNode("url-pattern").getStringValue();
            servletMap.put(urlPattern, instance);
//            servletMap.put(urlPattern, (HttpServlet) Class.forName(servletClass).newInstance());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
