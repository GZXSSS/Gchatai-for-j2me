import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;

import cc.nnproject.json.JSONArray;
import cc.nnproject.json.JSONObject;

public class GchataiStream implements Runnable {
    private String messageString;
    private StringItem contentItem;
    private Display display;
    private Form form;
    private volatile boolean running;
    
    public GchataiStream(String messageString, Form form, StringItem item, Display display) {
        this.messageString = messageString;
        this.contentItem = item;
        this.display = display;
        this.running = true;
        this.form = form;
    }

    public void stop() {
        running = false;
    }

	public void run() {
        HttpConnection conn = null;
        OutputStream os = null;
        InputStream is = null;
        InputStreamReader isr = null;

        try {
            String API_KEY = ConfigStorage.getConfig("API_KEY");
            String URL = ConfigStorage.getConfig("API_URL");
            String MODEL = ConfigStorage.getConfig("MODEL");
            String SYSTEM_PROMPT = ConfigStorage.getConfig("SYSTEM");
            int MAX_TOKENS = Integer.parseInt(ConfigStorage.getConfig("MAX_TOKENS"));
            
            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", MODEL);
            requestBody.put("stream", true);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", MAX_TOKENS);

            JSONArray messages = new JSONArray();
            
            // 系统消息
            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", SYSTEM_PROMPT);
            messages.add(systemMsg);
            
            // 用户消息
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", messageString);
            messages.add(userMsg);
            
            requestBody.put("messages", messages);

            
            
            // 建立连接
            conn = (HttpConnection)Connector.open(URL);
            conn.setRequestMethod(HttpConnection.POST);
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "text/event-stream");
            conn.setRequestProperty("Cache-Control", "no-cache");

            // 发送请求
            os = conn.openOutputStream();
            byte[] requestBytes = requestBody.toString().getBytes("UTF-8");
            os.write(requestBytes);
            os.flush();

            // 处理响应
            int responseCode = conn.getResponseCode();
            
            
            
            if (responseCode == HttpConnection.HTTP_OK) {
                
                is = conn.openInputStream();
                isr = new InputStreamReader(is, "UTF-8");
                
                StringBuffer buffer = new StringBuffer();
                char[] charBuffer = new char[1024];
                int bytesRead;
                form.setTitle("回答内容");
                contentItem.setText(
            			"你:"
                    	+"\n"+messageString+"\n"+"\n"+
                    	"AI:"
                    	+"\n"
                    	);
                
                while (running && (bytesRead = isr.read(charBuffer)) != -1) {
                    
                    
                    buffer.append(charBuffer, 0, bytesRead);
                    
                    // 处理完整行
                    int newlineIndex;
                    while ((newlineIndex = indexOf(buffer, '\n')) >= 0) {
                        
                        
                        // 提取行
                        String line = substring(buffer, 0, newlineIndex).trim();
                        buffer.delete(0, newlineIndex + 1);
                        
                        if (line.startsWith("data: ")) {
                            
                            
                            String jsonData = line.substring(6).trim();
                            if ("[DONE]".equals(jsonData)) break;

                            try {
                                JSONObject response = new JSONObject(jsonData);
                                JSONArray choices = response.getArray("choices");
                                
                                if (choices != null && choices.size() > 0) {
                                    String content = response.getNullableArray("choices")
                                            .getNullableObject(0)
                                            .getNullableObject("delta")
                                            .getString("content");
                                    if (content != null && content.length() > 0) {
                                        
                                        final String finalContent = content;
                                        display.callSerially(new Runnable() {
                                            public void run() {
                                                // 使用StringItem的setText方法更新内容
                                                contentItem.setText(contentItem.getText() + finalContent);
                                            }
                                        });
                                    }
                                }
                            } catch (Exception e) {
                                // 跳过JSON解析错误
                            }
                        }
                    }
                }
            } else {
                throw new IOException("HTTP Error: " + responseCode);
            }
        } catch (Exception e) {
            final String errorMsg = "[错误] " + e.getClass().getName() + ": " + e.getMessage();
            display.callSerially(new Runnable() {
                public void run() {
                    contentItem.setText(contentItem.getText() + errorMsg);
                }
            });
        } finally {
            // 清理资源
            try {
                if (isr != null) isr.close();
                if (is != null) is.close();
                if (os != null) os.close();
                if (conn != null) conn.close();
            } catch (IOException e) {}
        }
    }
    
    // 辅助方法：在StringBuffer中查找字符
    private int indexOf(StringBuffer sb, char c) {
        for (int i = 0; i < sb.length(); i++) {
            if (sb.charAt(i) == c) {
                return i;
            }
        }
        return -1;
    }
    
    // 辅助方法：从StringBuffer获取子字符串
    private String substring(StringBuffer sb, int start, int end) {
        StringBuffer result = new StringBuffer();
        for (int i = start; i < end && i < sb.length(); i++) {
            result.append(sb.charAt(i));
        }
        return result.toString();
    }
}