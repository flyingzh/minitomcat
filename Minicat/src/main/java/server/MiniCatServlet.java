package server;

import java.io.IOException;

public class MiniCatServlet extends HttpServlet {

    public void init() throws Exception {

    }

    public void destory() throws Exception {

    }

    public void doGet(Request request, Response response) throws Exception {
        String content = "<h1>Demo1Servlet Get</h1>";
        try {
            response.output((HttpProtocolUtil.getHttpHeader200(content.getBytes().length) + content));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doPost(Request request, Response response) throws Exception {
        String content = "<h1>Demo1Servlet Post</h1>";
        try {
            response.output((HttpProtocolUtil.getHttpHeader200(content.getBytes().length) + content));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
