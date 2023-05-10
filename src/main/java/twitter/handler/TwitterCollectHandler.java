package twitter.handler;

import com.iciql.Db;
import java.util.regex.Pattern;
import twitter.config.Config;
import twitter.service.TweetCollectionService;
import twitter.util.DBUtil;
import twitter4j.Status;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.UserStreamAdapter;

/**
 *
 * @author daiki
 */
public class TwitterCollectHandler {
    
    public static void main(String[] args) {
        
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.addListener(new MyStatusListener());
        
        twitterStream.sample("ja");
    }
    
    private static class MyStatusListener extends UserStreamAdapter {
        
        /** 設定 */
        private final Config conf = Config.load();
        
        /** twitterサービス */
        private TweetCollectionService service = new TweetCollectionService(this.conf);
        
        /** 収集件数 */
        private int cnt = 0;
        
        /** 正規表現：URL */
        private static final Pattern URL = Pattern.compile("(?<![\\w])https?://(([\\w]|[^ -~])+(([\\w\\-]|[^ -~])+([\\w]|[^ -~]))?\\.)+(aero|asia|biz|cat|com|coop|edu|gov|info|int|jobs|mil|mobi|museum|name|net|org|pro|tel|travel|xxx|ac|ad|ae|af|ag|ai|al|am|an|ao|aq|ar|as|at|au|aw|ax|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bm|bn|bo|br|bs|bt|bv|bw|by|bz|ca|cc|cd|cf|cg|ch|ci|ck|cl|cm|cn|co|cr|cs|cu|cv|cx|cy|cz|dd|de|dj|dk|dm|do|dz|ec|ee|eg|eh|er|es|et|eu|fi|fj|fk|fm|fo|fr|ga|gb|gd|ge|gf|gg|gh|gi|gl|gm|gn|gp|gq|gr|gs|gt|gu|gw|gy|hk|hm|hn|hr|ht|hu|id|ie|il|im|in|io|iq|ir|is|it|je|jm|jo|jp|ke|kg|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|lu|lv|ly|ma|mc|md|me|mg|mh|mk|ml|mm|mn|mo|mp|mq|mr|ms|mt|mu|mv|mw|mx|my|mz|na|nc|ne|nf|ng|ni|nl|no|np|nr|nu|nz|om|pa|pe|pf|pg|ph|pk|pl|pm|pn|pr|ps|pt|pw|py|qa|re|ro|rs|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sj|sk|sl|sm|sn|so|sr|ss|st|su|sv|sy|sz|tc|td|tf|tg|th|tj|tk|tl|tm|tn|to|tp|tr|tt|tv|tw|tz|ua|ug|uk|us|uy|uz|va|vc|ve|vg|vi|vn|vu|wf|ws|ye|yt|za|zm|zw)(?![\\w])(/([\\w\\.\\-\\$&%/:=#~!]*\\??[\\w\\.\\-\\$&%/:=#~!]*[\\w\\-\\$/#])?)?");
        
        /** 正規表現：メンション */
        private static final Pattern MENTION = Pattern.compile("(?<![\\w])(@|＠)([\\w]{1,15})");
        
        @Override
        public void onStatus(Status status) {
            
            if (status.getId() % 10 != 0) {
                return;
            }
            
            // リプライは無視
            if (status.getInReplyToStatusId() > 0) {
                return;
            }
            
            // リツイートは無視
            if (status.isRetweet()) {
                return;
            }
            
            String text = status.getText();
            
            // URLつきはやだ
            if (URL.matcher(text).find()) {
                return;
            }
            
            // これもリプライ判定？
            if (MENTION.matcher(text).find()) {
                return;
            }
            
            this.service.addSentence(text);
            this.cnt++;
            
            // 1000件たまったら
            if (this.cnt > 100) {
                
                // 退避
                TweetCollectionService workService = this.service;
                this.service = new TweetCollectionService(this.conf);
                this.cnt = 0;
                
                // 登録
                try (Db db = DBUtil.open(this.conf)) {
                    workService.save(db);
                }
            }
        }
    }
}
