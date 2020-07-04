
package com.sandyhu.helloSlide;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Properties;

import com.sun.net.httpserver.*;
import org.python.core.*;
import org.python.util.PythonInterpreter;


public class Server {

    public static final int port = 2202;

    public static void main(String[] args) {
        Handler h = new Handler();
        h.invokeNLPRunner("", "");
//        // 监听端口，等待连接
//        try {
//            HttpServer hs = HttpServer.create(new InetSocketAddress(port), 0);
//            hs.createContext("/nlp", new Handler());
//            hs.setExecutor(null);
//            hs.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}

class Handler implements HttpHandler {

    public static final String ORIGIN_FILE_PATH = "/root/HelloSlide/origin/";
    public static final String RESULT_FILE_PATH = "/root/HelloSlide/result/";
    public static final String PYTHON_FILE_PATH_URL = "src/main/python/python.py";

    public void handle(HttpExchange t) throws IOException {
        // 客户端求请求IP
        String hostName = t.getRemoteAddress().getHostName();

        // 解析请求头
        Headers headers = t.getRequestHeaders();
        List<String> list = headers.get("File-Name");
        List<String> list_len = headers.get("Content-Length");
        if(list.size() == 0) return;

        // 得到文件长度（单位为byte）、文件名（带后缀）
        int len = Integer.valueOf(list_len.get(0));
        String fileName = list.get(0);

        /**
         *  源文件和输出文件的URL
         *  源文件格式为 /绝对本地路径/ + /客户端IP地址/ + /_#_/ + /文件名称/
         *  输出文件格式为 /绝对本地路径/ + /客户端IP地址/ + /_#_/ + /文件名称/ + /_#_/ + ".json"
         *  其中/_#_/为分隔符号
         **/
        String inputPath = ORIGIN_FILE_PATH + hostName + "_#_" + fileName;
//        String outputPath = RESULT_FILE_PATH + hostName + "_#_" + fileName + "_#_" + ".json";
        String outputPath = RESULT_FILE_PATH + "default.json";

        // 接收源文件
        receiveFile(t.getRequestBody(), len, inputPath);

        // 调用nlpRunner(Python)
        invokeNLPRunner(inputPath, outputPath);

        // 发送返回数据
        sendResponse(t, outputPath);

    }

    public void receiveFile(InputStream in, int len, String inputPath) throws IOException {
        int originLen = len;
        FileOutputStream out = new FileOutputStream(inputPath);
        while(len != 0) {
            int tmpLen = Math.min(Math.max(256, in.available()), len);
            System.out.println("tmpLen:" + tmpLen);
            byte[] temp = new byte[tmpLen];
            in.read(temp);
            out.write(temp);
            len -= tmpLen;
            System.out.println("len:" + len);
            if(len == originLen) {
                System.out.println("Error");
            }
        }
        in.close();
        out.flush();
        out.close();
    }

    public void invokeNLPRunner(String inputPath, String outputPath) {
        // 系统设置及引入python中文件
        Properties props = new Properties();
        props.put("python.import.site", "false");
        Properties preprops = System.getProperties();
        PythonInterpreter.initialize(preprops, props, new String[0]);
        PythonInterpreter interpreter = new PythonInterpreter();
        String fileUrlPath = PYTHON_FILE_PATH_URL;
        interpreter.execfile(fileUrlPath);

        // 解析python文件中的函数
        PyFunction func = (PyFunction)interpreter.get("nlpRunner",PyFunction.class);

        // 传参并返回
        func.__call__(new PyString(inputPath), new PyString(outputPath));
        interpreter.close();
    }

    public void sendResponse(HttpExchange t, String outputPath) throws IOException {
        FileInputStream in = new FileInputStream(outputPath);
        byte[] response = new byte[in.available()];
        in.read(response);
        in.close();

        t.sendResponseHeaders(200, response.length);
        OutputStream os = t.getResponseBody();
        os.write(response);

        os.flush();
        os.close();
    }
}