package com.scrm.r2dbc.plus.test.suites;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * 核心功能测试套件
 * 包含核心功能测试，日常开发使用
 * 
 * 运行方式：
 * mvn test -Dtest=CoreTestSuite
 * 
 * @author dason
 */
@Suite
@SuiteDisplayName("R2DBC Plus - 核心功能测试套件")
@SelectPackages({
    "com.scrm.r2dbc.plus.test.functional",
    "com.scrm.r2dbc.plus.test.annotation",
    "com.scrm.r2dbc.plus.test.unit"
})
@IncludeTags("core")
public class CoreTestSuite {
    // 测试套件类，无需实现内容
}
