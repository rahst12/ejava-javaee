package ejava.jpa.examples.tuning.suites;

import org.junit.runner.RunWith;

import org.junit.runners.Suite;
import ejava.jpa.examples.tuning.SuiteBase;
import ejava.jpa.examples.tuning.env.FullTableScanSingleIndex;
import ejava.jpa.examples.tuning.env.FullTableScanNoIndex;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	FullTableScanNoIndex.class,
	FullTableScanSingleIndex.class
})
public class FullTableScanTest extends SuiteBase {
	public static final int MAX_ROWS=1000;
}
