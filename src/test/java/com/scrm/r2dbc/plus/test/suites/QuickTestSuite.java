package com.scrm.r2dbc.plus.test.suites;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * 快速测试套件
 * 包含快速执行的测试，适用于 CI/CD 流水线
 * 
 * 运行方式：
 * mvn test -Dtest=QuickTestSuite
 * 
 * @author dason
 */
@Suite
@SuiteDisplayName("R2DBC Plus - 快速测试套件")
@SelectPackages({
    "com.scrm.r2dbc.plus.test.functional",
    "com.scrm.r2dbc.plus.test.annotation",
    "com.scrm.r2dbc.plus.test.unit"
})
@IncludeTags("fast")
public class QuickTestSuite {
    // 测试套件类，无需实现内容
}
