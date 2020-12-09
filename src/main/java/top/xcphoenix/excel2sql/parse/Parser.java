package top.xcphoenix.excel2sql.parse;

import com.alibaba.excel.EasyExcelFactory;
import lombok.Data;
import lombok.extern.java.Log;
import org.apache.commons.jexl3.*;
import top.xcphoenix.excel2sql.config.CsvMapConfig;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuanc
 * @version 1.0
 * @date 2020/12/8 下午1:10
 */
@Data
@Log
public class Parser {

    private static final String EMPTY_VALUE = "";
    private static final String JEXL_FLAG = "val";
    private CsvMapConfig csvMapConfig;

    private String dbName;
    private String tableName;
    private final Map<String, List<String>> field2Val = new LinkedHashMap<>();

    public void parse(String excelPath, String configPath) throws FileNotFoundException {
        log.info("parse... excelPath: " + excelPath + ", configPath: " + configPath);

        this.csvMapConfig = CsvMapConfig.load(configPath);
        this.dbName = this.csvMapConfig.getDb();
        this.tableName = this.csvMapConfig.getTable();
        buildField2Val(excelPath);
    }

    public String toSql() {
        if (this.tableName == null || this.field2Val.size() == 0) {
            log.warning("this.toString()" + " can not parse valid sql");
            return null;
        }
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("INSERT INTO \n")
                .append(sqlKeywordWrap(dbName)).append(".").append(sqlKeywordWrap(tableName));
        sqlBuilder.append(" (");
        boolean startFieldAppend = true;
        final List<Map.Entry<String, List<String>>> field2ValEntries = new ArrayList<>(field2Val.entrySet());
        for (Map.Entry<String, List<String>> fieldEntry : field2ValEntries) {
            if (startFieldAppend) {
                startFieldAppend = false;
            } else {
                sqlBuilder.append(",");
            }
            sqlBuilder.append(sqlKeywordWrap(fieldEntry.getKey()));
        }
        sqlBuilder.append(") VALUES");

        int maxInsertEntry = field2ValEntries.stream()
                .mapToInt(entry -> Optional.ofNullable(entry.getValue()).map(List::size).orElse(0))
                .max().orElse(0);
        if (maxInsertEntry <= 0) {
            log.warning("no insert entry list");
            return null;
        }

        final Map<String, CsvMapConfig.Entry> field2Config = this.csvMapConfig.getEntryList().stream()
                .collect(Collectors.toMap(CsvMapConfig.Entry::getSqlField, entry -> entry));
        final JexlEngine jexlEngine = new JexlBuilder().create();
        final Map<String, JexlExpression> field2Expr = new HashMap<>();
        for (int i = 0; i < maxInsertEntry; i++) {
            if (i != 0) {
                sqlBuilder.append(",");
            }
            sqlBuilder.append("\n(");
            List<String> entryValues = new ArrayList<>(maxInsertEntry);
            for (Map.Entry<String, List<String>> entry : field2ValEntries) {
                String thisVal;
                if (entry.getValue() == null || entry.getValue().size() <= i) {
                    thisVal = EMPTY_VALUE;
                } else {
                    thisVal = entry.getValue().get(i);
                }

                // custom set value
                CsvMapConfig.Entry config = field2Config.get(entry.getKey());
                if (config != null && config.getJexl() != null && config.getJexl().trim().length() > 0) {
                    String jexl = config.getJexl();
                    log.info("use jexl expr " + jexl + " deal with value defined by config entry: " + config.toString());
                    JexlContext jexlContext = new MapContext();
                    jexlContext.set(JEXL_FLAG, thisVal);
                    JexlExpression expression = field2Expr.computeIfAbsent(
                            config.getSqlField(),
                            s -> jexlEngine.createExpression(config.getJexl())
                    );
                    thisVal = String.valueOf(expression.evaluate(jexlContext));
                    log.info("new value: " + thisVal);
                    // not warp value use ' for sql expr
                    entryValues.add(thisVal);
                    continue;
                }

                entryValues.add(sqlValWrap(thisVal));
            }
            sqlBuilder.append(String.join(",", entryValues));
            sqlBuilder.append(")");
        }

        sqlBuilder.append(";");

        return sqlBuilder.toString();
    }

    private List<Map<Integer, String>> readExcel(String excelPath) throws FileNotFoundException {
        Objects.requireNonNull(excelPath, "excel path can not be null");
        InputStream in = new FileInputStream(excelPath);
        return EasyExcelFactory.read(in).doReadAllSync();
    }

    private void buildField2Val(String excelPath) throws FileNotFoundException {
        // record col to entry
        List<CsvMapConfig.Entry> nonColEntry = csvMapConfig.getEntryList().stream()
                .filter(e -> e.getColName() == null).collect(Collectors.toList());
        Map<String, CsvMapConfig.Entry> col2Entry = csvMapConfig.getEntryList().stream()
                .collect(Collectors.toMap(CsvMapConfig.Entry::getColName, entry -> entry));

        // read excel
        // first col should be colName
        // the map is LinkedHashMap, ordered
        List<Map<Integer, String>> excelEntryList = readExcel(excelPath);
        if (excelEntryList.size() < 1) {
            throw new IllegalArgumentException("excel must have 2 col at least, the first is col name, other are values");
        }

        // build map
        List<Map.Entry<Integer, String>> colIndex2Name = new ArrayList<>(excelEntryList.get(0).entrySet());
        colIndex2Name = colIndex2Name.stream().filter(e -> e != null && e.getValue() != null && e.getValue().length() > 0)
                .collect(Collectors.toList());
        colIndex2Name.forEach(col -> {
            int index = col.getKey();
            CsvMapConfig.Entry cfgEntry = col2Entry.get(col.getValue());
            if (cfgEntry == null) {
                log.warning("col " + col.getValue() + " can not find match sql field, ignore");
                return;
            }
            String sqlField = cfgEntry.getSqlField();
            List<String> fieldValues = new ArrayList<>();
            for (int i = 1; i < excelEntryList.size(); i++) {
                fieldValues.add(
                        excelEntryList.get(i)
                                .getOrDefault(index, Optional.ofNullable(cfgEntry.getDefaultValue()).orElse(EMPTY_VALUE))
                );
            }
            field2Val.put(sqlField, fieldValues);
        });

        nonColEntry.forEach(entry -> {
            List<String> fieldValues = new ArrayList<>();
            String value = Optional.ofNullable(entry.getDefaultValue()).orElse(EMPTY_VALUE);
            for (int i = 0; i < excelEntryList.size() - 1; i++) {
                fieldValues.add(value);
            }
            field2Val.put(entry.getSqlField(), fieldValues);
        });

    }

    private static String sqlValWrap(String val) {
        return "'" + val + "'";
    }

    private static String sqlKeywordWrap(String keyword) {
        return '`' + keyword.trim() + '`';
    }

}
