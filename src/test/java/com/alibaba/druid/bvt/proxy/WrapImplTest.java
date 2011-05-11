package com.alibaba.druid.bvt.proxy;

import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.alibaba.druid.filter.FilterChain;
import com.alibaba.druid.proxy.DruidDriver;
import com.alibaba.druid.proxy.jdbc.ConnectionProxyImpl;
import com.alibaba.druid.proxy.jdbc.DataSourceProxyImpl;

public class WrapImplTest extends TestCase {

    private static String url = "jdbc:wrap-jdbc:filters=default,commonLogging,log4j,encoding,null:name=demo:jdbc:derby:classpath:petstore-db";

    public void test_clone() throws Exception {
		Class.forName("com.alibaba.druid.proxy.DruidDriver");
		
		DruidDriver driver = (DruidDriver) DriverManager.getDriver(url);
		
		ConnectionProxyImpl connection = (ConnectionProxyImpl) driver.connect(url, new Properties());
		
		connection.getRawObject();
		
		FilterChain filterChain = (FilterChain) connection.createChain();
		filterChain.cloneChain();
		
		DataSourceProxyImpl dataSource = (DataSourceProxyImpl) connection.getDirectDataSource();
		dataSource.getId();
		Assert.assertEquals(4, dataSource.getFilters().size());
		Assert.assertEquals(4, dataSource.getFilterClasses().length);
		Assert.assertNotNull(dataSource.getCreatedTime());
		Assert.assertTrue(dataSource.getCreatedTime().getTime() != 0);
		Assert.assertEquals("org.apache.derby.jdbc.EmbeddedDriver", dataSource.getRawDriverClassName());
		
		Assert.assertEquals(url, dataSource.getUrl());
		Assert.assertEquals("jdbc:derby:classpath:petstore-db", dataSource.getRawUrl());
		Assert.assertEquals(10, dataSource.getRawDriverMajorVersion());
		Assert.assertEquals(5, dataSource.getRawDriverMinorVersion());
		
		Assert.assertFalse(connection.isWrapperFor(com.mysql.jdbc.Connection.class));
		Assert.assertTrue(connection.isWrapperFor(ConnectionProxyImpl.class));
		Assert.assertTrue(connection.isWrapperFor(org.apache.derby.impl.jdbc.EmbedConnection.class));
		Assert.assertNotNull(connection.unwrap(ConnectionProxyImpl.class));
		Assert.assertNull(connection.unwrap(null));
		
		org.apache.derby.impl.jdbc.EmbedConnection derbyConnection = connection.unwrap(org.apache.derby.impl.jdbc.EmbedConnection.class);
		Assert.assertNotNull(derbyConnection);
		
		Statement statement = connection.createStatement();
		Assert.assertFalse(statement.isWrapperFor(com.mysql.jdbc.Statement.class));
		Assert.assertFalse(statement.isWrapperFor(null));
		Assert.assertTrue(statement.isWrapperFor(org.apache.derby.impl.jdbc.EmbedStatement.class));
		
		org.apache.derby.impl.jdbc.EmbedStatement rayStatement = statement.unwrap(org.apache.derby.impl.jdbc.EmbedStatement.class);
		Assert.assertNotNull(rayStatement);
		statement.close();
	}



}