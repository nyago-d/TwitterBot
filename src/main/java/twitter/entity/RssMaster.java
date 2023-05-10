package twitter.entity;

import com.iciql.Iciql.IQColumn;
import com.iciql.Iciql.IQTable;

@IQTable(name="rss_master")
public class RssMaster {
    
    @IQColumn(primaryKey=true)
    public int id;
    
    @IQColumn(length = 100)
    public String description;
    
    @IQColumn(length = 100)
    public String url;
    
    @IQColumn(length = 50)
    public String selector;
    
    @IQColumn()
    public int active_flg;
}