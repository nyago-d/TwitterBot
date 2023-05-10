package twitter.entity;

import com.iciql.Iciql.IQColumn;
import com.iciql.Iciql.IQTable;

@IQTable(name="word2vec_data")
public class Word2VecData {
    
    @IQColumn(length=1000)
    public String data;
}
