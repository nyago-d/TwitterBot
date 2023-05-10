package twitter.entity;

import com.iciql.Iciql.IQColumn;
import com.iciql.Iciql.IQTable;

@IQTable(name="ex_talk_analyze")
public class TalkAnalyze {
    
    @IQColumn(length=20, primaryKey=true)
    public String id;
    
    @IQColumn(primaryKey=true)
    public int idx;
    
    @IQColumn(length=200)
    public String word;
    
    @IQColumn(length=100)
    public String part_of_speech;
    
    @Override
    public String toString() {
        return word;
    }
}
