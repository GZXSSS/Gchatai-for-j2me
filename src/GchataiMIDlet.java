import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

public class GchataiMIDlet extends MIDlet implements CommandListener {
    private Display display;
    private Form mainForm;
    private TextField inputField, apiKeyField, urlField, modelField, tokensField,systemField;
    private Command sendCmd, exitCmd, setCmd, aboutCmd, backCmd, saveCmd,chatsetCmd;
    private ChoiceGroup streamchoice;
    
    public GchataiMIDlet() {
        display = Display.getDisplay(this);
        
        // 主表单
        mainForm = new Form("Gchatai");
        inputField = new TextField("输入文字: ", "", 6666, TextField.ANY);
        mainForm.append("提示:必须先去设置中设置自己的API接口否则无法使用!程序运行比较缓慢发送一次后请勿二次点击！");
        
        sendCmd = new Command("发送", Command.OK, 1);    
        setCmd = new Command("设置", Command.SCREEN, 1);
        chatsetCmd = new Command("对话设置", Command.SCREEN, 1);
        aboutCmd = new Command("关于", Command.SCREEN, 1);
        
        
        backCmd = new Command("返回", Command.BACK, 2);
        exitCmd = new Command("退出", Command.EXIT, 2);
        
        mainForm.append(inputField);
        mainForm.addCommand(sendCmd);
        mainForm.addCommand(setCmd);
        mainForm.addCommand(chatsetCmd);
        mainForm.addCommand(aboutCmd);
        
        mainForm.addCommand(exitCmd);
        mainForm.setCommandListener(this);
        
    }
    
    public void startApp() {
        display.setCurrent(mainForm);
    }
    
    public void pauseApp() {}
    public void destroyApp(boolean unconditional) {}
    
    public void commandAction(Command cmd, Displayable d) {
    	
        if (cmd == sendCmd) {
            String userInput = inputField.getString();
            if (userInput != null && userInput.length() > 0) {
                inputField.setString(""); // 清空输入框
                new Thread(new ApiCaller(userInput)).start();
            }
        } else if (cmd == setCmd) {
        	SettingsForm();
        }else if (cmd == aboutCmd) {
        	AboutForm();
        }else if (cmd == backCmd) {
        	display.setCurrent(mainForm);
        }else if (cmd == chatsetCmd) {
			ChatSettingsForm();
		}else if (cmd == exitCmd) {
            destroyApp(false);
            notifyDestroyed();	
        }
    }

    
	private void AboutForm() {
		// TODO 自动生成的方法存根
        Form aboutForm = new Form("关于");
        aboutForm.append("软件作者:GZX");
        aboutForm.append("\n"); // 换行
        aboutForm.append("电子邮件:2553106981@qq.com");
        aboutForm.append("\n"); // 换行
        aboutForm.append("GitHub:@GZXSSS");
        aboutForm.append("\n"); // 换行
        aboutForm.append("软件版本:V2.0");
        aboutForm.append("\n"); // 换行
        aboutForm.append("更新内容");
        aboutForm.append("\n"); // 换行
        aboutForm.append("======	");
        aboutForm.append("\n"); // 换行
        aboutForm.append("对话设置");
        aboutForm.append("\n"); // 换行
        aboutForm.append("流式传输");
        aboutForm.append("\n"); // 换行
        aboutForm.append("增加了加载时候的提醒界面");
        aboutForm.append("\n"); // 换行
        aboutForm.append("======");
        aboutForm.addCommand(backCmd);
        aboutForm.setCommandListener(this);
        display.setCurrent(aboutForm);
	}

