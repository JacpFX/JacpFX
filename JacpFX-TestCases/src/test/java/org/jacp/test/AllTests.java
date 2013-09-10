package org.jacp.test;

import org.jacp.test.missconfig.MissconfigLauncherTest;
import org.jacp.test.missconfig.MissconfigWorkbenchTest;
import org.jacp.test.workbench.BasicInitialisationTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 09.09.13
 * Time: 21:40
 * To change this template use File | Settings | File Templates.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ BasicInitialisationTests.class, MissconfigLauncherTest.class,MissconfigWorkbenchTest.class })
public class AllTests {
}
