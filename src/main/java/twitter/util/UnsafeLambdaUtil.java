package twitter.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * ラムダ式をUnsafeにラップする
 */
public class UnsafeLambdaUtil {
    
    /**
     * コンストラクタ
     */
    private UnsafeLambdaUtil(){
    }
    
    /**
     * ConsumerをUnsafeにラップする
     * 
     * @param <T>
     * @param target
     * @return 
     */
    public static <T> Consumer<T> wrapc(UnsafeConsumer<T> target){
        return (param -> {
            try {
                target.accept(param);
            } catch (Exception e) {
                throw new ExceptionWrapper(e);
            }
        });
    }
    
    /**
     * SupplierをUnsafeにラップする
     * 
     * @param <T>
     * @param target
     * @return 
     */
    public static <T> Supplier<T> wraps(UnsafeSupplier<T> target){
        return (() -> {
            try {
                return target.get();
            } catch (Exception e) {
                throw new ExceptionWrapper(e);
            }
        });
    }
    
    /**
     * FunctionをUnsafeにラップする
     * 
     * @param <T>
     * @param target
     * @return 
     */
    public static <T, R> Function<T, R> wrapf(UnsafeFunction<T, R> target){
        return (arg -> {
            try {
                return target.apply(arg);
            } catch (Exception e) {
                throw new ExceptionWrapper(e);
            }
        });
    }
    
    /**
     * 例外のラッパー
     */
    public static class ExceptionWrapper extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public ExceptionWrapper(Exception e){
            super(e);
        }
    }
    
    /**
     * UnsafeなConsumer
     */
    @FunctionalInterface
    public static interface UnsafeConsumer<T>{
        public void accept(T t) throws Exception;
    }

    /**
     * UnsafeなSupplier
     */    
    @FunctionalInterface
    public static interface UnsafeSupplier<T>{
        public T get() throws Exception;
    }
    
    /**
     * UnsafeなFunction
     */
    @FunctionalInterface
    public static interface UnsafeFunction<T, R>{
        public R apply(T arg) throws Exception;
    }
}
