package twitter.service;

import com.iciql.Db;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseTokenizer;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.PartOfSpeechAttribute;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;
import twitter.constants.TalkAnalyzeConst.Status;
import twitter.entity.Talk;
import twitter.entity.TalkAnalyze;

public class TalkAnalyzeService {
    
    private int analyzeCnt = 1000;
    
    public void setAnalyzeCnt(int analyzeCnt) {
        this.analyzeCnt = analyzeCnt;
    }
    
    public void analyzeTweetTalk(Db db) {
        
        // 未処理のもので古い順に指定件数を処理中に更新する
        db.executeUpdate("update ex_talk set status = ? where status = ? order by date_time asc limit ?", Status.Doing.cd(), Status.Init.cd(), analyzeCnt);
        
        // 処理中のデータを取り出す
        Talk table = new Talk();
        List<Talk> talkList = db.from(table).where(table.status).is(Status.Doing.cd()).select();
        
//        Tokenizer tokenizer = Tokenizer.builder().build();
        
        // 各行を解析してエンティティに変換
        List<TalkAnalyze> taList = talkList.stream().flatMap(talk -> {
            List<Pair<String, String>> tokens = this.tokenize(this.fixString(talk.text));
            List<Pair<String, String>> filterTokens = tokens.stream().filter(pair -> !"記号".equals(pair.getTwo())).collect(Collectors.toList());
            return IntStream.range(0, filterTokens.size()).mapToObj(idx -> this.toTalkAnalyze(talk, idx, filterTokens.get(idx)));
        }).collect(Collectors.toList());
        
        // すべて登録
        db.insertAll(taList);
        
        // 終わったら完了にしておく
        db.executeUpdate("update ex_talk set status = ? where status = ?", Status.Done.cd(), Status.Doing.cd());
    }
    
    /**
     * 文を修正する（リプライとURLを除去）
     */
    private String fixString(String str) {
        return str.replaceAll("@[a-zA-Z0-9_]+", "").replaceAll("http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?", "");
    }
    
    /**
     * 解析する
     */
    private List<Pair<String, String>> tokenize(String src) {
        
        List<Pair<String, String>> ret = new ArrayList<>();
        try (JapaneseTokenizer jt = new JapaneseTokenizer(null, false, JapaneseTokenizer.Mode.NORMAL)) {
            
            CharTermAttribute ct = jt.addAttribute(CharTermAttribute.class);        
            PartOfSpeechAttribute partOfSpeech = jt.addAttribute(PartOfSpeechAttribute.class);
            
            jt.setReader(new StringReader(src));
            jt.reset();
            
            while (jt.incrementToken()) {
                ret.add(Tuples.pair(ct.toString(), partOfSpeech.getPartOfSpeech().split("-")[0]));
            }
            
        } catch (IOException e) {
        }
        
        return ret;
    }
    
    /**
     * エンティティに変換
     */
    private TalkAnalyze toTalkAnalyze(Talk talk, int idx, Pair<String, String> pair) {
        
        TalkAnalyze ta = new TalkAnalyze();
        
        ta.id = talk.id;
        ta.idx = idx + 1;
        ta.word = pair.getOne();
        ta.part_of_speech = pair.getTwo();
        
        return ta;
    }
    
    /**
     * Twitterの会話データを登録する
     * 
     * @param db
     * @param stream 
     */
    public void collectTweetTalk(Db db, List<Talk> stream) {
        stream.forEach(t -> db.merge(t));
    }
}
