package twitter.handler;

import com.iciql.Db;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import twitter.config.Config;
import twitter.constants.TalkAnalyzeConst;
import twitter.entity.Talk;
import twitter.service.TalkAnalyzeService;
import twitter.service.TwitterService;
import twitter.util.DBUtil;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.UserStreamAdapter;

/**
 *
 */
public class TalkCollectHandler {
    
    public static void main(String[] args) {
        
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.addListener(new MyStatusListener());
        
        twitterStream.sample("ja");
    }
    
    private static class MyStatusListener extends UserStreamAdapter {
        
        /** twitterサービス */
        private final TwitterService service = new TwitterService();
        
        /** データ登録サービス */
        private final TalkAnalyzeService taService = new TalkAnalyzeService();
        
        /** ツイート */
        private List<Status> tweetList = new ArrayList<>();
        
        /** 設定 */
        private final Config conf = Config.load();
        
        /** 収集件数 */
        private int cnt = 0;
        
        @Override
        public void onStatus(Status status) {
            
            // リプライでないのは無視
            if (status.getInReplyToStatusId() < 0) {
                return;
            }
            
            // リツイートは無視
            if (status.isRetweet()) {
                return;
            }
            
            tweetList.add(status);
            cnt++;
            
            // 100件たまったら
            if (cnt == 100) {
                
                // リストを退避
                List<Status> workList = tweetList;
                tweetList = new ArrayList<>();
                cnt = 0;
                
                // リプライ先のツイートIDリストを作る
                long[] ids = workList.stream().mapToLong(s -> s.getInReplyToStatusId()).toArray();
                
                try (Db db = DBUtil.open(conf)) {
                    
                    // 元の発言を取得
                    ResponseList<Status> list = service.lookup(ids);
                    Map<Long, Status> map = list.stream().collect(Collectors.toMap(s -> s.getId(), s -> s));
                    
                    // 登録データに加工
                    Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                    List<Talk> stream = workList.stream().flatMap(s -> {
                        
                        Status targetStatus = map.get(s.getInReplyToStatusId());
                        if (targetStatus == null || targetStatus.getText() == null) {
                            return Stream.empty();
                        }
                        
                        // リプライの発言
                        Talk talk = new Talk();
                        talk.id = String.valueOf(s.getId());
                        talk.reply_to_id = String.valueOf(s.getInReplyToStatusId());
                        talk.text = s.getText().replaceAll("[^\\u0000-\\uFFFF]", "");
                        talk.date_time = now;
                        talk.status = TalkAnalyzeConst.Status.Init.cd();
                        
                        // リプライ先の発言
                        Talk target = new Talk();
                        target.id = String.valueOf(targetStatus.getId());
                        target.reply_to_id = String.valueOf(targetStatus.getInReplyToStatusId());
                        target.text = targetStatus.getText().replaceAll("[^\\u0000-\\uFFFF]", "");
                        target.date_time = now;
                        target.status = TalkAnalyzeConst.Status.Init.cd();
                        
                        return Stream.of(talk, target);
                    }).collect(Collectors.toList());
                    
                    // DBに登録
                    taService.collectTweetTalk(db, stream);
                    
                } catch (TwitterException e) {
                    System.out.println(e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}