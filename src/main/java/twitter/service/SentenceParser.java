package twitter.service;

import java.io.IOException;
import java.io.StringReader;
import twitter.entity.Word2VecData;
import twitter.entity.WordRelation;
import twitter.entity.WordChain;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import org.atilika.kuromoji.Token;
import org.atilika.kuromoji.Tokenizer;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseTokenizer;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.PartOfSpeechAttribute;
import org.eclipse.collections.api.tuple.Twin;
import org.eclipse.collections.impl.bag.mutable.HashBag;
import org.eclipse.collections.impl.tuple.Tuples;
import twitter.util.StreamHelper;
import twitter.util.UnsafeLambdaUtil.ExceptionWrapper;

/**
 * 文章を解析します
 */
public class SentenceParser {
    
    /**
     * 名詞としての収集から除外するパターン
     */
    private static final Pattern NOUN_IGNORE_PATTERN = Pattern.compile("^[0-9０-９]+$");
    
    /**
     * マルコフ連鎖の階数
     */
    private static final int RANK = 3;
    
    /**
     * 文末となり得る文字
     */
    private final List<String> sentenceEnd;
    
    /**
     * トークンの組のリスト
     */
    private final List<TokenChain> tokenChainList;
    
//    /**
//     * 関係性のバッグ
//     */
//    private final HashBag<Twin<String>> relationBag;
    
    /**
     * Word2Vec用データ
     */
    private final List<String> word2VecData;
    
//    /**
//     * トークナイザ
//     */
//    private final Tokenizer tokenizer;
    
    /**
     * コンストラクタ
     * 
     * @param sentenceEnd 文の終了
     */
    public SentenceParser(List<String> sentenceEnd) {
        this.tokenChainList = new ArrayList<>();
//        this.relationBag = HashBag.newBag();
        this.word2VecData = new ArrayList<>();
//        this.tokenizer = Tokenizer.builder().build();
        this.sentenceEnd = sentenceEnd;
    }
    
    /**
     * リセットする
     */
    public void reset() {
        this.tokenChainList.clear();
//        this.relationBag.clear();
        this.word2VecData.clear();
    }
    
    /**
     * 生成する文章の元となる文を追加する
     *
     * @param sentence 文章
     */
    public void addSentence(String sentence) {
        
        // 空の文は何もしない
        if (sentence == null) {
            return;
        }
        
        // 分を区切る
        String regex = "(" + this.sentenceEnd.stream().map(s -> "(?<=" + s + ")")
                                                      .collect(Collectors.joining("|")) + ")";
        String[] lines = sentence.trim().split(regex);
        
        // すべての文を単語の連結に変換する
        for (String line : lines) {
        
//            // 1行を解析
//            List<Token> tokens = this.tokenizer.tokenize(line);
//
//            // 無効なトークンを除去
//            List<TokenWrapper> tokenList = this.transform(tokens);
            List<Token> tokenList = this.toTokenList(line);
            
            // 文頭と文末にマーク
            if (!tokenList.isEmpty()) {
                tokenList.get(0).isHead = true;
                tokenList.get(tokenList.size() - 1).isTail = true;
            }
            
            // 単語の連結を作成
            for (int i = 0; i + RANK <= tokenList.size(); i++) {

                // マルコフ連鎖の階数分追加
//                List<TokenWrapper> chain = new ArrayList<>();
                List<Token> chain = new ArrayList<>();
                for (int j = 0; j < RANK; j++) {
                    chain.add(tokenList.get(i + j));
                }

                // 単語の連結として保持
                TokenChain tc = new TokenChain(chain);
                this.tokenChainList.add(tc);
            }
            
            // 名詞だけを抜き出して重複を排除
//            List<String> nounList = tokenList.stream().filter(t -> "名詞".equals(t.token.getPartOfSpeech().split(",")[0]))
//                                                      .map(t -> t.token.getSurfaceForm())
//                                                      .filter(w -> !NOUN_IGNORE_PATTERN.matcher(w).matches())
//                                                      .distinct()
//                                                      .collect(Collectors.toList());
//            List<String> nounList = tokenList.stream().filter(t -> "名詞".equals(t.partOfSpeech))
//                                                      .map(t -> t.text)
//                                                      .filter(w -> !NOUN_IGNORE_PATTERN.matcher(w).matches())
//                                                      .distinct()
//                                                      .collect(Collectors.toList());
            
//            // 単語組をペアにしてバッグに詰める
//            nounList.stream().forEach((noun1) -> {
//                nounList.stream().filter((noun2) -> !(noun1.equals(noun2)))
//                                 .forEach((noun2) -> this.relationBag.add(Tuples.twin(noun1, noun2)));
//            });
            
            // Word2Vec用データを保持
//            String data = tokenList.stream().map(t -> t.token.getSurfaceForm()).collect(Collectors.joining(" "));
            String data = tokenList.stream().map(t -> t.text).collect(Collectors.joining(" "));
            if (data.length() < 100) {
                this.word2VecData.add(data);
            }
        }
    }
    
//    /**
//     * トークンを変換する
//     */
//    private List<TokenWrapper> transform(List<Token> tokens) {
//        return tokens.stream().map(TokenWrapper::new)
//                              .filter(TokenWrapper::isActive)
//                              .collect(Collectors.toList());
//    }
    
