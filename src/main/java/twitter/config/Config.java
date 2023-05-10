package twitter.config;

import java.util.List;
import org.yaml.snakeyaml.Yaml;

/**
 * 設定
 */
public class Config {
    
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private String twitterAccount;
    private int maxChain;
    private int maxLength;
    private List<String> sentenceEnd;
    
    public static Config load() {
        Yaml yaml = new Yaml();
        return yaml.loadAs(ClassLoader.getSystemResourceAsStream("conf.yml"), Config.class);
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }
    
    public String getTwitterAccount() {
        return twitterAccount;
    }

    public void setTwitterAccount(String twitterAccount) {
        this.twitterAccount = twitterAccount;
    }

    public int getMaxChain() {
        return maxChain;
    }

    public void setMaxChain(int maxChain) {
        this.maxChain = maxChain;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public List<String> getSentenceEnd() {
        return sentenceEnd;
    }

    public void setSentenceEnd(List<String> sentenceEnd) {
        this.sentenceEnd = sentenceEnd;
    }
}