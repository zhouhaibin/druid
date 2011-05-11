package com.alibaba.druid.bvt.pool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;

import org.junit.Assert;

import junit.framework.TestCase;

import com.alibaba.druid.pool.DruidDataSource;

public class Test_C2 extends TestCase {
	private String jdbcUrl;
	private String user;
	private String password;
	private String driverClass;
	private int minPoolSize = 50;
	private int maxPoolSize = 100;
	private int maxActive = 500;
	
	protected void setUp() throws Exception {
		// jdbcUrl =
		// "jdbc:mysql://10.20.129.146/dragoon_v25masterdb?useUnicode=true&characterEncoding=UTF-8";
		// user = "dragoon25";
		// password = "dragoon25";
		// driverClass = "com.mysql.jdbc.Driver";

		jdbcUrl = "jdbc:fake:dragoon_v25masterdb";
		user = "dragoon25";
		password = "dragoon25";
		driverClass = "com.alibaba.druid.mock.MockDriver";
	}
	
	public void test_concurrent_2() throws Exception {
		final DruidDataSource dataSource = new DruidDataSource();

		Class.forName("com.alibaba.druid.mock.MockDriver");

		dataSource.setInitialSize(10);
		dataSource.setMaxActive(maxPoolSize);
		dataSource.setMinIdle(minPoolSize);
		dataSource.setMaxIdle(maxPoolSize);
		dataSource.setPoolPreparedStatements(true);
		dataSource.setDriverClassName(driverClass);
		dataSource.setUrl(jdbcUrl);
		dataSource.setPoolPreparedStatements(true);

		final int THREAD_COUNT = 2;
		final int LOOP_COUNT = 1000 * 1000;
		
		final CountDownLatch startLatch = new CountDownLatch(1);
		final CountDownLatch endLatch = new CountDownLatch(THREAD_COUNT);

		for (int threadIndex = 0; threadIndex < THREAD_COUNT; ++threadIndex) {
			Thread thread = new Thread() {
				public void run() {
					try {
						startLatch.await();
						
						for (int i = 0; i < LOOP_COUNT; ++i) {
							Connection conn = dataSource.getConnection();
							conn.close();
						}
					} catch (Throwable e) {
						e.printStackTrace();
					} finally {
						endLatch.countDown();
					}
				}
			};
			thread.start();
		}
		
		startLatch.countDown();
		endLatch.await();
		
		Assert.assertEquals(THREAD_COUNT * LOOP_COUNT, dataSource.getConnectCount());
		Assert.assertEquals(THREAD_COUNT * LOOP_COUNT, dataSource.getCloseCount());
		Assert.assertEquals(0, dataSource.getConnectErrorCount());
		Assert.assertEquals(0, dataSource.getActiveCount());
	}
}