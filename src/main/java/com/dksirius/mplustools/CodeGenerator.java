package com.dksirius.mplustools;

import cn.hutool.core.lang.Dict;
import cn.hutool.setting.yaml.YamlUtil;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author gongtao
 * MyBatis 代码框架一键生成器
 */
@Slf4j
public class CodeGenerator {

    /**
     * 配置
     */
    enum Config {
        /**
         * 目标配置文件
         */
        TargetPropertiesFile("application.properties"),
//        TargetPropertiesFile("application-local.yml"),

        /**
         * 父包
         */
        ParentPackage("net.easygo.business.sinotransbooking.provider"),

        /**
         * Entity
         */
        Entity_Package("repository.entity"),

        /**
         * mapper
         */
        Mapper_Package("repository.mapper"),

        /**
         * xml
         */
        Mapper_XML_Package("repository.mapper.xml");

        final String properties;

        Config(String properties) {
            this.properties = properties;
        }

    }

    @Data
    static class JDBC {
        String url;
        String name;
        String password;
    }

    final static class PropertiesValue {

        private static Properties loadProperties(String fileName) {
            try {
                Resource resource = new ClassPathResource(fileName);
                return PropertiesLoaderUtils.loadProperties(resource);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        static JDBC loadPropertiesBaseYaml(String resource) {
            Resource res = new ClassPathResource(resource);
            InputStream inputStream = null;
            JDBC jdbc = new JDBC();
            try {
                inputStream = res.getInputStream();
                Reader reader = new InputStreamReader(inputStream);
                Dict dict = YamlUtil.load(reader);
                LinkedHashMap<String, Object> sp = (LinkedHashMap<String, Object>) dict.filter("spring").get("spring");
                for (Map.Entry<String, Object> entry : sp.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (key.equals("datasource")) {
                        LinkedHashMap<String, Object> v1 = (LinkedHashMap<String, Object>) value;
                        v1.forEach((key1, value1) -> {
                            if (key1.equals("password")) {
                                jdbc.password = value1.toString();
                            } else if (key1.equals("url")) {
                                jdbc.url = value1.toString();
                            } else if (key1.equals("username")) {
                                jdbc.name = value1.toString();
                            }
                        });
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return jdbc;
        }

        static JDBC datasource() {
            Properties p = loadProperties(Config.TargetPropertiesFile.properties);
            if (p != null) {
                JDBC jdbc = new JDBC();
                jdbc.url = p.getProperty("spring.datasource.url");
                jdbc.password = p.getProperty("spring.datasource.password");
                jdbc.name = p.getProperty("spring.datasource.username");
                return jdbc;
            }
            return null;
        }

    }

    /**
     * 生成文件的目标目录
     */
    private static final String OutDir = System.getProperty("user.dir") + "/target/classes/generated";
    /**
     * 字段前缀
     */
    private static final String FIELD_PREFIX = "f_";
    /**
     * 表名前缀
     */
    private static final String TABLE_PREFIX = "t_";

    private static void fastGenerator(Boolean fromYaml) {
        JDBC datasource = null;
        if (fromYaml) {
            datasource = PropertiesValue.loadPropertiesBaseYaml(Config.TargetPropertiesFile.properties);
        } else {
            datasource = PropertiesValue.datasource();
        }
        assert datasource != null;
        log.info(datasource.toString());
        FastAutoGenerator.create(datasource.url, datasource.name, datasource.password)
                .globalConfig((scan, builder) -> {
                    builder.outputDir(OutDir).dateType(DateType.TIME_PACK);
                    builder.author(scan.apply("Please Enter Author Name:"));
                }).packageConfig(builder -> {
                    builder.parent(Config.ParentPackage.properties)
                            .entity(Config.Entity_Package.properties)
                            .mapper(Config.Mapper_Package.properties)
                            .xml(Config.Mapper_XML_Package.properties);
                }).strategyConfig(builder -> {
                    builder.entityBuilder().enableLombok();
                    builder.addFieldPrefix(FIELD_PREFIX)
                            .addTablePrefix(TABLE_PREFIX);
                }).templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }

    public static void main(String[] args) {
        fastGenerator(false);
    }

}
