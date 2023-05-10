import com.iciql.Db;
import twitter.config.Config;
import twitter.service.DataCollectionService;
import twitter.service.SentenceCreateService;
import twitter.util.DBUtil;

/**
 *
 */
public class Test {
    
//    @org.junit.Test
    public void tweetTest() {
        
        // 設定を読み込む
        Config conf = Config.load();
        
        // DBに接続する
        try (Db db = DBUtil.open(conf)) {
            
            SentenceCreateService service = new SentenceCreateService();
            for (int i = 0; i < 20; i++) {
                String s = service.createSentence(conf, db, null);
                System.out.println(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @org.junit.Test
    public void loadTest() {
        
        // 設定を読み込む
        Config conf = Config.load();
        
        // DBに接続する
        try (Db db = DBUtil.open(conf)) {
            
            // データ収集する
            DataCollectionService collect = new DataCollectionService(conf);
            collect.collect(db);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
