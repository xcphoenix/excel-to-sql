package top.xcphoenix.top;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.read.metadata.ReadSheet;
import lombok.extern.java.Log;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author xuanc
 * @version 1.0
 * @date 2020/12/9 下午2:34
 */
@Log
public class ExcelTest {

    @Test
    void testReadExcel() throws FileNotFoundException {
        String excelPath = "/tmp/test.xlsx";
        InputStream in = new FileInputStream(excelPath);
        ReadSheet readSheet = new ReadSheet();
        List<Map<Integer, String>> excelEntry = EasyExcelFactory.read(in).doReadAllSync();
        for (Map<Integer, String> items : excelEntry) {
            log.info(String.join("\t", items.values()));
        }
    }

}
