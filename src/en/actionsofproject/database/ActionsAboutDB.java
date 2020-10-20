package en.actionsofproject.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.internal.runtime.Activator;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.tensorflow.Graph;
import org.tensorflow.Operation;
import org.tensorflow.Output;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.TensorFlow;

import en.actions.ExtractClassName;
import en.actionsofproject.MethodAndItsRelationedClass;
// import en.actionsofproject.ConsoleFactory;
import en.actionsofproject.database.ui.ClassInfo;
import en.actionsofproject.database.ui.DistanceValue;
import en.actionsofproject.database.ui.EPValue;
import en.actionsofproject.database.ui.MethodInfo;
import en.actionsofproject.database.ui.Relations;

public class ActionsAboutDB {
	
	Connection conn;
	public ActionsAboutDB(){
		try {
			this.conn = getConn();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public Connection getConn() throws Exception{
		Class.forName("com.mysql.jdbc.Driver");
		
        Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/FeatureEnvy",
                "root","root");
        
        //Statement stmt =  conn.createStatement();
        System.out.println("连接数据库成功");
        return conn;
	}
	
	/* 返回i行中最大的值 */
	public int getTableMaxRow(int i) throws Exception{
		String sql = null;
		Connection conn = getConn();
		if(i == 1){
			sql = "select max(KeyNum) from relations;";
		}else{
			if(i == 2){
				sql = "select max(MethodID) from methodinfo;";
			}else{
				if(i == 3){
					sql = "select max(ClassID) from classinfo;";
				}
			}		
		}
		PreparedStatement pstmt = (PreparedStatement)conn.prepareStatement(sql);
	    ResultSet rs = pstmt.executeQuery();
	    //System.out.println("getTableMaxRow"+rs.getInt(0));
	    int maxRow = 0;
	    if(rs.next()){
	    	if(i == 1)
	    		maxRow = rs.getInt("max(KeyNum)");
	    	else
	    		if(i == 2)
	    			maxRow = rs.getInt("max(MethodID)");
	    		else{
	    			maxRow = rs.getInt("max(ClassID)");
	    		}
	    			
	    }
	    pstmt.close();
		conn.close();
	    return maxRow;
	}
	public int getMaxTimes() throws Exception{
		String sql =  "select max(NumOfTimes) from classinfo;";
		Connection conn = getConn();
		PreparedStatement pstmt = (PreparedStatement)conn.prepareStatement(sql);
	    ResultSet rs = pstmt.executeQuery();
	    int maxTimes = 0;
	    if(rs.next()){
	    	maxTimes = rs.getInt("max(NumOfTimes)");
	    }
	    pstmt.close();
		conn.close();
	    return maxTimes;
		
	}
	public void delete(int i) throws Exception{
		String sql = null;
		if(i == 1){
			sql = "delete from relations;";
		}else 
			if(i == 2){
				sql = "delete from methodinfo;";
			}else
				sql = "delete from classinfo;";
		PreparedStatement pstmt = (PreparedStatement)conn.prepareStatement(sql);
	    ResultSet rs = pstmt.executeQuery();
	    pstmt.close();
		conn.close();
	}
	public int getRelationsClassID(String className) throws Exception {
		int classId = 0;
		if(conn.isClosed()){
			conn = getConn();
		}
		String sql = "select ClassID from ClassInfo where ClassQualifiedName = ?;";
		PreparedStatement pstmt;
		pstmt = (PreparedStatement) conn.prepareStatement(sql);
		pstmt.setString(1, className);
	    ResultSet rs = pstmt.executeQuery();
	    while(rs.next()){
	    	classId = rs.getInt("ClassID");
	    }
		pstmt.close();
		conn.close();
		return classId;
	}
	/** 根据已有信息查询methodID */
	public int getRelationsMethodID(String methodName, String methodparameters, String className) throws Exception {
		int methodId = 0;
		if(conn.isClosed()){
			conn = getConn();
		}
		String sql = "select methodID from MethodInfo where methodName = ? and methodParameters = ? and methodOfClass = ?;";
		PreparedStatement pstmt;
		pstmt = (PreparedStatement) conn.prepareStatement(sql);
		pstmt.setString(1, methodName);
		pstmt.setString(2, methodparameters);
		pstmt.setString(3, className);
	    ResultSet rs = pstmt.executeQuery();
	    while(rs.next()){
	    	methodId = rs.getInt("MethodID");
	    }
		pstmt.close();
		conn.close();
		return methodId;
	}
	
	public int insertClassInfo(ClassInfo classInfo) throws Exception{
		int i = 0;
		if(whetherClassIsExistOrNot(classInfo.getClassQualifiedName())==0){
			if(conn.isClosed()){
				conn = getConn();
			}
			String sql = "insert into ClassInfo (ClassID,ClassQualifiedName,ClassName) values(?,?,?);";
			PreparedStatement pstmt;
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			pstmt.setInt(1, classInfo.getClassID());
			pstmt.setString(2, classInfo.getClassQualifiedName());
			pstmt.setString(3, classInfo.getClassName());
			i = pstmt.executeUpdate();
			pstmt.close();
			conn.close();
		}
		
		return i; 
	}
	public int whetherClassIsExistOrNot(String classQualifiedName) throws Exception{
		int i=0;
		if(conn.isClosed()){
			conn = getConn();
		}
		String sql ="select * from ClassInfo where classQualifiedName = ?;";
		PreparedStatement pstmt;
		pstmt = (PreparedStatement) conn.prepareStatement(sql);
		pstmt.setString(1, classQualifiedName);
		ResultSet rs = pstmt.executeQuery();
		if(rs.next())
			i = 1;
		pstmt.close();
		conn.close();
		System.out.println("whetherClassIsExistOrNot-----------"+i);
		return i;
	}
//	public int insertEPValue(EPValue epvalue) throws Exception{
//		int i = 0;
//		String sql = "insert into EPValue (ClassID,ClassName,EntityPlacement) values(?,?,?);";
//		PreparedStatement pstmt;
//		pstmt = (PreparedStatement) conn.prepareStatement(sql);
//		pstmt.setInt(1, epvalue.getClassID());
//		pstmt.setString(2, epvalue.getClassName());
//		pstmt.setDouble(3, epvalue.getEntityPlacement());
//		i = pstmt.executeUpdate();
//		pstmt.close();
//		conn.close();
//		return i;	 
//	}
	public int insertMethodInfo(MethodInfo methodInfo) throws Exception{
		int i = 0;
		if(whetherMethodIsExistOrNot(methodInfo.getMethodName(),methodInfo.getMethodKey(),methodInfo.getMethodOfClass())==0){
			if(conn.isClosed()){
				conn = getConn();
			}
			String sql = "insert into methodinfo (MethodID, MethodName, MethodParameters, MethodOfClass) values(?,?,?,?);";
			PreparedStatement pstmt;
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			pstmt.setInt(1, methodInfo.getMethodID());
			pstmt.setString(2, methodInfo.getMethodName());
			pstmt.setString(3, methodInfo.getMethodKey());
			pstmt.setString(4, methodInfo.getMethodOfClass());
			i = pstmt.executeUpdate();
			pstmt.close();
			conn.close();
		}
//		System.out.println("methodinfo the num of insert----" + i);
		return i;
	}
	public int whetherMethodIsExistOrNot(String methodName, String methodKey, String methodOfClass) throws Exception{
		int i = 0;
		if(conn.isClosed()){
			conn = getConn();
		}
		String sql ="select * from MethodInfo where methodName = ? and methodParameters = ? and methodOfClass = ?;";
		PreparedStatement pstmt;
		pstmt = (PreparedStatement) conn.prepareStatement(sql);
		pstmt.setString(1, methodName);
		pstmt.setString(2, methodKey);
		pstmt.setString(3, methodOfClass);
		ResultSet rs = pstmt.executeQuery();
		if(rs.next())
			i = 1;
		pstmt.close();
		conn.close();
		System.out.println("whether Method Is Exist Or Not----------"+i);
		return i;
	}

	public int insertRelations(Relations relations) throws Exception{
		int i = 0;
		if(whetherRelationsIsExistOrNot(relations) == 0){
			if(conn.isClosed()){
				conn = getConn();
			}
			String sql = "insert into relations (KeyNum, ClassID, MethodID,MethodInThisClassOrNot) values(?,?,?,?);";
			PreparedStatement pstmt;
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			pstmt.setInt(1, relations.getKey());
			pstmt.setInt(2, relations.getClassID());
			pstmt.setInt(3, relations.getMethodID());
			pstmt.setInt(4, relations.getMethodInThisClassOrNot());
			i = pstmt.executeUpdate();
			pstmt.close();
			conn.close();
		}
		return i;
	}
	public int whetherRelationsIsExistOrNot(Relations relations) throws Exception{
		int i = 0;
		if(conn.isClosed()){
			conn = getConn();
		}
		String sql ="select * from relations where MethodID = ? and ClassID = ?;";
		PreparedStatement pstmt;
		pstmt = (PreparedStatement) conn.prepareStatement(sql);
		pstmt.setInt(1, relations.getMethodID());
		pstmt.setInt(2, relations.getClassID());
		ResultSet rs = pstmt.executeQuery();
		if(rs.next())
			i = 1;
		pstmt.close();
		conn.close();
		
		return i;
	}
	public int insertDistanceValue(DistanceValue distanceValue) throws Exception{
		
		int i = 0;
		if(whetherClassExistOrNot(distanceValue.getMethodName(),distanceValue.getMethodKey(), distanceValue.getMethodOfClass(),distanceValue.getClassName()) == 0){
			if(conn.isClosed()){
				conn = getConn();
			}
			String sql = "insert into distanceValue (methodId,methodName,methodParameters, methodOfClass,className,distance) values(?,?,?,?,?,?);";
			PreparedStatement pstmt;
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			pstmt.setInt(1, distanceValue.getMethodId());
			pstmt.setString(2, distanceValue.getMethodName());
			pstmt.setString(3, distanceValue.getMethodKey());
			pstmt.setString(4, distanceValue.getMethodOfClass());
			pstmt.setString(5, distanceValue.getClassName());
			pstmt.setDouble(6, distanceValue.getDistance());
			i = pstmt.executeUpdate();
			pstmt.close();
			conn.close();
		}
//		System.out.println("----------"+i);
	
		return i;	 
	}
	public int getTableMaxRowofDistance() throws Exception{
		if(conn.isClosed()){
			conn = getConn();
		}
		String sql = "select max(methodid) from distanceValue;";
			
		PreparedStatement pstmt = (PreparedStatement)conn.prepareStatement(sql);
	    ResultSet rs = pstmt.executeQuery();
	    //System.out.println("getTableMaxRow"+rs.getInt(0));
	    int maxRow = 0;
	    if(rs.next()){
			maxRow = rs.getInt("max(methodid)");		
	    }
	    else
	    	return 0;
	    pstmt.close();
		conn.close();
	    return maxRow;
	}
	public int whetherClassExistOrNot(String methodName,String methodParameters, String methodOfClass, String className) throws SQLException, Exception{
		int i = 0;
		if(conn.isClosed()){
			conn = getConn();
		}
		String sql = "select * from distanceValue where methodName = ? and methodParameters = ? and methodofclass = ? and className = ?;";
		PreparedStatement pstmt = (PreparedStatement)conn.prepareStatement(sql);
		pstmt.setString(1, methodName);
		pstmt.setString(2, methodParameters);
		pstmt.setString(3, methodOfClass);
		pstmt.setString(4, className);
	    ResultSet rs = pstmt.executeQuery();
	    if(rs.next()){
	    	i = 1;
	    }
	    System.out.println("whetherClassExistOrNot-----"+i);
	    pstmt.close();
		conn.close();
		return i;
	}
	public void commitMySQL(){
		try {
			conn.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// 建表语句
	public void createTables() throws Exception{
		ExtractClassName.consoleStream.println("----Create Table");
		if(conn.isClosed()){
			conn = getConn();
		}
		String sql = "DROP TABLE IF EXISTS `classinfo`";
		PreparedStatement pstmt = (PreparedStatement)conn.prepareStatement(sql);
		pstmt.execute();
		sql = "CREATE TABLE `classinfo` (\r\n" + 
				"  `ClassID` int(11) NOT NULL,\r\n" + 
				"  `ClassQualifiedName` varchar(128) DEFAULT NULL,\r\n" + 
				"  `ClassName` varchar(128) DEFAULT NULL,\r\n" + 
				"  `AllMethod` varchar(1024) DEFAULT NULL,\r\n" + 
				"  PRIMARY KEY (`ClassID`)\r\n" + 
				") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
		pstmt = (PreparedStatement)conn.prepareStatement(sql);
		pstmt.execute();
		
		sql = "DROP TABLE IF EXISTS `distancevalue`";
		pstmt = (PreparedStatement)conn.prepareStatement(sql);
		pstmt.execute();
		sql = "CREATE TABLE IF NOT EXISTS `distancevalue` (\r\n" + 
				"  `MethodId` int(11) NOT NULL,\r\n" + 
				"  `MethodName` varchar(128) DEFAULT NULL,\r\n" + 
				"  `MethodParameters` varchar(1024) DEFAULT NULL,\r\n" + 
				"  `MethodOfClass` varchar(128) DEFAULT NULL,\r\n" + 
				"  `ClassName` varchar(128) DEFAULT NULL,\r\n" + 
				"  `Distance` varchar(32) DEFAULT NULL,\r\n" + 
				"  PRIMARY KEY (`methodId`)\r\n" + 
				") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
		pstmt = (PreparedStatement)conn.prepareStatement(sql);
		pstmt.execute();
		
		sql = "DROP TABLE IF EXISTS `methodinfo`";
		pstmt = (PreparedStatement)conn.prepareStatement(sql);
		pstmt.execute();
		sql = "CREATE TABLE IF NOT EXISTS `methodinfo` (\r\n" + 
				"  `MethodID` int(11) NOT NULL,\r\n" + 
				"  `MethodName` varchar(128) DEFAULT NULL,\r\n" + 
				"  `MethodParameters` varchar(1024) DEFAULT NULL,\r\n" + 
				"  `MethodOfClass` varchar(128) DEFAULT NULL,\r\n" + 
				"  PRIMARY KEY (`MethodID`)\r\n" + 
				") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
		pstmt = (PreparedStatement)conn.prepareStatement(sql);
		pstmt.execute();
		
		sql = "DROP TABLE IF EXISTS `featureenvy`";
		pstmt = (PreparedStatement)conn.prepareStatement(sql);
		pstmt.execute();
		sql = "CREATE TABLE IF NOT EXISTS `featureenvy` (\r\n" + 
				"  `InfoId` int(11) NOT NULL AUTO_INCREMENT,\r\n" + 
				"  `MethodId` int(11),\r\n" + 
				"  `MethodName` varchar(128),\r\n" + 
				"  `MethodParameters` varchar(1024),\r\n" + 
				"  `ClassName` varchar(128),\r\n" + 
				"  `TargetClassName` varchar(128),\r\n" + 
				"  `AllMethod1` varchar(1024),\r\n" + 
				"  `AllMethod2` varchar(1024),\r\n" + 
				"  `Distance1` varchar(32),\r\n" + 
				"  `Distance2` varchar(32),\r\n" + 
				"  PRIMARY KEY (`InfoId`)\r\n" + 
				") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
		pstmt = (PreparedStatement)conn.prepareStatement(sql);
		pstmt.execute();
		
		sql = "DROP TABLE IF EXISTS `relations`";
		pstmt = (PreparedStatement)conn.prepareStatement(sql);
		pstmt.execute();
		sql = "CREATE TABLE IF NOT EXISTS `relations` (\r\n" + 
				"  `KeyNum` int(11) NOT NULL,\r\n" + 
				"  `ClassID` int(11) DEFAULT NULL,\r\n" + 
				"  `MethodID` int(11) DEFAULT NULL,\r\n" + 
				"  `MethodInThisClassOrNot` int(11) DEFAULT NULL,\r\n" + 
				"  PRIMARY KEY (`KeyNum`)\r\n" + 
				") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
		pstmt = (PreparedStatement)conn.prepareStatement(sql);
		pstmt.execute();
		
		pstmt.close();
		conn.close();
	}
	
	// 数据预处理
	public void inputdataPreprocessing() throws Exception{
		if(conn.isClosed()){
			conn = getConn();
		}
		String sql = "INSERT INTO featureenvy(MethodId,MethodName,MethodParameters,ClassName,TargetClassName,AllMethod1,AllMethod2,Distance1,Distance2) "
				+ "SELECT methodinfo.MethodId,d1.MethodName,methodinfo.MethodParameters,d1.MethodOfClass,d1.ClassName,c1.AllMethod,c2.AllMethod,d2.Distance,d1.Distance "
				+ "FROM distancevalue d1,distancevalue d2,methodinfo,classinfo c1,classinfo c2 "
				+ "WHERE d1.methodName=d2.methodName AND d1.MethodOfClass=d2.MethodOfClass AND d2.MethodOfClass=d2.ClassName AND methodinfo.MethodName=d1.MethodName AND methodinfo.MethodOfClass=D1.MethodOfClass AND d1.MethodOfClass!=d1.ClassName AND c1.ClassQualifiedName=d1.MethodOfClass AND c2.ClassQualifiedName=d1.ClassName;";
		PreparedStatement pstmt = (PreparedStatement)conn.prepareStatement(sql);
		pstmt.execute();
		conn.close();
	}
	
	private String arrayToString(String temp_array[]) {
		String temp_toString = "";
		for(int k=0;k<temp_array.length && temp_array[k] != null;k++) {
			temp_toString += temp_array[k];
			temp_toString += " ";
		}
		return temp_toString.trim();
	}
	
	// 单数组拼接，[ A B C, D E ] --> [ A B C D E ] 
	private String[] arrayStitch(String[] array_raw) {
		if(array_raw == null)
			return null;
		List<String> list = new ArrayList<String>();
		for(int i=0;i<array_raw.length;i++) {
			String array[] = array_raw[i].split(" ");
			for(int j=0;j<array.length;j++)
				list.add(array[j]);
		}
		String array[] = (String[])list.toArray(new String[list.size()]);
		return array;
	}
	
	// 多数组拼接，[ A B C] & [ D E ] --> [ A B C D E ] ,限长55，前者优先级更高
	private String[] arrayConcat(String array1[],String array2[]) {
		List<String> list = new ArrayList<String>();
		if(array1 != null) {
			for(int i=0;i<array1.length && list.size()<55;i++) {
				list.add(array1[i]);
			}
		}
		if(array2 != null) {
			for(int i=0;i<array2.length && list.size()<55;i++) {
				list.add(array2[i]);
			}
		}
		if(list.isEmpty())
			return null;
		String array[] = (String[])list.toArray(new String[list.size()]);
		return array;
	}
	
	
	/**	进行分词处理
		isName为true则为方法名或类名，分词后只保留主名中的前五个字符，空位用*补全
		isName为false则为一般单词，进行分词并保留五个单词*/
	public String[] cutoff(String str,boolean isName) {
		int size = 0;
		if (isName) {
			size = 5;
			int index =str.lastIndexOf(".");
			str = str.substring(index+1,str.length());
		}
		else
			size = 10;
		String str_after[] = new String[size];
		//char[] srChar=str.toCharArray();
		Vector<Integer> flags = new Vector<Integer>();
		
		// 按大写字母进行分割, 第一个字母不算直接从第二个字母开始
		for (int i=1;i<str.length();i++) {
			if ((char)str.charAt(i)>='A'&&(char)str.charAt(i)<='Z') {
				// 前面那个字母是小写
				if ((char)str.charAt(i-1)>='a'&&(char)str.charAt(i-1)<='z')
					flags.add(i);
				// 当有后面的字母时, 那个字母是小写
				else if (i+1<str.length())
						if((char)str.charAt(i+1)>='a'&&(char)str.charAt(i+1)<='z') {
							flags.add(i);
						}
				}
			}
		
		int last_index = 0;
		String temp;
		for (int i=0;i<size;i++) {
			if(flags.isEmpty()) {
				if(isName) {
					temp = str.substring(last_index,str.length());
					str_after[i] = temp;
					for(int j=i+1;j<size;j++)
						str_after[j]="*";
					break;
				}
				else {
					temp = str.substring(last_index,str.length());
					str_after[i] = temp;
					break;
				}
			}
			temp = str.substring(last_index,flags.firstElement());
			str_after[i] = temp;
			last_index = flags.firstElement();
			flags.remove(0);
		}
		
		return str_after;
	}
	
	// 对方法参数进行处理
	public String[] cutoff_parameters(String str){
		if(str.equals("0"))
			return null;
		Vector<String> str_after_v = new Vector<String>();
		String temp_para[] = str.split(",");
		Set<String> paraSet = new HashSet<String>();
		for(int i=0;i<temp_para.length;i++) {
			//对容器类参数进行处理
			int index = temp_para[i].indexOf("<");
			if(index>-1) {
				// 对map类参数第一个元素，因为最后没有>符号，获取substring不需-1
				if(temp_para[i].contains("java.util.Map"))
					temp_para[i] = temp_para[i].substring(index+1, temp_para[i].length());
				else
					temp_para[i] = temp_para[i].substring(index+1, temp_para[i].length()-1);
			}
			
			//对数组进行处理
			if(temp_para[i].contains("["))
				temp_para[i] = temp_para[i].substring(0,temp_para[i].length()-2);
			
			// 对map类参数后一个元素遗留的>符号进行处理
			if(temp_para[i].contains(">"))
				temp_para[i] = temp_para[i].substring(0,temp_para[i].length()-1);
			
			//判断是否已存在该参数，因为可能同时存在list<A>和map<A>
			if(paraSet.contains(temp_para[i])) {
				continue;
			}else {
				paraSet.add(temp_para[i]);
				String temp_name[] = temp_para[i].split("\\.");
				
				// 去除基本类型
				String basicType = temp_name[temp_name.length-1];
				if(basicType.equals("int") || basicType.equals("String") || basicType.equals("Double") || basicType.equals("boolean"))
					continue;
				
				for(int j=0;j<temp_name.length;j++) {
					String temp_name_trimed[] = cutoff(temp_name[j],false);
					String temp_name_trimed_toString = arrayToString(temp_name_trimed);
					temp_name[j] = temp_name_trimed_toString;
				}
				
				str_after_v.add(arrayToString(temp_name));
			}
		}
		/*
		 * vector转string
		String str_after = "";
		int size = str_after_v.size();
		for(int i=0;i<size;i++) {
			str_after += str_after_v.firstElement();
			str_after += " ";
			str_after_v.remove(0);
		}*/
		String str_after_a[] = str_after_v.toArray( new String[str_after_v.size()]);
		return str_after_a;
	}
	
	// 对类中所含方法进行处理
	public String[] cutoff_methods(String str){
		if(str == null)
			return null;
		String str_after[] = str.split(" ");
		for(int i=0;i<str_after.length;i++) {
			str_after[i] = arrayToString(cutoff(str_after[i],false));
		}
		return str_after;
	}
	
	public void predict() throws Exception{
		ExtractClassName.consoleStream.println("----Data Preprocess");
		try (Graph g = new Graph()) {
		      final String value = "Hello from " + TensorFlow.version();

		      // Construct the computation graph with a single operation, a constant
		      // named "MyConst" with a value "value".
		      try (Tensor t = Tensor.create(value.getBytes("UTF-8"))) {
		        // The Java API doesn't yet include convenience functions for adding operations.
		        g.opBuilder("Const", "MyConst").setAttr("dtype", t.dataType()).setAttr("value", t).build();
		      }

		      // Execute the "MyConst" operation in a Session.
		      try (Session s = new Session(g);
		           Tensor output = s.runner().fetch("MyConst").run().get(0)) {
		        System.out.println(new String(output.bytesValue(), "UTF-8"));
		      }
		    }
		// 加载模型
		String path = "../../../../lib/model_weights.pb";
//		SavedModelBundle b = SavedModelBundle.load(path);
//		Session tfSession = b.session();
//      Operation operationPredict = b.graph().operation("predict"); // 要执行的op
//      Output output = new Output(operationPredict, 0);
		
				
		if(conn.isClosed()){
			conn = getConn();
		}
		// 获取所有methodId
		Vector<Integer> methodIds = new Vector<Integer>();
		String sql = "SELECT DISTINCT methodid FROM featureenvy";
		PreparedStatement pstmt = (PreparedStatement)conn.prepareStatement(sql);
	    ResultSet rs = pstmt.executeQuery();
	    while(rs.next()){
	    	methodIds.add(rs.getInt("methodid"));
	    }
	    
	    // 获取用例个数
	    sql = "SELECT count(*) FROM featureenvy";
	    pstmt = (PreparedStatement)conn.prepareStatement(sql);
	    rs = pstmt.executeQuery();
	    int numOfCases = 0;
	    if(rs.next()){
	    	numOfCases = rs.getInt("count(*)");
	    }
	    
	    /** 已经弃用的文件流 */
	    /*// id.txt:用于记录methodid
	    File id =new File("C:\\Users\\Administrator\\git\\FeatrueEnvyPlugin\\src\\data\\id.txt");
        if(id.exists()){
        	id.delete();
        }
        id.createNewFile();
        FileWriter id_fileWritter = new FileWriter(id,true);
        
        // 一些常量，每个method对应三个文件：id_methodinfo（记录method经过分词的名字，方法参数），id_classinfo（自身类名字，目标类名字，自身类所含方法，目标类所含方法），id_dis（到自身类距离，到目标类距离）
        String dir = "C:\\Users\\Administrator\\git\\FeatrueEnvyPlugin\\src\\data\\";
        String distancesPostfix = "_dis.txt";
        String classinfoPostfix = "_classinfo.txt";		
        String methodinfoPostfix = "_methodinfo.txt";*/
	    
	    // 类信息的单词和数量映射表
	 	HashMap<String, Integer> map_class=new HashMap<String,Integer>();
	 	// 方法信息的单词和数量映射表
	 	HashMap<String, Integer> map_method=new HashMap<String,Integer>();
	 	// 未转文本时的类信息合集
	 	Vector<String[]> x_class_raw = new Vector<String[]>();
	 	Vector<int[]> x_class = new Vector<int[]>();
	 	// 未转文本时的类信息合集
	 	Vector<String[]> x_method_raw = new Vector<String[]>();
	 	Vector<int[]> x_method = new Vector<int[]>();
	 	// 距离合集
	 	Vector<float[]> x_distances = new Vector<float[]>();
	 	// 神经网络输入数据
	 	float[][] class_data_input = new float[numOfCases][55];
	 	float[][] method_data_input = new float[numOfCases][55];
	 	float[][][] dis_data_input = new float[numOfCases][2][1];
	 			
	 	// 获取所有信息
	 	sql = "SELECT * FROM featureenvy";
 		pstmt = (PreparedStatement)conn.prepareStatement(sql);
     	rs = pstmt.executeQuery();
     	int pos = 0;
	 	while(rs.next()){
	     	String MethodName[] = cutoff(rs.getString("MethodName"),true);
	 		// 该数组未分成单独的词，如[A B C,D E]，需将其转化成[A B C D E],allmethod亦同理
	    	String MethodParameters_raw[] = cutoff_parameters(rs.getString("MethodParameters"));
	     	String MethodParameters[] = arrayStitch(MethodParameters_raw);
		    String ClassName[] = cutoff(rs.getString("ClassName"),true);
		 	String TargetClassName[] = cutoff(rs.getString("TargetClassName"),true);
		 	String AllMethod1_raw[] = cutoff_methods(rs.getString("AllMethod1"));
		 	String AllMethod1[] = arrayStitch(AllMethod1_raw);
		 	String AllMethod2_raw[] = cutoff_methods(rs.getString("AllMethod2"));
		 	String AllMethod2[] = arrayStitch(AllMethod2_raw);
		 	String Distance1 = rs.getString("Distance1");
		 	String Distance2 = rs.getString("Distance2");
		 	
		 	// 将一条记录中method的信息拼在一起，方法名->方法参数，限长55
		 	String methodInfo[] = arrayConcat(MethodName,MethodParameters);
		 	x_method_raw.add(methodInfo);
		 	// 将一条记录中class的信息拼在一起，自身类名->目标类名->前者所含方法->后者所含方法，限长55
		 	String classInfo[] = arrayConcat(arrayConcat(arrayConcat(ClassName,TargetClassName),AllMethod1),AllMethod2);
		 	x_class_raw.add(classInfo);
		 	// 将两个距离值放入集合中
		 	float disInfo[] = {Float.parseFloat(Distance1),Float.parseFloat(Distance2)};
		 	x_distances.add(disInfo);
		 	dis_data_input[pos][0][0]=Float.parseFloat(Distance1);
		 	dis_data_input[pos][1][0]=Float.parseFloat(Distance2);
		 	pos++;
	 	}
	 	
	 	/** 对class信息进行编码  */
	 	// 遍历统计类信息字符中出现的次数
    	for (int i = 0; i < x_class_raw.size(); i++) {
    		String str_temp[] = x_class_raw.get(i);
    		for(int j = 0; j < str_temp.length; j++) {
    			if(!map_class.containsKey(str_temp[j]))
    				map_class.put(str_temp[j], 1);
    			else
    				map_class.put(str_temp[j], (map_class.get(str_temp[j])+1));  
    		}
    	}
		List<Map.Entry<String, Integer>> list_class = new ArrayList<Map.Entry<String, Integer>>(map_class.entrySet());
		// 次数按照降序排序，如果次数相同，则按照键值的字母升序排序
        Collections.sort(list_class, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> mapping2, Map.Entry<String, Integer> mapping1) {
                return mapping1.getValue().compareTo(mapping2.getValue());
            }
        });
        Map.Entry<String, Integer> mapping_class = null;
        // 转化参考字典
        HashMap<String, Integer > doc_class =new HashMap<String,Integer>();
        // 这里的i即表示文本向量化后该单词的位置
        for (int i = 0 ;i<list_class.size() ;i++) {
        	mapping_class = list_class.get(i);
        	doc_class.put(mapping_class.getKey(), i);
            //System.out.println(mapping.getKey() + "=" + mapping.getValue());
        }
        // 使用转化参考字典进行文本向量化
        pos = 0;
        for (String[] str : x_class_raw) {
        	int[] temp = new int[55];
        	for (int i = 0; i < str.length; i++) {
        		temp[i] = doc_class.get(str[i]);
        		class_data_input[pos][i] = (float)temp[i];
        	}
        	x_class.add(temp);
        	pos++;
        }
        
    	/** 对method信息进行编码  */
    	// 遍历统计方法信息字符出现的次数
    	for (int i = 0; i < x_method_raw.size(); i++) {
    		String str_temp[] = x_method_raw.get(i);
    		for(int j = 0; j < str_temp.length; j++) {
    			if(!map_method.containsKey(str_temp[j]))
    				map_method.put(str_temp[j], 1);
    			else
    				map_method.put(str_temp[j], (map_method.get(str_temp[j])+1));  
    		}
    	}
    	List<Map.Entry<String, Integer>> list_method = new ArrayList<Map.Entry<String, Integer>>(map_method.entrySet());
		// 次数按照降序排序，如果次数相同，则按照键值的字母升序排序
        Collections.sort(list_method, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> mapping2, Map.Entry<String, Integer> mapping1) {
                return mapping1.getValue().compareTo(mapping2.getValue());
            }
        });
        Map.Entry<String, Integer> mapping_method = null;
        // 转化参考字典
        HashMap<String, Integer > doc_method =new HashMap<String,Integer>();
        // 这里的i即表示文本向量化后该单词的位置
        for (int i = 0 ;i<list_method.size() ;i++) {
        	mapping_method = list_method.get(i);
        	doc_method.put(mapping_method.getKey(), i);
            //System.out.println(mapping.getKey() + "=" + mapping.getValue());
        }
        // 使用转化参考字典进行文本向量化
        pos = 0;
        for (String[] str : x_method_raw) {
        	int[] temp = new int[55];
        	for (int i = 0; i < str.length; i++) {
        		temp[i] = doc_method.get(str[i]);
        		method_data_input[pos][i] = (float)temp[i];
        	}
        	x_method.add(temp);
        	pos++;
        }
	 	
