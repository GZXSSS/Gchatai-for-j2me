import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.StringItem;

import cc.nnproject.json.JSONArray;
import cc.nnproject.json.JSONObject;

public class GchataiFinal {
	
    public static String sendMessage(String	 messageString,StringItem stringItem,Display display) throws Exception{
    	
    	String API_KEY = ConfigStorage.getConfig("API_KEY");
    	String url = ConfigStorage.getConfig("API_URL");
    	String model=ConfigStorage.getConfig("MODEL");
    	String SYSTEM = ConfigStorage.getConfig("SYSTEM");   	
    	int max_tokens=Integer.parseInt(ConfigStorage.getConfig("MAX_TOKENS"));   	
    	 	
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", model); // 模型名称

        // 构造 messages 数组
        JSONArray messages = new JSONArray();
        JSONObject usermessage = new JSONObject();
        JSONObject systemmessage = new JSONObject();
        usermessage.put("role", "user");
        usermessage.put("content", messageString);
        messages.add(usermessage);
        systemmessage.put("role", "system");
        systemmessage.put("content", SYSTEM);
        messages.add(systemmessage);

        requestBody.put("messages", messages); // 设置 messages
        requestBody.put("temperature", 0.7); // 温度参数
        requestBody.put("max_tokens", max_tokens); // 最大 token 数

        // 发送 POST 请求
        String response = null;  // 用于存储响应结果
        HttpConnection conn = null;
        InputStream is = null;
        OutputStream os = null;

        try {
            // 1. 创建HTTP连接
            conn = (HttpConnection)Connector.open(url);
            
            // 2. 设置请求方法为POST
            conn.setRequestMethod(HttpConnection.POST);
            
            // 3. 设置请求头
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);  // 认证头
            conn.setRequestProperty("Content-Type", "application/json");   // 内容类型
            
            // 4. 写入请求体
            os = conn.openOutputStream();
            byte[] requestBytes = requestBody.toString().getBytes("UTF-8"); // 转换为字节数组
            os.write(requestBytes);  // 发送请求数据		
            // 5. 获取响应
            int responseCode = conn.getResponseCode();  // 获取状态码
            if (responseCode == HttpConnection.HTTP_OK) {
            	
                is = conn.openInputStream();
                int length = (int)conn.getLength();
                
                if (length > 0) {
                    // 情况1: 已知长度的响应
                    byte[] data = new byte[length];
                    int bytesRead = 0;
                    while (bytesRead < length) {
                        int count = is.read(data, bytesRead, length - bytesRead);
                        if (count == -1) {
                            throw new IOException("Unexpected end of stream");
                        }
                        bytesRead += count;
                    }
                    response = new String(data, "UTF-8");
                } else {
                    // 情况2: 分块传输的响应
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    int ch;
                    while ((ch = is.read()) != -1) {
                        bos.write(ch);
                    }
                    response = new String(bos.toByteArray(), "UTF-8");
                }
            } else {
                // HTTP错误处理
                throw new IOException("HTTP错误代码: " + responseCode);
            }
        } catch (Exception e) {
            // 异常处理
        	final String errorMsg = "[错误] " + e.getClass().getName() + ": " + e.getMessage();
        	final StringItem E = stringItem;
            display.callSerially(new Runnable() {
                public void run() {
                    E.setText(E.getText() + errorMsg);
                }
            });
            
        }finally {
            // 6. 清理资源
            if (is != null) try { is.close(); } catch (IOException e) {}
            if (os != null) try { os.close(); } catch (IOException e) {}
            if (conn != null) try { conn.close(); } catch (IOException e) {}
        }

        // 解析响应 JSON
        JSONObject jsonResponse = new JSONObject(response);
        
        String generatedText = jsonResponse.getNullableArray("choices")
                .getNullableObject(0)
                .getNullableObject("message")
                .getString("content");
        
		return generatedText;
    	
    }

}

