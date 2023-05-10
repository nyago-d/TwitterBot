package twitter.handler;

import twitter.config.Config;
import com.iciql.Db;
import twitter.service.DataCollectionService;
import twitter.service.TwitterService;
import twitter.util.DBUtil;

public class TwitterHandler {
    
    public static void main(String[] args) throws Exception {
        
        // 設定を読み込む
        Config conf = Config.load();
        
        // DBに接続する
        try (Db db = DBUtil.open(conf)) {
        
            // データ収集する
            DataCollectionService collect = new DataCollectionService(conf);
            collect.collect(db);

            // ランダムに文書を作成してtweetする
            TwitterService twitter = new TwitterService();
            twitter.tweet(conf, db, null);
        }
    }
}
