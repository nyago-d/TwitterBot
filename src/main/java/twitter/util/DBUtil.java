package twitter.util;

import com.iciql.Db;
import com.iciql.Iciql;
import com.iciql.IciqlException;
import com.iciql.util.StringUtils;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import twitter.annotation.OptionInMerge;
import twitter.config.Config;

/**
 *
 */
public class DBUtil {
    
    /**
     * DB接続を取得する
     */
    public static Db open(Config conf) {
        return Db.open(conf.getDbUrl(), conf.getDbUser(), conf.getDbPassword());
    }
    
    /**
     * マージ文を実行する
     * 
     * @param query     クエリ
     * @param db        DB
     * @param entity    エンティティ
     */
    public static void merge(String query, Db db, Object entity) {
        
        try {
        
            List<Object> values = new ArrayList<>();  
            for (Field field : entity.getClass().getDeclaredFields()) {
                
                Iciql.IQColumn anno = field.getAnnotation(Iciql.IQColumn.class);
                if (anno == null) {
                    continue;
                }
                
                values.add(field.get(entity));
            }

            int cnt = db.executeUpdate(query, values.toArray());
            
        } catch (IllegalAccessException e) {
            throw new IciqlException(e);
        }
    }
    
    /**
     * マージ用のクエリを作成する
     * 
     * @param db        DB
     * @param entity    エンティティ
     * @return クエリ
     */ 
    public static String getMergeQuery(Db db, Object entity) {
        
        try {
        
            String tableName = entity.getClass().getAnnotation(Iciql.IQTable.class).name();
            tableName = db.getDialect().prepareTableName(db.getConnection().getSchema(), tableName);

            StringBuilder sb = new StringBuilder();
            sb.append("insert into ").append(tableName).append(" (");

            List<String> columnNames = new ArrayList<>();
            List<String> updates = new ArrayList<>();
            List<String> binds = new ArrayList<>();     

            for (Field field : entity.getClass().getDeclaredFields()) {

                Iciql.IQColumn anno = field.getAnnotation(Iciql.IQColumn.class);
                if (anno == null) {
                    continue;
                }

                // アノテーションで名前が定義されてい場合はその名前、なければフィールド名
                String columnName = StringUtils.isNullOrEmpty(anno.name()) ? field.getName() : anno.name();
                columnNames.add(columnName);
                
                // 値
                binds.add("?");
                
                // 更新処理
                OptionInMerge opt = field.getAnnotation(OptionInMerge.class);
                if (opt == null) {
                    updates.add(columnName + "=values(" + columnName + ")");
                } else {
                    updates.add(columnName + "=" + opt.value().formula(columnName));
                }
            }
            
            sb.append(String.join(",", columnNames));
            sb.append(") values (");
            sb.append(String.join(",", binds));
            sb.append(") on duplicate key update ");
            sb.append(String.join(",", updates));
            
            return sb.toString();
        
        } catch (SQLException | SecurityException | IllegalArgumentException e) {
            throw new IciqlException(e);
        }
    }
    
}
