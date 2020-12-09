package top.xcphoenix.excel2sql.config;

import lombok.Data;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

/**
 * 列名映射SQL
 *
 * @author xuanc
 * @version 1.0
 * @date 2020/12/8 下午1:11
 */
@Data
public class CsvMapConfig {

    private String configYamlPath;
    private String db;
    private String table;
    private List<Entry> entryList;

    public static CsvMapConfig load(String configYamlPath) throws FileNotFoundException {
        Objects.requireNonNull(configYamlPath, "config yaml path can not be empty");
        Yaml yaml = new Yaml(new Constructor(CsvMapConfig.class));
        InputStream in = new FileInputStream(configYamlPath);
        CsvMapConfig csvMapConfig = yaml.load(in);
        Objects.requireNonNull(csvMapConfig, "yaml load error");
        Objects.requireNonNull(csvMapConfig.getEntryList(), "config entryList can not be empty");
        csvMapConfig.getEntryList().forEach(entry -> {
            Objects.requireNonNull(entry.getSqlField(), "the sql field of entryList item can not be null");
        });
        csvMapConfig.setConfigYamlPath(configYamlPath);
        return csvMapConfig;
    }

    @Data
    public static class Entry {
        private String colName;
        private String sqlField;
        private String defaultValue;
        private String jexl;
    }

}