	 	/** 
		 *  下面是以方法为单位进行编码和检测，已废弃
		 *   目前更改为对所有的信息整体进行编码
		 */
	    
//	    // 对每个用例进行处理，从1开始
//	    int cur = 1;
//	    for (int methoId: methodIds) {
//	    	
//	    	/** 已经弃用的文件流 */
//	    	// 将当前methodid写入文件
//	    	id_fileWritter.write(String.valueOf(methoId)+"\n");
//	    	
//	    	// 新建三个文件
//	    	String filename = dir+String.valueOf(methoId)+distancesPostfix;
//	    	File id_distances =new File(filename);
//	        if(id_distances.exists()){
//	        	id_distances.delete();
//	        }
//	        id_distances.createNewFile();
//	        FileWriter id_distances_fileWritter = new FileWriter(id_distances,true);
//	        
//	        filename = dir+String.valueOf(methoId)+classinfoPostfix;
//	        File id_classinfo =new File(filename);
//	        if(id_classinfo.exists()){
//	        	id_classinfo.delete();
//	        }
//	        id_classinfo.createNewFile();
//	        FileWriter id_classinfo_fileWritter = new FileWriter(id_classinfo,true);
//	        
//	        filename = dir+String.valueOf(methoId)+methodinfoPostfix;
//	        File id_methodinfo =new File(filename);
//	        if(id_methodinfo.exists()){
//	        	id_methodinfo.delete();
//	        }
//	        id_methodinfo.createNewFile();
//	        FileWriter id_methodinfo_fileWritter = new FileWriter(id_methodinfo,true);
//	        
//	    	// 类信息的单词和数量映射表
//			HashMap<String, Integer> map_class=new HashMap<String,Integer>();
//			// 方法信息的单词和数量映射表
//			HashMap<String, Integer> map_method=new HashMap<String,Integer>();
//			// 未转文本时的类信息合集
//			Vector<String[]> x_class_raw = new Vector<String[]>();
//			Vector<int[]> x_class = new Vector<int[]>();
//			// 未转文本时的类信息合集
//			Vector<String[]> x_method_raw = new Vector<String[]>();
//			Vector<int[]> x_method = new Vector<int[]>();
//			// 距离合集
//			Vector<float[]> x_distances = new Vector<float[]>();
//			// 神经网络输入数据
//			float[][] class_data_input = new float[numOfCases][55];
//			float[][] method_data_input = new float[numOfCases][55];
//			float[][][] dis_data_input = new float[numOfCases][2][1];
//			
//			while(true) {
//				sql = "SELECT * FROM featureenvy WHERE infoid=?";
//				pstmt = (PreparedStatement)conn.prepareStatement(sql);
//		    	pstmt.setString(1, String.valueOf(cur));
//		    	rs = pstmt.executeQuery();
//		    	if(rs.next()){
//		    		if(rs.getInt("MethodId")==methoId) {
//		    			cur++;
//
//		    			String MethodName[] = cutoff(rs.getString("MethodName"),true);
//		    			// 该数组未分成单独的词，如[A B C,D E]，需将其转化成[A B C D E],allmethod亦同理
//		    			String MethodParameters_raw[] = cutoff_parameters(rs.getString("MethodParameters"));
//				    	String ClassName[] = cutoff(rs.getString("ClassName"),true);
//				    	String TargetClassName[] = cutoff(rs.getString("TargetClassName"),true);
//				    	String AllMethod1_raw[] = cutoff_methods(rs.getString("AllMethod1"));
//				    	String AllMethod2_raw[] = cutoff_methods(rs.getString("AllMethod2"));
//				    	String Distance1 = rs.getString("Distance1");
//				    	String Distance2 = rs.getString("Distance2");
//				    	
//				    	// 将[A B C,D E]转化成[A B C D E]的方法
//				    	String MethodParameters[] = arrayStitch(MethodParameters_raw);
//				    	String AllMethod1[] = arrayStitch(AllMethod1_raw);
//				    	String AllMethod2[] = arrayStitch(AllMethod2_raw);
//				    	
//				    	// 方法信息全都加入x_method_raw中
//				    	x_method_raw.add(MethodName);
//				    	x_method_raw.add(MethodParameters);
//				    	
//				    	// 类信息全都加入x_class_raw中
//				    	x_class_raw.add(ClassName);
//				    	x_class_raw.add(TargetClassName);
//				    	x_class_raw.add(AllMethod1);
//				    	x_class_raw.add(AllMethod2);
//				    	
//				    	/** 已经弃用的文件流 */
//				    	/*// 名字信息全都加入id_names
//				    	id_names_fileWritter.write(MethodName[0]+" "+MethodName[1]+" "+MethodName[2]+" "+MethodName[3]+" "+MethodName[4]+" ");
//				    	id_names_fileWritter.write(ClassName[0]+" "+ClassName[1]+" "+ClassName[2]+" "+ClassName[3]+" "+ClassName[4]+" ");
//				    	id_names_fileWritter.write(TargetClassName[0]+" "+TargetClassName[1]+" "+TargetClassName[2]+" "+TargetClassName[3]+" "+TargetClassName[4]+"\n");*/
//				    	
//				    	// 距离信息全都加入x_distances
//				    	float Distance1_d = Float.valueOf(Distance1.trim()).floatValue();
//				    	float Distance2_d = Float.valueOf(Distance2.trim()).floatValue();
//				    	float[] temp = new float[2];
//	    	        	temp[0] = Distance1_d;
//	    	        	temp[1] = Distance2_d;
//	    	        	x_distances.add(temp);
//	    	        	/** 已经弃用的文件流 */
//	    	        	/*// 距离信息全都加入id_names
//	    	        	id_distances_fileWritter.write(Distance1+" "+Distance2+"\n");*/
//	    	        	
//				    	// 遍历统计类信息字符中出现的次数
//				    	for (int i=0;i<5;i++) {
//				    		if (!map_class.containsKey(ClassName[i]))  
//				    			map_class.put(ClassName[i], 1);  
//				            else  
//				            	map_class.put(ClassName[i], (map_class.get(ClassName[i])+1));  
//				    		if (!map_class.containsKey(TargetClassName[i]))  
//				    			map_class.put(TargetClassName[i], 1);  
//				            else  
//				            	map_class.put(TargetClassName[i], (map_class.get(TargetClassName[i])+1)); 
//				    	}
//				    	for (int i=0;i<AllMethod1.length;i++) {
//				    		if (!map_class.containsKey(AllMethod1[i]))  
//				    			map_class.put(AllMethod1[i], 1);  
//				            else  
//				            	map_class.put(AllMethod1[i], (map_class.get(AllMethod1[i])+1));
//				    	}
//				    	for (int i=0;i<AllMethod2.length;i++) {
//				    		if (!map_class.containsKey(AllMethod2[i]))  
//				    			map_class.put(AllMethod2[i], 1);  
//				            else  
//				            	map_class.put(AllMethod2[i], (map_class.get(AllMethod2[i])+1));
//				    	}
//				    	
//				    	// 遍历统计方法信息字符出现的次数
//				    	for (int i=0;i<5;i++) {
//				    		if (!map_method.containsKey(MethodName[i]))  
//				    			map_method.put(MethodName[i], 1);  
//				            else  
//				            	map_method.put(MethodName[i], (map_method.get(MethodName[i])+1));  
//				    	}
//				    	for (int i=0;i<MethodParameters.length;i++) {
//				    		if (!map_method.containsKey(MethodParameters[i]))  
//				    			map_method.put(MethodParameters[i], 1);  
//				            else  
//				            	map_method.put(MethodParameters[i], (map_method.get(MethodParameters[i])+1));  
//				    	}
//		    		}else {	//到下一个方法了
//		    			
//		    			/** 对class进行处理 */
//		    			List<Map.Entry<String, Integer>> list_class = new ArrayList<Map.Entry<String, Integer>>(map_class.entrySet());
//		    			// 次数按照降序排序，如果次数相同，则按照键值的字母升序排序
//		    	        Collections.sort(list_class, new Comparator<Map.Entry<String, Integer>>() {
//		    	            public int compare(Map.Entry<String, Integer> mapping2, Map.Entry<String, Integer> mapping1) {
//		    	                return mapping1.getValue().compareTo(mapping2.getValue());
//		    	            }
//		    	        });
//		    	        Map.Entry<String, Integer> mapping_class = null;
//		    	        // 转化参考字典
//		    	        HashMap<String, Integer > doc_class =new HashMap<String,Integer>();
//		    	        // 这里的i即表示文本向量化后该单词的位置
//		    	        for (int i = 0 ;i<list_class.size() ;i++) {
//		    	        	mapping_class = list_class.get(i);
//		    	        	doc_class.put(mapping_class.getKey(), i);
//		    	            //System.out.println(mapping.getKey() + "=" + mapping.getValue());
//		    	        }
//		    	        // 使用转化参考字典进行文本向量化
//		    	        for (String[] str : x_class_raw) {
//		    	        	int[] temp = new int[5];
//		    	        	for (int i = 0; i < str.length; i++) {
//		    	        		temp[i] = doc_class.get(str[i]);
//		    	        		
//		    	        	}
//		    	        	x_class.add(temp);
//		    	        }
//		    	        
//		    	        
//		    	        /** 对method进行处理 */
//		    			List<Map.Entry<String, Integer>> list_method = new ArrayList<Map.Entry<String, Integer>>(map_method.entrySet());
//		    			// 次数按照降序排序，如果次数相同，则按照键值的字母升序排序
//		    	        Collections.sort(list_method, new Comparator<Map.Entry<String, Integer>>() {
//		    	            public int compare(Map.Entry<String, Integer> mapping2, Map.Entry<String, Integer> mapping1) {
//		    	                return mapping1.getValue().compareTo(mapping2.getValue());
//		    	            }
//		    	        });
//		    	        Map.Entry<String, Integer> mapping_method = null;
//		    	        // 转化参考字典
//		    	        HashMap<String, Integer > doc_method =new HashMap<String,Integer>();
//		    	        // 这里的i即表示文本向量化后该单词的位置
//		    	        for (int i = 0 ;i<list_method.size() ;i++) {
//		    	        	mapping_class = list_method.get(i);
//		    	        	doc_class.put(mapping_method.getKey(), i);
//		    	            //System.out.println(mapping.getKey() + "=" + mapping.getValue());
//		    	        }
//		    	        // 使用转化参考字典进行文本向量化，只保留55个单词
//		    	        int pos = 0;
//		    	        for (String[] name : x_method_raw) {
//		    	        	int[] temp = new int[5];
//		    	        	for (int i = 0; i < name.length && pos<55; i++) {
//		    	        		temp[i] = doc_method.get(name[i]);
//		    	        		method_data_input[cur][pos] = doc_method.get(name[i]);
//		    	        		pos++;
//		    	        	}
//		    	        	x_method.add(temp);
//		    	        }
//		    	        
//		    	        /* 使用libtensorflow预测
//		    	        x_val.addAll(x_names);
//		    	        x_val.addAll(x_distances);
//		    	        Tensor x_names_tensor = Tensor.create(x_names);
//		    	        Tensor x_distances_tensor = Tensor.create(x_distances);
//		    	        Tensor x_tensor = Tensor.create(x_val);
//		    	        Session sess = new Session(graph);
//		    	        Tensor result = sess.runner()
//		    	                .feed("ori_quest_embedding", x_tensor)//输入你自己的数据
//		    	                .fetch("quest_out") //和上面python保存模型时的output_node_names对应
//		    	                .run().get(0);*/
//		    	        break;
//		    		}
//			    }else {	//没有语句了
//			    	// 同上
//			    	/** 对class进行处理 */
//	    			List<Map.Entry<String, Integer>> list_class = new ArrayList<Map.Entry<String, Integer>>(map_class.entrySet());
//	    			// 次数按照降序排序，如果次数相同，则按照键值的字母升序排序
//	    	        Collections.sort(list_class, new Comparator<Map.Entry<String, Integer>>() {
//	    	            public int compare(Map.Entry<String, Integer> mapping2, Map.Entry<String, Integer> mapping1) {
//	    	                return mapping1.getValue().compareTo(mapping2.getValue());
//	    	            }
//	    	        });
//	    	        Map.Entry<String, Integer> mapping_class = null;
//	    	        // 转化参考字典
//	    	        HashMap<String, Integer > doc_class =new HashMap<String,Integer>();
//	    	        // 这里的i即表示文本向量化后该单词的位置
//	    	        for (int i = 0 ;i<list_class.size() ;i++) {
//	    	        	mapping_class = list_class.get(i);
//	    	        	doc_class.put(mapping_class.getKey(), i);
//	    	            //System.out.println(mapping.getKey() + "=" + mapping.getValue());
//	    	        }
//	    	        // 使用转化参考字典进行文本向量化
//	    	        for (String[] name : x_class_raw) {
//	    	        	int[] temp = new int[5];
//	    	        	for (int i = 0; i < 5; i++) {
//	    	        		temp[i] = doc_class.get(name[i]);
//	    	        	}
//	    	        	x_class.add(temp);
//	    	        }
//	    	        
//	    	        
//	    	        /** 对method进行处理 */
//	    			List<Map.Entry<String, Integer>> list_method = new ArrayList<Map.Entry<String, Integer>>(map_method.entrySet());
//	    			// 次数按照降序排序，如果次数相同，则按照键值的字母升序排序
//	    	        Collections.sort(list_method, new Comparator<Map.Entry<String, Integer>>() {
//	    	            public int compare(Map.Entry<String, Integer> mapping2, Map.Entry<String, Integer> mapping1) {
//	    	                return mapping1.getValue().compareTo(mapping2.getValue());
//	    	            }
//	    	        });
//	    	        Map.Entry<String, Integer> mapping_method = null;
//	    	        // 转化参考字典
//	    	        HashMap<String, Integer > doc_method =new HashMap<String,Integer>();
//	    	        // 这里的i即表示文本向量化后该单词的位置
//	    	        for (int i = 0 ;i<list_method.size() ;i++) {
//	    	        	mapping_class = list_method.get(i);
//	    	        	doc_class.put(mapping_method.getKey(), i);
//	    	            //System.out.println(mapping.getKey() + "=" + mapping.getValue());
//	    	        }
//	    	        // 使用转化参考字典进行文本向量化
//	    	        for (String[] name : x_method_raw) {
//	    	        	int[] temp = new int[5];
//	    	        	for (int i = 0; i < 5; i++) {
//	    	        		temp[i] = doc_method.get(name[i]);
//	    	        	}
//	    	        	x_method.add(temp);
//	    	        }
//			    	break;
//			    }
//			}
//			
//			/** 已经弃用的文件流 */
//			/*id_distances_fileWritter.close();
//			id_names_fileWritter.close();*/
//	    	
//	    }
	    
