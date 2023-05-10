package twitter.entity;

import com.iciql.Iciql.IQColumn;
import com.iciql.Iciql.IQTable;
import java.sql.Timestamp;
import twitter.annotation.OptionInMerge;
import twitter.annotation.OptionInMerge.Rule;

@IQTable(name="word_chain")
public class WordChain {
    
    @IQColumn(primaryKey=true, length=50)
    public String word1;

    @IQColumn(primaryKey=true, length=50)
    public String word2;

    @IQColumn(primaryKey=true, length=50)
    public String word3;
    
    @OptionInMerge(Rule.MAX)
    @IQColumn()
    public int head;
    
    @OptionInMerge(Rule.MAX)
    @IQColumn()
    public int tail;
    
    @IQColumn()
    public Timestamp date_time;
    
    @OptionInMerge(Rule.INCREMENT)
    @IQColumn()
    public int cnt;

    @Override
    public String toString() {
        return word1 + word2 + word3;
    }
}