	private void SettingsForm() {
		// TODO 自动生成的方法存根
	    final Form settingsForm = new Form("API设置");
	    
	    // 创建输入字段
	    apiKeyField = new TextField("API_KEY", "", 666, TextField.ANY);
	    urlField = new TextField("API_URL", "", 666, TextField.URL);
	    modelField = new TextField("Model", "", 666, TextField.ANY);
	    tokensField = new TextField("Max Tokens", "", 666, TextField.NUMERIC);
	    
	    // 尝试加载已保存的值
	    try {
	        String savedApiKey = ConfigStorage.getConfig("API_KEY");
	        if (savedApiKey != null) apiKeyField.setString(savedApiKey);
	        
	        String savedUrl = ConfigStorage.getConfig("API_URL");
	        if (savedUrl != null) urlField.setString(savedUrl);
	        
	        String savedModel = ConfigStorage.getConfig("MODEL");
	        if (savedModel != null) modelField.setString(savedModel);
	        
	        String savedTokens = ConfigStorage.getConfig("MAX_TOKENS");
	        if (savedTokens != null) tokensField.setString(savedTokens);
	    } catch (Exception e) {
	        // 处理加载错误
	        settingsForm.append("加载配置失败: " + e.getMessage());
	    }
	    
	    // 添加到表单
	    settingsForm.append(apiKeyField);
	    settingsForm.append(urlField);
	    settingsForm.append(modelField);
	    settingsForm.append(tokensField);
	    
	    // 添加保存命令
	    saveCmd = new Command("保存", Command.OK, 1);
	    settingsForm.addCommand(saveCmd);
	    settingsForm.addCommand(backCmd);
	    settingsForm.setCommandListener(new CommandListener() {
	        public void commandAction(Command c, Displayable d) {
	            if (c == backCmd) {
	                display.setCurrent(mainForm);
	            } else if (c == saveCmd) {
	                // 保存配置
	                try {
	                    ConfigStorage.saveConfig("API_KEY", apiKeyField.getString());
	                    ConfigStorage.saveConfig("API_URL", urlField.getString());
	                    ConfigStorage.saveConfig("MODEL", modelField.getString());
	                    
	                    // 处理max_tokens为int类型
	                    try {
	                        int maxTokens = Integer.parseInt(tokensField.getString());
	                        ConfigStorage.saveConfig("MAX_TOKENS", maxTokens);
	                    } catch (NumberFormatException e) {
	                        Alert alert = new Alert("错误", "最大TOKEN必须是有效的数字", null, AlertType.ERROR);
	                        display.setCurrent(alert, settingsForm);
	                        return;
	                    }
	                    
	                    Alert alert = new Alert("成功", "配置已保存", null, AlertType.CONFIRMATION);
	                    alert.setTimeout(2000);
	                    display.setCurrent(alert, mainForm);
	                } catch (Exception e) {
	                    Alert alert = new Alert("错误", "保存失败: " + e.getMessage(), null, AlertType.ERROR);
	                    display.setCurrent(alert, settingsForm);
	                }
	            }
	        }
	    });
	    
	    display.setCurrent(settingsForm);
	}
	
	
	private void ChatSettingsForm() {
	    
		// TODO 自动生成的方法存根
	    final Form settingsForm = new Form("对话设置");
	    
	    
	    streamchoice = new ChoiceGroup("是否启用流式传输", Choice.EXCLUSIVE);
		streamchoice.append("启动", null);
		streamchoice.append("关闭", null);
	    // 创建输入字段
	    systemField = new TextField("背景设置", "", 666, TextField.ANY);
	    
	    // 尝试加载已保存的值
	    try {
	        String savedSystem = ConfigStorage.getConfig("SYSTEM");
	        ConfigStorage.loadStates(streamchoice);
	        if (savedSystem != null) systemField.setString(savedSystem);
	    } catch (Exception e) {
	        // 处理加载错误
	        settingsForm.append("加载配置失败: " + e.getMessage());
	    }
	    
	    // 添加到表单
	    settingsForm.append(systemField);
	    settingsForm.append(streamchoice);
	    
	    // 添加保存命令
	    saveCmd = new Command("保存", Command.OK, 1);
	    settingsForm.addCommand(saveCmd);
	    settingsForm.addCommand(backCmd);
	    settingsForm.setCommandListener(new CommandListener() {
	        public void commandAction(Command c, Displayable d) {
	            if (c == backCmd) {
	                display.setCurrent(mainForm);
	            } else if (c == saveCmd) {
	                // 保存配置
	                try {
	                	ConfigStorage.saveStates(streamchoice);
	                    ConfigStorage.saveConfig("SYSTEM", systemField.getString());                
	                    Alert alert = new Alert("成功", "配置已保存", null, AlertType.CONFIRMATION);
	                    alert.setTimeout(2000);
	                    display.setCurrent(alert, mainForm);
	                } catch (Exception e) {
	                    Alert alert = new Alert("错误", "保存失败: " + e.getMessage(), null, AlertType.ERROR);
	                    display.setCurrent(alert, settingsForm);
	                }
	            }
	        }
	    });
	    
	    display.setCurrent(settingsForm);
	}
	
	private void updateChat(final String userMsg, final String aiMsg) {
        display.callSerially(new Runnable() {
            public void run() {
            	TextBox textBox = new TextBox("问答内容", 
            			"AI:"
            			+"\n"+aiMsg+"\n"+"\n"+
            			"你:"
            			+"\n"+userMsg
            			, 9999, TextField.ANY);
            	textBox.addCommand(backCmd);
            	textBox.setCommandListener(new CommandListener() {
					
					public void commandAction(Command c, Displayable d) {
						// TODO 自动生成的方法存根
						if (c == backCmd) {
			                display.setCurrent(mainForm);
			            }
					}
				});
            	display.setCurrent(textBox);
            }
        });
    }


    class ApiCaller implements Runnable {
        private final String userInput;
        
        public ApiCaller(String input) {
            this.userInput = input;
        }

		public void run() {
			// TODO 自动生成的方法存根
					try {
						if (ConfigStorage.loadcheckboxes(streamchoice)==0) {
							Form form = new Form("非流式传输");
							StringItem contentItem = new StringItem("", "");
							contentItem.setText("加载中请等待");
							form.append(contentItem);
							form.addCommand(backCmd);	
							
							display.setCurrent(form);
							
							updateChat(userInput, GchataiFinal.sendMessage(userInput,contentItem,display));	
							
			            	form.setCommandListener(new CommandListener() {							
								public void commandAction(Command c, Displayable d) {
									// TODO 自动生成的方法存根
									if (c == backCmd) {
						                display.setCurrent(mainForm);
						            }
								}
							});
						
			            	
							
							
						}else {
							Form form = new Form("流式传输");							
							StringItem contentItem = new StringItem("", "");
							contentItem.setText("正在连接中，请等待");
							form.append(contentItem);						
							display.setCurrent(form);
			            	final GchataiStream gStream = new GchataiStream(userInput, form,contentItem,display);		            
			            	form.addCommand(backCmd);
			            	new Thread(gStream).start();
			            	form.setCommandListener(new CommandListener() {							
								public void commandAction(Command c, Displayable d) {
									// TODO 自动生成的方法存根
									if (c == backCmd) {
						                display.setCurrent(mainForm);
						                gStream.stop();
						            }
								}
							});
						}
	
						
					} catch (Exception e) {
						// TODO 自动生成的 catch 块
						Alert alert = new Alert("错误", "没有进行配置！或出现未知错误", null, AlertType.ERROR);
	                    display.setCurrent(alert, mainForm);
						e.printStackTrace();
					}
				}
    }
}