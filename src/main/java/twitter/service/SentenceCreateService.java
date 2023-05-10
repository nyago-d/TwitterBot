package twitter.service;

import com.iciql.Db;
import com.iciql.QueryWhere;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import twitter.config.Config;
import twitter.entity.BracketMaster;
import twitter.entity.WordChain;
import twitter.entity.WordRelation;

/**
 *
 */
public class SentenceCreateService {
    
    /**
     * ランダムに文章を生成する
     * 
     * @param conf      設定
     * @param db        DB
     * @param firstWord 最初の語
     * @return      文章
     */
    public String createSentence(Config conf, Db db, String firstWord) {
        
        List<WordChain> list = this.getWordChain(conf, db, firstWord);
        
        if (list.isEmpty()) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < list.size(); i++) {
            WordChain chain = list.get(i);
            if (i == 0) {
                sb.append(chain.word1).append(chain.word2);
            }
            sb.append(chain.word3);
        }
        
        return sb.toString();
    }
    
    /**
     * 適当に文章を作る
     */
    private List<WordChain> getWordChain(Config conf, Db db, String firstWord) {
        
        BracketController bc = new BracketController(db);
        
        List<WordChain> words = new ArrayList<>();
        
        // 最初の単語の組を取得
        WordChain word = this.getFirstWordChain(db, firstWord, bc);
        if (word == null) {
            return words; 
        }
        
        words.add(word);
        bc.pushFirst(word);
        
        // 最大結合長までトライする
        for (int i = 0; i < conf.getMaxChain(); i++) {
            
            // 括弧があれば取り出しておく
            bc.pop();
            
            // 次の単語の組を取得
            word = this.getNextWordChain(db, word, bc);

            // ここで途切れてしまった場合、やり直し
            if (word == null) {
                return this.getWordChain(conf, db, firstWord);
            }
            
            // 括弧が閉じなかったら戻す
            bc.repush(word);

            // 単語の組を追加
            words.add(word);
            bc.push(word);
            
            // 文末まで来た場合
            if (word.tail == 1) {
                
                // 括弧が閉じ終わっていれば終わり
                if (bc.isClose()) {
                    return words;
                }
            }
        }
        
        // 文末にたどり着かなかった場合、やり直し
        return this.getWordChain(conf, db, firstWord);
    }
    
    /**
     * 最初の単語の組を取得する
     */
    private WordChain getFirstWordChain(Db db, String firstWord, BracketController bc) {
        
        WordChain wc = new WordChain();
        
        // クエリ作成（閉じ括弧は除外）
        QueryWhere q = db.from(wc).where(wc.word3).noneOf(bc.getIgnore());
        if (firstWord == null) {
            q = q.and(wc.head).is(1);
        } else {
            q = q.and(wc.word1).is(firstWord);
        }
        
        // 全件数を取得し、偏乱数によって選択するレコードを決定
        long cnt = q.selectCount();
        if (cnt == 0) {
            return null;
        }
        long idx = new Double(cnt * Math.random() * Math.random()).longValue();

        // 日付の新しい、出現回数の多いレコードほどhit率が上がる
        String sql = q.toSQL() + " order by (now() - date_time) / 10000 - cnt limit 1 offset " + idx;
        return db.executeQuery(WordChain.class, sql).get(0);
    }
    
    /**
     * 次の単語の組を取得する
     */
    private WordChain getNextWordChain(Db db, WordChain pre, BracketController bc) {
        
        WordChain wc = new WordChain();
        
        // クエリ作成（今回対象とする閉じ括弧以外は除外）
        QueryWhere q = db.from(wc)
                         .where(wc.word1).is(pre.word2)
                         .and(wc.word2).is(pre.word3)
                         .and(wc.word3).noneOf(bc.getIgnore());
        
        // 全件数を取得し、偏乱数によって選択するレコードを決定
        long cnt = q.selectCount();
        if (cnt == 0) {
            return null;
        }
        long idx = new Double(cnt * Math.random() * Math.random()).longValue();
        
        // 括弧が開いている場合には閉じようとし、日付の新しいレコードほどhit率が上がる
        StringBuilder sql = new StringBuilder();
        sql.append(q.toSQL()).append(" order by ");
        if (bc.currentBracket != null) {
            sql.append("word3 = '").append(bc.currentBracket).append("' desc, ");
        }
        sql.append("(now() - date_time) / 100000 - cnt * cnt limit 1 offset ").append(idx);
        
        return db.executeQuery(WordChain.class, sql.toString()).get(0);
    }
    
    /**
     * 括弧の管理クラス
     */
    private class BracketController {
        
        /** 括弧のマスタ */
        final Map<String, String> master;
        
        /** 状態 */
        final Deque<String> stack = new ArrayDeque<>();
        
        /** 今欲しい閉じ括弧 */
        String currentBracket;
        
        /**
         * コンストラクタ
         */
        BracketController(Db db) {
            BracketMaster bm = new BracketMaster();
            List<BracketMaster> list = db.from(bm).where(bm.active_flg).is(1).select();
            this.master = list.stream().collect(Collectors.toMap(e -> e.open, e -> e.close));
        }
        
        /**
         * 今ほしい閉じ括弧以外を返す
         */
        List<String> getIgnore() {
            List<String> res = new ArrayList<>(this.master.values());
            res.remove(this.currentBracket);
            return res;
        }
        
        /**
         * 追加する
         * 
         * <pre>
         * 括弧があったら足しておく。
         * </pre>
         */
        void push(WordChain word) {
            this.push(word.word3);
        }
        
        /**
         * 追加する
         */ 
        void push(String open) {
            if (this.master.containsKey(open)) {
                this.stack.push(this.master.get(open));
            }
        }
        
        /**
         * 追加する（最初）
         */
        void pushFirst(WordChain word) {
            Arrays.asList(word.word1, word.word2, word.word3).stream().forEach(this::push);
        }
        
        /**
         * 括弧が閉じられなかったらスタックに戻す
         */
        void repush(WordChain word) {
            if (this.currentBracket != null && !word.word3.equals(this.currentBracket)) {
                this.stack.push(this.currentBracket);
            }
        }
        
        /**
         * 括弧を取り出す
         * 
         * <pre>
         * popしたけど閉じなかったら、またpushしよう。
         * </pre>
         */
        void pop() {
            this.currentBracket = this.stack.pollFirst();
        }
        
        /**
         * すべての括弧を閉じられたか
         */
        boolean isClose() {
            return this.stack.isEmpty();
        }
    }
}