	    /** 已经弃用的文件流 */
	    /*id_fileWritter.close();*/
	    
	    ExtractClassName.consoleStream.println("----Connect to keras");
	    
//	    ProposedApproach.getDefault().getStateLocation().makeAbsolute().toFile().getAbsolutePath());
//	    URL url2 = Activator.getDefault().getBundle(Activator.PLUGIN_ID).getEntry("plugin.xml");
//	    URL url1 = Activator.getDefault().getBundle(Activator.PLUGIN_ID).getEntry("lib1/dom4j-1.6.1.jar");
//	    System.out.println(url2); 	
//	    System.out.println(FileLocator.toFileURL(url2));
	    Graph graph = new Graph();
	    String path_actived = FileLocator.toFileURL(Platform.getBundle(Activator.PLUGIN_ID).getEntry("")).getPath();
	    graph.importGraphDef(Files.readAllBytes(Paths.get(path_actived)));
		Session session = new Session(graph);
		Tensor class_data_tensor = Tensor.create(class_data_input);
		Tensor method_data_tensor = Tensor.create(method_data_input);
		Tensor dis_data_tensor = Tensor.create(dis_data_input);
		List<Tensor> x_data = new ArrayList<Tensor>();
		x_data.add(class_data_tensor);
		x_data.add(method_data_tensor);
		x_data.add(dis_data_tensor);
		// Tensor y = session.runner().feed("input_1", x_data).fetch("output_1").run().get(0);
		
