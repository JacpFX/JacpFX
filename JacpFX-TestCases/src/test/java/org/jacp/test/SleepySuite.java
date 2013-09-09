package org.jacp.test;


import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.runners.Suite;
import org.junit.runners.model.RunnerBuilder;


import java.lang.annotation.*;


/**
 * Created with IntelliJ IDEA.
 * User: ady
 * Date: 09.09.13
 * Time: 22:25
 * To change this template use File | Settings | File Templates.
 */
public class SleepySuite  extends Suite {

    private final Integer defaultSleepSec = 0;
    private final Integer sleepSec;

    public SleepySuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(klass, builder);
        sleepSec = initSleep(klass);
    }

    private Integer initSleep(Class<?> klass) {
        SleepSec ts = klass.getAnnotation(SleepSec.class);
        Integer sleep = defaultSleepSec;
        if (ts != null) {
            sleep = ts.value();

        }
        return sleep;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    public @interface SleepSec {
        public int value();
    }

    /**
     * @see org.junit.runners.Suite#runChild(org.junit.runner.Runner, org.junit.runner.notification.RunNotifier)
     */
    @Override
    protected void runChild(Runner runner, RunNotifier notifier) {
        super.runChild(runner, notifier);
        //Simply wrapped Thread.sleep(long)
        try {
            Thread.currentThread().sleep(sleepSec);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}