    /**
     * 解析した単語の連結を取得する
     * 
     * @return 解析結果
     */
    public List<WordChain> getWordChainList() {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        return this.tokenChainList.stream().map(t -> {
            WordChain wc = new WordChain();
//            wc.word1 = t.tokens.get(0).token.getSurfaceForm();
//            wc.word2 = t.tokens.get(1).token.getSurfaceForm();
//            wc.word3 = t.tokens.get(2).token.getSurfaceForm();
            wc.word1 = t.tokens.get(0).text;
            wc.word2 = t.tokens.get(1).text;
            wc.word3 = t.tokens.get(2).text;
            wc.head = t.isHead() ? 1 : 0;
            wc.tail = t.isTail() ? 1 : 0;
            wc.date_time = now;
            wc.cnt = 1;
            return wc;
        }).collect(Collectors.toList());
    }
    
//    /**
//     * 解析した名詞の関係性を取得する
//     * 
//     * @return 解析結果
//     */
//    public List<WordRelation> getWordRelationList() {
//          return this.relationBag.toMapOfItemToCount().entrySet().stream().map(e -> {
//              WordRelation wr = new WordRelation();
//              wr.word1 = e.getKey().getOne();
//              wr.word2 = e.getKey().getTwo();
//              wr.cnt = e.getValue();
//              return wr;
//          }).collect(Collectors.toList());
//    }
    
    /**
     * Word2Vec用データを取得する
     * 
     * @return 解析結果
     */
    public List<Word2VecData> getWord2VecDataList() {
        return this.word2VecData.stream().map(s -> {
            Word2VecData w2vd = new Word2VecData();
            w2vd.data = s;
            return w2vd;
        }).collect(Collectors.toList());
    }
    
