package top.xcphoenix.excel2sql;

import lombok.extern.java.Log;
import top.xcphoenix.excel2sql.parse.Parser;

import java.io.FileNotFoundException;

/**
 * @author xuanc
 * @version 1.0
 * @date 2020/12/7 下午10:43
 */
@Log
public class Main {

    /**
     * arg: [excel path] [config yaml]
     */
    public static void main(String[] args) throws FileNotFoundException {
        if (args.length < 2) {
            System.out.println("program should be two arg at least, eg: execCmd [excel path] [config yaml path]");
            System.exit(-1);
        }
        final String excelPath = args[0];
        final String configPath = args[1];
        Parser parser = new Parser();
        parser.parse(excelPath, configPath);
        String sql = parser.toSql();
        log.info("parse success, sql is \n" + sql);
    }

}
