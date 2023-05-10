package twitter.entity;

import com.iciql.Iciql.IQColumn;
import com.iciql.Iciql.IQTable;

@IQTable(name="bracket_master")
public class BracketMaster {
    
    @IQColumn(primaryKey=true)
    public int id;
    
    @IQColumn()
    public String open;
    
    @IQColumn()
    public String close;
    
    @IQColumn()
    public int active_flg;
}
