package com.scrm.r2dbc.plus.test.suites;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * 慢速测试套件
 * 包含性能测试等耗时测试
 * 
 * 运行方式：
 * mvn test -Dtest=SlowTestSuite
 * 
 * @author dason
 */
@Suite
@SuiteDisplayName("R2DBC Plus - 慢速测试套件")
@SelectPackages({
    "com.scrm.r2dbc.plus.test.performance",
    "com.scrm.r2dbc.plus.test.integration"
})
@IncludeTags("slow")
public class SlowTestSuite {
    // 测试套件类，无需实现内容
}
