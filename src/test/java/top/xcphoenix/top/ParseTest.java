package top.xcphoenix.top;

import lombok.extern.java.Log;
import org.junit.jupiter.api.Test;
import top.xcphoenix.excel2sql.parse.Parser;

import java.util.*;

/**
 * @author xuanc
 * @version 1.0
 * @date 2020/12/9 下午2:01
 */
@Log
public class ParseTest {

    @Test
    void testToSql() {
        Parser parser = new Parser();
        parser.setDbName("cs_linux");
        parser.setTableName("cs_user");
        Map<String, List<String>> fieldMap = new HashMap<>();
        fieldMap.put("id", Arrays.asList("1", "2", "3", "4", "5", "6"));
        fieldMap.put("name", Arrays.asList("xuanc", "cangm", "bming", "huo"));
        parser.getField2Val().putAll(fieldMap);
        log.info(parser.toSql());
    }

}
