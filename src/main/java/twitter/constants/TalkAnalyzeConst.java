package twitter.constants;

public interface TalkAnalyzeConst {
    
    public enum Status {
        
        Init(0)
        , Doing(1)
        , Done(2)
        ;
        
        private final int cd;
        
        private Status(int cd) {
            this.cd = cd;
        }
        
        public int cd() {
            return cd;
        }
    };
}
