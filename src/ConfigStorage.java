import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.rms.*;

public class ConfigStorage {
    private static final String STORE_NAME = "APIConfig";
    private static final String STORE_NAME2 = "ChatConfig";
    
    // 保存配置项
    public static void saveConfig(String key, String value) throws Exception {
        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(STORE_NAME, true);
            int id = findRecordId(rs, key);
            byte[] data = (key + "=" + value).getBytes("UTF-8");
            
            if (id != -1) {
                rs.setRecord(id, data, 0, data.length);
            } else {
                rs.addRecord(data, 0, data.length);
            }
        } finally {
            if (rs != null) rs.closeRecordStore();
        }
    }
    
    // 保存int类型配置
    public static void saveConfig(String key, int value) throws Exception {
        saveConfig(key, String.valueOf(value));
    }
    
    // 读取配置
    public static String getConfig(String key) throws Exception {
        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(STORE_NAME, true);
            int id = findRecordId(rs, key);
            if (id != -1) {
                byte[] data = rs.getRecord(id);
                String record = new String(data, "UTF-8");
                return record.substring(record.indexOf("=") + 1);
            }
            return null;
        } finally {
            if (rs != null) rs.closeRecordStore();
        }
    }
    
    // 查找记录ID
    private static int findRecordId(RecordStore rs, String key) throws RecordStoreException {
        RecordEnumeration re = null;
        try {
            re = rs.enumerateRecords(null, null, false);
            while (re.hasNextElement()) {
                int id = re.nextRecordId();
                byte[] data = rs.getRecord(id);
                String record = new String(data);
                if (record.startsWith(key + "=")) {
                    return id;
                }
            }
            return -1;
        } finally {
            if (re != null) re.destroy();
        }
    }
    
    // 保存所有选项的选中状态
    public static void saveStates(ChoiceGroup checkboxes) {
        try {
            RecordStore rs = RecordStore.openRecordStore(STORE_NAME2, true);
            byte[] data = new byte[checkboxes.size()];
            
            for (int i = 0; i < checkboxes.size(); i++) {
                data[i] = (byte) (checkboxes.isSelected(i) ? 1 : 0);
            }
            
            if (rs.getNumRecords() > 0) {
                rs.setRecord(1, data, 0, data.length);
            } else {
                rs.addRecord(data, 0, data.length);
            }
            
            rs.closeRecordStore();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // 加载并设置选项的选中状态
    public static void loadStates(ChoiceGroup checkboxes) {
        try {
            RecordStore rs = RecordStore.openRecordStore(STORE_NAME2, false);
            
            if (rs.getNumRecords() > 0) {
                byte[] data = rs.getRecord(1);
                for (int i = 0; i < Math.min(data.length, checkboxes.size()); i++) {
                	
                    checkboxes.setSelectedIndex(i, data[i] == 1);
                }
            }
            
            rs.closeRecordStore();
        } catch (RecordStoreNotFoundException e) {
            // 第一次运行，无存储记录
        } catch (Exception e) {
            e.printStackTrace();
        }
    }    
    
    // 读取单选选择的数据
    public static int loadcheckboxes(ChoiceGroup checkboxes) {
        try {
            RecordStore rs = RecordStore.openRecordStore(STORE_NAME2, false);
            
            if (rs.getNumRecords() > 0) {
                byte[] data = rs.getRecord(1);
                rs.closeRecordStore();
                return data[0];
            }
            
            rs.closeRecordStore();
        } catch (RecordStoreNotFoundException e) {
            // 第一次运行，无存储记录
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1; // 默认返回  
}
}