import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;

import cc.nnproject.json.JSONArray;
import cc.nnproject.json.JSONObject;

public class GchataiFinal {
	
    public static String sendMessage(String	 messageString) throws Exception{
    	
    	String API_KEY = ConfigStorage.getConfig("API_KEY");
    	String url = ConfigStorage.getConfig("API_URL");
    	String model=ConfigStorage.getConfig("MODEL");
    	int max_tokens=Integer.parseInt(ConfigStorage.getConfig("MAX_TOKENS"));   	
    	 	
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", model); // 模型名称

        // 构造 messages 数组
        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", messageString);
        messages.add(message);

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
            e.printStackTrace();
        }finally {
            // 6. 清理资源
            if (is != null) try { is.close(); } catch (IOException e) {}
            if (os != null) try { os.close(); } catch (IOException e) {}
            if (conn != null) try { conn.close(); } catch (IOException e) {}
        }

        // 处理响应
        System.out.println("API 响应: " + response);

        // 解析响应 JSON
        JSONObject jsonResponse = new JSONObject(response);
        
        String generatedText = jsonResponse.getNullableArray("choices")
                .getNullableObject(0)
                .getNullableObject("message")
                .getString("content");
        
		return generatedText;
    	
    }

    /**
     * 处理流式数据接收
     * @param inputStream 从服务器获取的输入流
     * @throws IOException 当流读取出现问题时抛出
     */
    private void processStreamingData(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[256]; // 缓冲区大小可调整
        StringBuffer dataBuffer = new StringBuffer();
        int bytesRead;
        
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            String chunk = new String(buffer, 0, bytesRead, "UTF-8");
            dataBuffer.append(chunk);
            
            // 处理完整消息
            processCompleteMessages(dataBuffer);
        }
        
        // 处理剩余数据
        if (dataBuffer.length() > 0) {
            handleStreamingChunk(dataBuffer.toString().trim());
        }
    }

    /**
     * 从缓冲区提取并处理完整消息(以换行符分隔)
     * @param dataBuffer 包含接收数据的StringBuffer
     */
    private void processCompleteMessages(StringBuffer dataBuffer) {
        int newlinePos = -1;
        
        // 查找换行符位置
        for (int i = 0; i < dataBuffer.length(); i++) {
            if (dataBuffer.charAt(i) == '\n') {
                newlinePos = i;
                break;
            }
        }
        
        while (newlinePos != -1) {
            // 提取完整消息(手动实现substring功能)
            char[] messageChars = new char[newlinePos];
            dataBuffer.getChars(0, newlinePos, messageChars, 0);
            String completeMessage = new String(messageChars).trim();
            
            // 移除已处理的数据
            dataBuffer.delete(0, newlinePos + 1);
            
            // 处理非空消息
            if (completeMessage.length() > 0) {
                handleStreamingChunk(completeMessage);
            }
            
            // 查找下一个换行符
            newlinePos = -1;
            for (int i = 0; i < dataBuffer.length(); i++) {
                if (dataBuffer.charAt(i) == '\n') {
                    newlinePos = i;
                    break;
                }
            }
        }
    }

    /**
     * 处理单个数据块
     * @param chunk 从流中接收到的完整数据块
     */
    private void handleStreamingChunk(String chunk) {
        // 实现你的业务逻辑
        System.out.println("Received chunk: " + chunk);
        
        // 如果是JSON数据可以这样解析:
        // try {
        //     JSONObject json = new JSONObject(chunk);
        //     // 处理json数据...
        // } catch (JSONException e) {
        //     System.err.println("JSON解析错误: " + e.getMessage());
        // }
	}
}
