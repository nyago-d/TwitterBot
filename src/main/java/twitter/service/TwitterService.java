package twitter.service;

import com.iciql.Db;
import twitter.config.Config;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

/**
 * Twitter関連のサービス
 */
public class TwitterService {
    
    /** Twitterクライアント */
    private final Twitter twitter = new TwitterFactory().getInstance();
    
    /**
     * 任意にツイートする
     * 
     * @param conf
     * @param db
     * @param word
     * @throws TwitterException 
     */
    public void tweet(Config conf, Db db, String word) throws TwitterException {
        
        // ランダムに文書を作成してtweetする
        twitter.updateStatus(this.getTweet(conf, db, word));
    }
    
    /**
     * リプライする
     * 
     * @param conf
     * @param db
     * @param word
     * @param status
     * @throws TwitterException 
     */
    public void reply(Config conf, Db db, String word, Status status) throws TwitterException {
        
        String userId = status.getUser().getScreenName();
        long statusId = status.getId();
        
        String tweet = this.getTweet(conf, db, word);
        if (tweet == null) {
            return;
        }
        
        StatusUpdate update = new StatusUpdate("@" + userId + " " + tweet);
        
        // ランダムに文書を作成してreplyする
        twitter.updateStatus(update.inReplyToStatusId(statusId));
    }
    
    /**
     * スイートを取得する（最大100件）
     * 
     * @param tweetIds
     * @return
     * @throws TwitterException 
     */
    public ResponseList<Status> lookup(long[] tweetIds) throws TwitterException {
        return twitter.lookup(tweetIds);
    }
    
    /**
     * Tweetを取得する
     * 
     * <pre>
     * 140文字に納まらなかったらもう一回作る。
     * </pre>
     */
    private String getTweet(Config conf, Db db, String word) {
        SentenceCreateService scs = new SentenceCreateService();
        String tweet = scs.createSentence(conf, db, word);
        if (tweet == null) {
            return null;
        } else if (tweet.length() > conf.getMaxLength()) {
            return this.getTweet(conf, db, word);
        } else {
            return tweet;
        }
    }
    
//    private void autoReFollow(Twitter twitter) throws TwitterException {
//        
//        // フォロワーリスト取得
//        List<Long> followersList = getIdList(c -> twitter.getFollowersIDs(c));
//        
//        // フレンドリスト取得
//        List<Long> friendsList = getIdList(c -> twitter.getFriendsIDs(c));
//        
//        // フォローされているが、フォローしていないユーザを新たにフォロー
//        followersList.stream().filter(id -> !friendsList.contains(id))
//                              .map(UnsafeLambdaUtil.wrapf(id -> twitter.createFriendship(id)));
//        
//        // フォローされていないが、フォローしているユーザをリムーブ
//        friendsList.stream().filter(id -> !followersList.contains(id))
//                            .map(UnsafeLambdaUtil.wrapf(id -> twitter.destroyFriendship(id)));
//    }
//    
//    private List<Long> getIdList(UnsafeFunction<Long, IDs, TwitterException> f) throws TwitterException {
//        
//        List<Long> list = new ArrayList<>();
//        long cursor = -1L;
//        
//        while (true) {
//            
//            // フォロワーorフレンドを取得
//            IDs followers = f.apply(cursor);
//            
//            // もういなければ終わり
//            long[] ids = followers.getIDs();
//            if (0 == ids.length) { 
//                break;
//            }
//            
//            // リストに追加
//            for(long id : ids) {
//                list.add(id);
//            }
//            
//            // 
//            cursor = followers.getNextCursor();
//        }
//        
//        return list;
//    }
}
