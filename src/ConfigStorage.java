import javax.microedition.rms.*;

public class ConfigStorage {
    private static final String STORE_NAME = "APIConfig";
    
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
}