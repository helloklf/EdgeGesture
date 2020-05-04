package shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class RemoteAPI {
    // 从=分割
    private String subPackageName(String recents) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            if (recents != null) {
                String[] rows = recents.split("\n");
                for (String row : rows) {
                    if (row.contains("=") && row.contains("/")) {
                        stringBuilder.append(row.substring(row.indexOf("=") + 1, row.indexOf("/")));
                        stringBuilder.append("\n");
                    }
                }
            }
        } catch (Exception ignored) {
        }
        // System.out.println(stringBuilder.toString());
        return stringBuilder.toString();
    }

    // 从 cmp= 分割
    private String subPackageName2(String recents, String separator) {
        StringBuilder stringBuilder = new StringBuilder();
        int separatorLength = separator.length();
        try {
            if (recents != null) {
                String[] rows = recents.split("\n");
                for (String row : rows) {
                    if (row.contains(separator) && row.contains("/")) {
                        String r = row.substring(row.indexOf(separator) + separatorLength);
                        stringBuilder.append(r, 0, r.indexOf("/"));
                        stringBuilder.append("\n");
                    }
                }
            }
        } catch (Exception ignored) {
        }
        System.out.println(stringBuilder.toString());
        return stringBuilder.toString();
    }

    private void routerMatch(String request, Socket socket) {
        System.out.println("Gesture Router Match：" + request);
        if (request.startsWith("/version")) {
            responseEnd(socket, "0.1.0(1)");
        } else if (request.startsWith("/bar-color")) {
            if (request.matches("^/bar-color\\?[\\d]{1,4}x[\\d]{1,4}$")) {
                try {
                    String[] cols = request.substring(request.indexOf("?") + 1).split("x");
                    GlobalState.displayWidth = Integer.parseInt(cols[0]);
                    GlobalState.displayHeight = Integer.parseInt(cols[1]);
                } catch (Exception ignored) {
                }
            }
            responseEnd(socket, "" + new ScreenColor().autoBarColor());
        } else if (request.startsWith("/nav-light-color")) {
            // dumpsys window visible | grep LIGHT_
            // LIGHT_STATUS_BAR
            // LIGHT_NAVIGATION_BAR
            String result = KeepShellPublic.doCmdSync("dumpsys window visible | grep LIGHT_");
            boolean isLight = result.contains("LIGHT_STATUS_BAR") || result.contains("LIGHT_NAVIGATION_BAR");
            // System.out.println(result + ">" + isLight);
            responseEnd(socket, (isLight ? "true" : "false"));
        } else if (request.startsWith("/recent-9")) {
            // responseEnd(socket, subPackageName(KeepShellPublic.doCmdSync("dumpsys activity r | grep realActivity")));
            responseEnd(socket, subPackageName2(KeepShellPublic.doCmdSync("dumpsys activity r | grep 'cmp='"), "cmp="));
        } else if (request.startsWith("/recent-10")) {
            // responseEnd(socket, subPackageName(KeepShellPublic.doCmdSync("dumpsys activity r | grep mActivityComponent")));
            responseEnd(socket, subPackageName2(KeepShellPublic.doCmdSync("dumpsys activity r | grep baseIntent"), "cmp="));
        } else if (request.startsWith("/fix-delay")) {
            responseEnd(socket, KeepShellPublic.doCmdSync(
                    // "am start -n com.omarea.gesture/com.omarea.gesture.FixAppSwitchDelay --activity-no-animation --activity-no-history --activity-exclude-from-recents --activity-clear-top --activity-clear-task"
                    "am start -n com.omarea.gesture/com.omarea.gesture.FixAppSwitchDelay"
            ));
        } else if (request.startsWith("/shell")) {
            if (request.contains("?")) {
                // KeepShellPublic.doCmdSync(request.substring(request.indexOf("?") + 1));
            }
            responseEnd(socket, "unsupported");
        } else {
            responseEnd(socket, "error");
        }
    }

    public void start() {
        try {
            /*监听端口号，只要是8888就能接收到*/
            ServerSocket ss = new ServerSocket(8906);

            while (true) {
                /*实例化客户端，固定套路，通过服务端接受的对象，生成相应的客户端实例*/
                Socket socket = ss.accept();
                /*获取客户端输入流，就是请求过来的基本信息：请求头，换行符，请求体*/
                BufferedReader bd = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // 接受HTTP请求，并解析数据
                String requestHeader;
                int contentLength = 0;

                while ((requestHeader = bd.readLine()) != null && !requestHeader.isEmpty()) {
                    // System.out.println(requestHeader);

                    // 获得GET参数
                    if (requestHeader.startsWith("GET")) {
                        int begin = requestHeader.indexOf("/?") + 1;
                        int end = requestHeader.indexOf("HTTP/");
                        String condition = requestHeader.substring(begin + 3, end).trim();
                        routerMatch(condition, socket);
                    }

                    /**
                     * 获得POST参数
                     * 1.获取请求内容长度
                     */
                    if (requestHeader.startsWith("Content-Length")) {
                        int begin = requestHeader.indexOf("Content-Lengh:") + "Content-Length:".length();
                        String postParamterLength = requestHeader.substring(begin).trim();
                        contentLength = Integer.parseInt(postParamterLength);
                        System.out.println("POST参数长度是：" + Integer.parseInt(postParamterLength));
                    }
                }

                StringBuffer sb = new StringBuffer();
                if (contentLength > 0) {
                    for (int i = 0; i < contentLength; i++) {
                        sb.append((char) bd.read());
                    }
                    System.out.println("POST参数是：" + sb.toString());
                }

                /*
                // 发送回执
                PrintWriter pw = new PrintWriter(socket.getOutputStream());

                pw.println("HTTP/1.1 200 OK");
                pw.println("Content-type:text/html");
                pw.println();
                pw.println("<h1>successful</h1>");

                pw.flush();
                socket.close();
                */
            }
        } catch (IOException e) {
            System.out.println("Gesture ADB Process RemoteAPI Fail!");
        }
    }


    private void responseEnd(Socket socket, String response) {
        try {
            /*发送回执*/
            PrintWriter pw = new PrintWriter(socket.getOutputStream());

            pw.println("HTTP/1.1 200 OK");
            pw.println("Content-type:text/plain");
            pw.println();
            pw.println(response);

            pw.flush();
            socket.close();
        } catch (Exception ex) {
        }
    }
}
