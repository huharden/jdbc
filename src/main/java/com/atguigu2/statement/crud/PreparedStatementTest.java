/**
 * 
 */
package com.atguigu2.statement.crud;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Scanner;

import org.junit.Test;

import com.atguigu3.util.JDBCUtils;

/**
 * @Description 演示使用PreparedStatement替换Statement，解决SQL注入问题
 * @author lijipeng
 * @Date Jun 26, 2020 4:44:55 PM
 * 
 * 除了解决Statement的拼串，sql问题之外，Preparedment还有哪些好处呢？
 * 1.PreparedStatement操作Blob的数据，而Statement做不到。
 * 2.PreparedStatement可以实现更高效的批量操作。
 *
 */
public class PreparedStatementTest {
	
	@Test
	public void testLogin() {
		Scanner scanner = new Scanner(System.in);
		System.out.println("请输入用户名：");
		String user = scanner.nextLine();//nextLine 表示
		System.out.println("请输入密码：");
		String password = scanner.next();
		//select user,password from user_table where user = '1' or ' and password = '=1 or '1' = '1'
		String sql = "select user,password from user_table where user = ? and password = ?";
		User returnUser = getInstance(User.class,sql,user,password);
		if (returnUser != null) {
			System.out.println("登录成功！");
		} else {
			System.out.println("用户名不存在或密码错误！");
		}
		
		
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