		graph.close();
	    
	    try {

	           String cmd = "cmd /c C:\\Users\\Administrator\\git\\FeatrueEnvyPlugin\\src\\proxy.bat";

	           Process process = Runtime.getRuntime().exec(cmd);

	           BufferedReader reader=new BufferedReader(new InputStreamReader(process.getInputStream(),"GBK"));
	           String line;
	           line=reader.readLine();
	           System.out.println(line);
	           reader.close();
	           process.destroy();
	     } catch (Exception e) {
	           e.printStackTrace();
	     }
	    
	    try {
	    	ExtractClassName.consoleStream.setColor(new Color(null,0,173,232));
	    	ExtractClassName.consoleStream.println("\n\n***********************************\n");
	    	ExtractClassName.consoleStream.println("Featrue Envy Detection Result");
	    	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = new Date();
			String s = simpleDateFormat.format(date);
			ExtractClassName.consoleStream.println("Time: " + s);
		    
	    	String strFile = "C:\\Users\\Administrator\\git\\FeatrueEnvyPlugin\\src\\data\\predictResult.txt";
            File file = new File(strFile);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String strLine = null;
            ExtractClassName.consoleStream.println("Number of potential methods: " +String.valueOf(methodIds.size()));
            int cur_id = 0;
            while(null != (strLine = bufferedReader.readLine())){
            	cur_id ++;
            	String[] temp = strLine.split(" ");
            	int methodId = Integer.parseInt(temp[0]);
            	int result = Integer.parseInt(temp[1]);
            	int num = Integer.parseInt(temp[2]);
            	
            	//有坏味时的预测结果
            	String comTarget = null;
            	float min = 0;
            	
            	sql = "select * from featureenvy where MethodId = ?;";
        		pstmt = (PreparedStatement) conn.prepareStatement(sql);
        		pstmt.setString(1, String.valueOf(methodId));
        	    rs = pstmt.executeQuery();
        	    String MethodName = null;
        	    String ClassName = null;
        	    String TargetClassName = null;
        	    if(rs.next()) {
        	    	MethodName = rs.getString("MethodName");
        	    	ClassName = rs.getString("ClassName");
        	    	TargetClassName = rs.getString("TargetClassName");
        	    }
        	    // 打印该方法的信息
        	    ExtractClassName.consoleStream.println("No. " + String.valueOf(cur_id));
        	    ExtractClassName.consoleStream.println("ClassName: " + ClassName);
        	    ExtractClassName.consoleStream.println("MethodName: " + MethodName);
        	    if(result == 0) 
        	    	ExtractClassName.consoleStream.println("Prediction results: No Featrue envy Smell.");
        	    else
        	    	ExtractClassName.consoleStream.println("Prediction results: May have Featrue envy Smell！");
        	    ExtractClassName.consoleStream.println("FE Probability of Detection Object：");
        	    strLine = bufferedReader.readLine();
        	    min = Float.parseFloat(strLine);
        	    comTarget = TargetClassName;
        	    ExtractClassName.consoleStream.println("    " + TargetClassName + " : " + strLine);
        	    while(rs.next()){
        	    	TargetClassName = rs.getString("TargetClassName");
        	    	strLine = bufferedReader.readLine();
        	    	if (min > Float.parseFloat(strLine)) {
        	    		min = Float.parseFloat(strLine);
        	    		comTarget = TargetClassName;
        	    	}
            	    ExtractClassName.consoleStream.println("    " + TargetClassName + " : " + strLine);
        	    }
        	    if(result == 1) 
        	    	ExtractClassName.consoleStream.println("Advice: Move This Method To " + comTarget);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
	    
	    pstmt.close();
		conn.close();
		System.out.println("End_Of_Predict");
	}
	
	// 更新allMethod信息至class表
	public void addAllMethods() {
		try {
			if(conn.isClosed()){
				conn = getConn();
			}
			String sql = "SELECT ClassID FROM ClassInfo;";
			PreparedStatement pstmt;
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
		    while(rs.next()){
		    	int classID = rs.getInt("ClassID");
		    	String allMethod = new String();
		    	List<String> allMethod_List = null;
				allMethod_List = MethodAndItsRelationedClass.MethodInClass.get(classID);
		    	if (allMethod_List != null) {
		    		for (int i = 0; i < allMethod_List.size(); i++) {
			    		allMethod += allMethod_List.get(i);
			    		allMethod += ' ';
			    	}
			    	
			    	sql = "UPDATE ClassInfo SET AllMethod = ? WHERE ClassID = ? ;";
			    	pstmt = (PreparedStatement)conn.prepareStatement(sql);
			    	pstmt.setString(1, allMethod);
			    	pstmt.setInt(2, classID);
					pstmt.execute();
		    	}
		    }
			pstmt.close();
			conn.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	public static void printToConsole(String message, boolean activate) {
		MessageConsoleStream printer = ConsoleFactory.getConsole()
				.newMessageStream();
		printer.setActivateOnWrite(activate);
		printer.println(message);
	}*/

}
