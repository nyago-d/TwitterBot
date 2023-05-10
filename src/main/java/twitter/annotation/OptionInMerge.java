package twitter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Function;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OptionInMerge {
    
    /**
     * ルール
     * 
     * @return ルール
     */
    public Rule value();
    
    /**
     * ルール
     */
    public static enum Rule {
        
        // 加算（元の値+1）
        INCREMENT(columnName -> "ifnull(" + columnName + ", 1) + 1")
        
        // 減算（元の値-1）
        , DECREMENT(columnName -> "ifnull(" + columnName + ", 1) - 1")
        
        // 減算（元の値+新しい値）
        , ADD(columnName -> "ifnull(" + columnName + ", 1) + values(" + columnName + ")")
        
        // 減算（元の値-新しい値）
        , REMOVE(columnName -> "ifnull(" + columnName + ", 1) - values(" + columnName + ")")
        
        // 最大（元の値と新しい値で大きい方）
        , MAX(columnName -> "greatest(values(" + columnName + ")," + columnName + ")")
        
        // 最小（元の値と新しい値で小さい方）
        , MIN(columnName -> "least(values(" + columnName + ")," + columnName + ")")
        ;
        
        /** 関数 */
        private final Function<String, String> func;
        
        /**
         * コンストラクタ
         * 
         * @param func 関数
         */
        private Rule(Function<String, String> func) {
            this.func = func;
        }
        
        /**
         * 式
         * 
         * @param columnName カラム名
         * @return 式
         */
        public String formula(String columnName) {
            return func.apply(columnName);
        }
    }
}
