package twitter.entity;

import com.iciql.Iciql.IQColumn;
import com.iciql.Iciql.IQTable;
import java.sql.Timestamp;

@IQTable(name="ex_talk")
public class Talk {
    
    @IQColumn(length=20, primaryKey=true)
    public String id;
    
    @IQColumn(length=20)
    public String reply_to_id;
    
    @IQColumn(length=300)
    public String text;
    
    @IQColumn()
    public Timestamp date_time;
    
    @IQColumn()
    public int status;
    
    @Override
    public String toString() {
        return text;
    }
}
