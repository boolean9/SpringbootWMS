package com.wms.common;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;
import java.util.Scanner;

public class CodeGenerator {

    public static String scanner(String tip) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入" + tip + "：");
        if (scanner.hasNext()) {
            return scanner.next().trim();
        }
        throw new IllegalArgumentException("请输入正确的" + tip + "。");
    }

    public static void main(String[] args) {
        String projectPath = System.getProperty("user.dir");

        FastAutoGenerator.create(
                "jdbc:mysql://localhost:3306/wms?useUnicode=true&characterEncoding=UTF8&useSSL=false&serverTimezone=GMT%2B8",
                "root",
                "root"
        ).globalConfig(builder -> builder
                .author("wms")
                .disableOpenDir()
                .outputDir(projectPath + "/src/main/java"))
                .packageConfig(builder -> builder
                        .parent("com.wms")
                        .pathInfo(Collections.singletonMap(OutputFile.xml, projectPath + "/src/main/resources/mapper")))
                .strategyConfig(builder -> builder
                        .addInclude(scanner("表名，多个英文逗号分隔").split(","))
                        .entityBuilder()
                        .enableLombok()
                        .controllerBuilder()
                        .enableRestStyle()
                        .serviceBuilder()
                        .formatServiceFileName("%sService"))
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }
}
