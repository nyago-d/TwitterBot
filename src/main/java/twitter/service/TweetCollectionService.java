package twitter.service;

import com.iciql.Db;
import twitter.config.Config;

/**
 * データ収集（twitter）サービス
 */
public class TweetCollectionService extends DataCollectionService {
    
    /**
     * コンストラクタ
     * 
     * @param conf  設定
     * @param db    DB 
     */
    public TweetCollectionService(Config conf) {
        super(conf);
    }
    
    /**
     * センテンスを追加する
     * 
     * @param sentence 
     */
    public void addSentence(String sentence) {
        super.sp.addSentence(sentence);
    }
    
    /**
     * 登録する
     */
    public void save(Db db) {
        
        // DBに分析結果を登録する
        super.saveWordChain(db);
    }
}
