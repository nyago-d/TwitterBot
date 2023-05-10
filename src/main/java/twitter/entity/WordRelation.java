package twitter.entity;

import com.iciql.Iciql.IQColumn;
import com.iciql.Iciql.IQTable;
import twitter.annotation.OptionInMerge;
import twitter.annotation.OptionInMerge.Rule;

@IQTable(name="word_relation")
public class WordRelation {
    
    @IQColumn(primaryKey=true, length=50)
    public String word1;

    @IQColumn(primaryKey=true, length=50)
    public String word2;
    
    @OptionInMerge(Rule.ADD)
    @IQColumn()
    public int cnt;
}
