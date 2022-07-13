package com.yujutg.generator;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableFill;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;

/**
 * @author Fishhead
 * @version 1.0
 * @date 2020/9/23 17:25
 */
@SpringBootTest
public class AutoBuildTable {

    @Test
    public void build(){

        AutoGenerator autoGenerator = new AutoGenerator();
        // 配置策略

        // 全局配置
        GlobalConfig globalConfig = new GlobalConfig();
        String property = System.getProperty("user.dir");
        System.out.println(property);
        globalConfig.setOutputDir(property+"\\src\\main\\java");
        globalConfig.setAuthor("Fishhead");
        globalConfig.setOpen(false);
        globalConfig.setFileOverride(false);
        globalConfig.setServiceName("%sService"); // 去掉service的i前缀
        globalConfig.setDateType(DateType.ONLY_DATE);
//        globalConfig.setSwagger2(true);


        // 设置全局配置
        autoGenerator.setGlobalConfig(globalConfig);

        // 设置数据源
        DataSourceConfig config = new DataSourceConfig();
        config.setUrl("jdbc:mysql:///pos_system?useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC");
        config.setDriverName("com.mysql.cj.jdbc.Driver");
        config.setUsername("root");
        config.setPassword("root");
        config.setDbType(DbType.MYSQL);

        autoGenerator.setDataSource(config);

        // 包的配置
        PackageConfig packageConfig = new PackageConfig();
        packageConfig.setModuleName("test");
        packageConfig.setParent("com.yujutg");
        packageConfig.setEntity("entity");
        packageConfig.setMapper("mapper");
        packageConfig.setService("service");
        packageConfig.setController("controller");

        autoGenerator.setPackageInfo(packageConfig);

        // 策略配置
        StrategyConfig strategyConfig = new StrategyConfig();
        strategyConfig.setInclude("address","category", "order_item", "orders", "product", "shopping_cart", "users");
//        strategyConfig.setTablePrefix("tb_");
        // 下划线转驼峰命名法
        strategyConfig.setNaming(NamingStrategy.underline_to_camel);
        strategyConfig.setColumnNaming(NamingStrategy.underline_to_camel);
        strategyConfig.setEntityLombokModel(true);
        // 逻辑删除
        strategyConfig.setLogicDeleteFieldName("is_deleted");

        // 自动填充配置
//        TableFill gmt_create = new TableFill("gmt_create", FieldFill.INSERT);
//        TableFill gmt_update = new TableFill("gmt_update", FieldFill.INSERT_UPDATE);
//        ArrayList<TableFill> arrayList = new ArrayList<>();
//        arrayList.add(gmt_create);
//        arrayList.add(gmt_update);
//        strategyConfig.setTableFillList(arrayList);

        // 乐观锁
//        strategyConfig.setVersionFieldName("version");

        strategyConfig.setRestControllerStyle(true);

        autoGenerator.setStrategy(strategyConfig);

        TemplateConfig templateConfig = new TemplateConfig();
        templateConfig.setXml(null);
        autoGenerator.setTemplate(templateConfig);

        autoGenerator.execute();

    }

}
