# ExcelToSql

写的一个小工具，将Excel按照指定的规则转换为MySQL的插入语句；

## Usage

```shell
git clone https://github.com/PhoenixXC/ExcelToSql.git
cd ExcelToSql
mvn clean package -Dmaven.test.skip=true
cd target
java -jar [jar包] [excel文件位置] [配置文件位置]
```

### Excel格式

Excel文件中需要指定第一行为列名

### 配置文件

配置文件样例：
```yaml
# 表名
db: cs_linux
# 数据库名
table: cs_user
# 数据库字段配置
entryList:
  # 列名，可为空
  - colName: 姓名
    # 数据库字段名，不能为空
    sqlField: name
  - sqlField: privilege
    # 当表格中不存在对应的列，或列值为空，则使用默认值
    defaultValue: 0
  - colName: 密码
    sqlField: password
    # Apache Jexl 表达式，val 表示表格中列对应的值，需返回字符串，若返回一个纯字符串（不包含SQL关键字）需要手动使用单引号或双引号包裹字符串
    jexl: "`md5('${val}')`"
  - colName: 性别
    sqlField: sex
    jexl: "val eq '男' ? '1' : '0'"
  - colName: 手机
    sqlField: phone
  - colName: 邮箱
    sqlField: mail
  - colName: 博客地址
    sqlField: blog
  - colName: github地址
    sqlField: github
  - colName: 部门
    sqlField: grade
  - colName: 职务
    sqlField: major
```

通过配置文件建立 Excel 列名到 Sql 字段的映射关系，复杂的映射关系
可以使用 Jexl 来处理（比如将表格中的姓名由男/女转换为1/0），表达式中 `val`
为表格中的列值；