    /**
     * 連鎖を文章に変換する
     * 
     * @param list  連鎖
     * @return      文章
     */
    public String toSentence(List<WordChain> list) {
        
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
     * トークンの組
     * 
     * <pre>
     * リストのサイズはマルコフ連鎖の階数と一致する。
     * </pre>
     */
    private class TokenChain {
        
        /** 結合しているトークン */
//        private final List<TokenWrapper> tokens;
        private final List<Token> tokens;
        
        /**
         * コンストラクタ
         * 
         * @param tokens トークン
         */
//        public TokenChain(List<TokenWrapper> tokens) {
        public TokenChain(List<Token> tokens) {
            this.tokens = tokens;
        }
        
        /**
         * このトークンの組が文章の開始と成り得るかを判定する
         */
        public boolean isHead() {
            return this.tokens.get(0).isHead;
        }
        
        /**
         * このトークンの組が文章の終了と成り得るかを判定する
         */
        public boolean isTail() {
            return this.tokens.get(this.tokens.size() - 1).isTail;
        }
        
        /**
         * トークンの組が連結できるかを判定する
         * 
         * @param next  次のトークンの組
         */
        public boolean canConnect(TokenChain next) {
            for (int i = 0; i < tokens.size() - 1; i++) {
//                if (!tokens.get(i + 1).token.getSurfaceForm().equals(next.tokens.get(i).token.getSurfaceForm())) {
                if (!tokens.get(i + 1).text.equals(next.tokens.get(i).text)) {
                    return false;
                }
            }
            return true;
        }
        
        /**
         * 文字列表現
         */       
        @Override
        public String toString() {
//            return this.tokens.stream().map(t -> t.token.getSurfaceForm()).collect(Collectors.joining());
            return this.tokens.stream().map(t -> t.text).collect(Collectors.joining());
        }
    }
    
//    /**
//     * トークンのラッパー
//     */
//    private class TokenWrapper {
//        
//        /** トークン */
//        private final Token token;
//        
//        /** このトークンが文頭 */
//        private boolean isHead = false;
//        
//        /** このトークンが文末 */
//        private boolean isTail = false;
//        
//        /**
//         * コンストラクタ
//         * 
//         * @param token トークン
//         */
//        public TokenWrapper(Token token) {
//            this.token = token;
//        }
//        
//        /**
//         * このトークンが有効であるか判定
//         */
//        public boolean isActive() {
//            return token.getSurfaceForm().length() != 0 && !Arrays.asList(" ", "　").contains(token.getSurfaceForm());
//        }
//        
//        /**
//         * 文字列表現
//         */
//        @Override
//        public String toString() {
//            return this.token.getSurfaceForm();
//        }
//    }
    
    private class Token {
        
        /** テキスト */
        private final String text;
        
        /** 品詞 */
        private final String partOfSpeech;
        
        /** このトークンが文頭 */
        private boolean isHead = false;
        
        /** このトークンが文末 */
        private boolean isTail = false;
        
        /**
         * コンストラクタ
         * 
         * @param text          テキスト
         * @param partOfSpeech  品詞
         */
        public Token(String text, String partOfSpeech) {
            this.text = text;
            this.partOfSpeech = partOfSpeech;
        }
        
        /**
         * このトークンが有効であるか判定
         */
        public boolean isActive() {
            return text.length() != 0 && !Arrays.asList(" ", "　", "\n").contains(text);
        }
        
        /**
         * 文字列表現
         */
        @Override
        public String toString() {
            return text;
        }
        
    }
    
    /**
     * 文をトークンのリストに変換する
     * 
     * @param src
     * @return 
     */
    private List<Token> toTokenList(String src) {

        try (JapaneseTokenizer jt = new JapaneseTokenizer(null, false, JapaneseTokenizer.Mode.NORMAL)) {

            jt.setReader(new StringReader(src));
            jt.reset();

            return StreamHelper.whileStream(jt, StreamHelper.throwingPredicate(j -> j.incrementToken()), this::toToken)
                               .filter(Token::isActive)
                               .collect(Collectors.toList());
            
        } catch (IOException | ExceptionWrapper e) {
            return Collections.EMPTY_LIST;
        }
    }
    
    /**
     * トークンを取り出す
     * 
     * @param jt
     * @return 
     */
    private Token toToken(JapaneseTokenizer jt) {
        
        CharTermAttribute ct = jt.getAttribute(CharTermAttribute.class);
        PartOfSpeechAttribute pos = jt.getAttribute(PartOfSpeechAttribute.class);
        
        return new Token(ct.toString(), pos.getPartOfSpeech().split("-")[0]);
    }
} 
