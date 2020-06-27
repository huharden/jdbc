/**
 * 
 */
package com.atguigu3.preparedstatement.crud;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.atguigu3.bean.Customer;
import com.atguigu3.bean.Order;
import com.atguigu3.util.JDBCUtils;

/**
 * @Description 使用PreparedStatement实现不同表的通用的查询操作
 * @author lijipeng
 * @Date Jun 25, 2020 7:15:42 PM
 *
 */
public class PreparedStatementQueryTest {
	
	@Test
	public void testGetForList() {
		
		String sql = "select id,name,email from customers where id < ?";
		List<Customer> list = getForList(Customer.class,sql,12);
		list.forEach(System.out::println);
		
		String sql1 = "select order_id orderId,order_name orderName from 'order' where order_id < ?";
		List<Order> OrderList = getForList(Order.class, sql1, 5);
		OrderList.forEach(System.out::println);
	}
	
	public <T> List<T> getForList(Class<T> clazz,String sql,Object ...args){
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			//1.获取数据库连接
			conn = JDBCUtils.getConnection();
			//2.获取prepareStatement实例
			ps = conn.prepareStatement(sql);
			for (int i = 0; i < args.length; i++) {
				ps.setObject(i + 1, args[i]);
			}
			
			rs = ps.executeQuery();
			//获取结果集的元数据：ResultSetMetaData
			ResultSetMetaData rsmd = rs.getMetaData();
			//通过ResultSetMetaData获取结果集中的列数
			int columnCount = rsmd.getColumnCount();
			//创建集合对象
			ArrayList<T> list = new ArrayList<T>();
			while (rs.next()) {
				 T t = clazz.newInstance();
				//处理结果集一行数据中的每一列：给t对象指定的属性赋值
				for (int i = 0; i < columnCount; i++) {
					Object columnValue = rs.getObject(i + 1);
					
					//获取每个列的列名
					//String columnName = rsmd.getColumnName(i + 1);
					String columnLabel = rsmd.getColumnLabel(i + 1);
					
					//给cust对象指定的columnName属性，赋值为columnValue：通过反射
					Field field = clazz.getDeclaredField(columnLabel);
					field.setAccessible(true);
					field.set(t,columnValue);
				}
				list.add(t);
				
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			JDBCUtils.closeResource(conn, ps,rs);
		}
		
		return null;
	}
	
	@Test
	public void testGetInstance() {
		String sql = "select id,name,email from customers where id = ?";
		Customer customer = getInstance(Customer.class, sql, 12);
		System.out.println(customer);
		
		String sql1 = "select order_id orderId,order_name orderName from 'order' where order_id = ?";
		Order order = getInstance(Order.class, sql1, 1);
		System.out.println(order);
	}
	
	/**
	 * 
	 * @Description 针对于不同的表的通用查询操作，返回表中的一条记录
	 * @author lijipeng
	 * @Date Jun 25, 2020 8:02:52 PM
	 *
	 * @param <T>
	 * @param clazz
	 * @param sql
	 * @param args
	 * @return
	 */
	
	public <T> T getInstance(Class<T> clazz,String sql,Object ...args) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			//1.获取数据库连接
			conn = JDBCUtils.getConnection();
			//2.获取prepareStatement实例
			ps = conn.prepareStatement(sql);
			for (int i = 0; i < args.length; i++) {
				ps.setObject(i + 1, args[i]);
			}
			
			rs = ps.executeQuery();
			//获取结果集的元数据：ResultSetMetaData
			ResultSetMetaData rsmd = rs.getMetaData();
			//通过ResultSetMetaData获取结果集中的列数
			int columnCount = rsmd.getColumnCount();
			
			if (rs.next()) {
				 T t = clazz.newInstance();
				//处理结果集一行数据中的每一列
				for (int i = 0; i < columnCount; i++) {
					Object columnValue = rs.getObject(i + 1);
					
					//获取每个列的列名
					//String columnName = rsmd.getColumnName(i + 1);
					String columnLabel = rsmd.getColumnLabel(i + 1);
					
					//给cust对象指定的columnName属性，赋值为columnValue：通过反射
					Field field = clazz.getDeclaredField(columnLabel);
					field.setAccessible(true);
					field.set(t,columnValue);
				}
				return t;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			JDBCUtils.closeResource(conn, ps,rs);
		}
		
		return null;
	}

}
