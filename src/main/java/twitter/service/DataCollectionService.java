package twitter.service;

import com.iciql.Db;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import twitter.config.Config;
import twitter.entity.RssMaster;
import twitter.entity.Url;
import twitter.entity.Word2VecData;
import twitter.entity.WordChain;
import twitter.entity.WordRelation;
import twitter.util.DBUtil;

/**
 *
 */
public class DataCollectionService {
    
    /** パーサ */
    protected final SentenceParser sp;
    
    /**
     * コンストラクタ
     * 
     * @param conf  設定
     */
    public DataCollectionService(Config conf) {
        this.sp = new SentenceParser(conf.getSentenceEnd());
    }
    
    /**
     * データを収集する
     *
     * @param db    DB
     */
    public void collect(Db db) {
        
        // センテンスを収集
        this.loadSentences(db);
        
        // DBに分析結果を登録する
        this.saveWordChain(db);
        
//        // DBに名詞の関連性を登録する
//        this.saveWordRelation(db);
        
        // DBにWord2Vec用のデータを登録する
        this.saveWord2VecData(db);
    }
    
    /**
     * センテンスを収集する
     * 
     * @param db    DB
     * @param sp    パーサ
     */
    private void loadSentences(Db db) {
        
        // 参照済みのURLはスキップした上で、各記事の本文を形態素分析する
        this.loadRssMaster(db).stream().flatMap(m -> this.getFeed(m))
                                       .filter(s -> this.saveURL(s, db))
                                       .forEach(s -> sp.addSentence(this.getDetail(s)));
    }
    
    /**
     * 収集するRSSのデータを取得する
     */
    private List<RssMaster> loadRssMaster(Db db) {
        RssMaster rm = new RssMaster();
        return db.from(rm).where(rm.active_flg).is(1).select();
    }
    
    /**
     * RSSフィードを取得する
     */
    private Stream<Source> getFeed(RssMaster master) {
        SyndFeedInput input = new SyndFeedInput();
        try {
            XmlReader xr = new XmlReader(new URL(master.url));
            BufferedReader br = new BufferedReader(xr);
            SyndFeed feed = input.build(xr);
            
            return feed.getEntries().stream().map(e -> Source.of(SyndEntry.class.cast(e).getLink(), master.selector));
        } catch (IllegalArgumentException | FeedException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * 読み込んだ記事のURLを保存する
     *
     * <pre>
     * 既に読み込んだ記事は解析しない。
     * </pre>
     */
    private boolean saveURL(Source source, Db db) {
        Url table = new Url();
        if (db.from(table).where(table.url).is(source.url).selectCount() > 0) {
            return false;
        } else {
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            table.url = source.url;
            table.date_time = now;
            db.insert(table);
            return true;
        }
    }
    
    /**
     * 対象ページから文章を取得する
     */
    private String getDetail(Source source) {
        
        try {
            Document document = Jsoup.connect(source.url).get();
            Elements ele = document.select(source.selector);
            return ele.stream().map(e -> e.text()).collect(Collectors.joining());
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * 解析したマルコフ連鎖のパーツをDBに登録する
     */
    protected void saveWordChain(Db db) {
        
        List<WordChain> wordChainList = sp.getWordChainList();
        
        // 処理対象なしは何もしない
        if (wordChainList.isEmpty()) {
            return;
        }
        
        // クエリ取得
        String query = DBUtil.getMergeQuery(db, wordChainList.get(0));
        System.out.println(query);
        
        // マージ
        wordChainList.stream().forEach(e -> DBUtil.merge(query, db, e));
        System.out.println(wordChainList.size() + "件の登録・更新");
    }
    
//    /**
//     * 解析した名詞の関係性をDBに登録する
//     */
//    protected void saveWordRelation(Db db) {
//        
//        List<WordRelation> wordRelationList = sp.getWordRelationList();
//        
//        // 処理対象なしは何もしない
//        if (wordRelationList.isEmpty()) {
//            return;
//        }
//        
//        // クエリ取得
//        String query = DBUtil.getMergeQuery(db, wordRelationList.get(0));
//        System.out.println(query);
//        
//        // マージ
//        wordRelationList.stream().forEach(e -> DBUtil.merge(query, db, e));
//        System.out.println(wordRelationList.size() + "件の登録・更新（名詞の関係性）");
//    }
    
    /**
     * Word2Vec用データをDBに登録する
     */
    protected void saveWord2VecData(Db db) {
        List<Word2VecData> word2VecDataList = sp.getWord2VecDataList();
        word2VecDataList.stream().forEach(d -> db.insert(d));
        System.out.println(word2VecDataList.size() + "件の登録・更新（Word2Vec用）");
    }
    
    private static class Source {
        
        private String url;
        private String selector;
        
        public static Source of(String url, String selector) {
            Source source = new Source();
            source.url = url;
            source.selector = selector;
            return source;
        }
    }
}
