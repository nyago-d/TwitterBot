package twitter.entity;

import com.iciql.Iciql.IQColumn;
import com.iciql.Iciql.IQTable;
import java.sql.Timestamp;

@IQTable(name="url")
public class Url {
    
    @IQColumn(length=500)
    public String url;
    
    @IQColumn()
    public Timestamp date_time;
}
