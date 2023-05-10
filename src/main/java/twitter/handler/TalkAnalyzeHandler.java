package twitter.handler;

import com.iciql.Db;
import twitter.config.Config;
import twitter.service.TalkAnalyzeService;
import twitter.util.DBUtil;

public class TalkAnalyzeHandler {
    
    public static void main(String[] args) throws Exception {
        
        // 設定を読み込む
        Config conf = Config.load();
        
        for (int i = 0; i < 40; i++) {
        
            // DBに接続する
            try (Db db = DBUtil.open(conf)) {
                
                TalkAnalyzeService service = new TalkAnalyzeService();
                
                // 引数で指定があれば、1回で処理する件数を変更する
                if (args.length > 0) {
                    service.setAnalyzeCnt(Integer.parseInt(args[0]));
                }
                
                service.setAnalyzeCnt(5000);
                
                // 解析処理する
                
                service.analyzeTweetTalk(db);
            }
            
            System.gc();
            
        }
    }
}
