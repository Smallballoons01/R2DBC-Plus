package com.scrm.r2dbc.plus.test.suites;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * 全部测试套件
 * 包含所有测试用例，用于完整验证
 * 
 * 运行方式：
 * mvn test -Dtest=AllTestSuite
 * 
 * @author dason
 */
@Suite
@SuiteDisplayName("R2DBC Plus - 全部测试套件")
@SelectPackages({
    "com.scrm.r2dbc.plus.test.functional",
    "com.scrm.r2dbc.plus.test.annotation", 
    "com.scrm.r2dbc.plus.test.unit",
    "com.scrm.r2dbc.plus.test.integration",
    "com.scrm.r2dbc.plus.test.performance",
    "com.scrm.r2dbc.plus.test.compatibility",
    "com.scrm.r2dbc.plus.test.xml"
})
public class AllTestSuite {
    // 测试套件类，无需实现内容
}
