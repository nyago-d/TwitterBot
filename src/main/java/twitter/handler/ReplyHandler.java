package twitter.handler;

import com.iciql.Db;
import twitter.config.Config;
import twitter.service.TwitterService;
import twitter.util.DBUtil;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.UserStreamAdapter;

/**
 *
 */
public class ReplyHandler {
    
    public static void main(String[] args) {
        
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.addListener(new MyStatusListener());
        
        twitterStream.user();
    }
    
    private static class MyStatusListener extends UserStreamAdapter {

        /** サービス */
        private final TwitterService service = new TwitterService();
        
        /** 設定 */
        private final Config conf = Config.load();
        
        @Override
        public void onStatus(Status status) {

            // 自身あてのリプライじゃないのは無視
            if (!status.getInReplyToScreenName().equals(conf.getTwitterAccount())) {
                return;
            }
            
            // リツイートは無視
            if (status.isRetweet()) {
                return;
            }
            
            // ついーと
            String tweet = status.getText().replace("@" + conf.getTwitterAccount(), "").trim();
            
            // リプライする
            try (Db db = DBUtil.open(conf)) {
                this.service.reply(conf, db, tweet, status);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        }
    }
}
