package en.actionsofproject.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import en.actions.ExtractClassName;
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
				"  `ClassQualifiedName` varchar(1000) DEFAULT NULL,\r\n" + 
				"  `ClassName` varchar(1000) DEFAULT NULL,\r\n" + 
				"  PRIMARY KEY (`ClassID`)\r\n" + 
				") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
		pstmt = (PreparedStatement)conn.prepareStatement(sql);
		pstmt.execute();
		
		sql = "DROP TABLE IF EXISTS `distancevalue`";
		pstmt = (PreparedStatement)conn.prepareStatement(sql);
		pstmt.execute();
		sql = "CREATE TABLE IF NOT EXISTS `distancevalue` (\r\n" + 
				"  `MethodId` int(11) NOT NULL,\r\n" + 
				"  `MethodName` varchar(1000) DEFAULT NULL,\r\n" + 
				"  `MethodParameters` varchar(1000) DEFAULT NULL,\r\n" + 
				"  `MethodOfClass` varchar(1000) DEFAULT NULL,\r\n" + 
				"  `ClassName` varchar(1000) DEFAULT NULL,\r\n" + 
				"  `Distance` varchar(1000) DEFAULT NULL,\r\n" + 
				"  PRIMARY KEY (`methodId`)\r\n" + 
				") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
		pstmt = (PreparedStatement)conn.prepareStatement(sql);
		pstmt.execute();
		
		sql = "DROP TABLE IF EXISTS `methodinfo`";
		pstmt = (PreparedStatement)conn.prepareStatement(sql);
		pstmt.execute();
		sql = "CREATE TABLE IF NOT EXISTS `methodinfo` (\r\n" + 
				"  `MethodID` int(11) NOT NULL,\r\n" + 
				"  `MethodName` varchar(1000) DEFAULT NULL,\r\n" + 
				"  `MethodParameters` varchar(1000) DEFAULT NULL,\r\n" + 
				"  `MethodOfClass` varchar(1000) DEFAULT NULL,\r\n" + 
				"  PRIMARY KEY (`MethodID`)\r\n" + 
				") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
		pstmt = (PreparedStatement)conn.prepareStatement(sql);
		pstmt.execute();
		
		sql = "DROP TABLE IF EXISTS `projectinfo`";
		pstmt = (PreparedStatement)conn.prepareStatement(sql);
		pstmt.execute();
		sql = "CREATE TABLE IF NOT EXISTS `projectinfo` (\r\n" + 
				"  `InfoId` int(11) NOT NULL AUTO_INCREMENT,\r\n" + 
				"  `MethodId` int(11),\r\n" + 
				"  `MethodName` varchar(1000),\r\n" + 
				"  `ClassName` varchar(1000),\r\n" + 
				"  `TargetClassName` varchar(1000),\r\n" + 
				"  `Distance1` varchar(1000),\r\n" + 
				"  `Distance2` varchar(1000),\r\n" + 
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
		String sql = "INSERT INTO projectinfo(MethodId,MethodName,ClassName,TargetClassName,Distance1,Distance2) "
				+ "SELECT methodinfo.MethodId,d1.MethodName,d1.MethodOfClass,d1.ClassName,d2.Distance,d1.Distance "
				+ "FROM distancevalue d1,distancevalue d2,methodinfo "
				+ "WHERE d1.methodName=d2.methodName AND d1.MethodOfClass=d2.MethodOfClass AND d2.MethodOfClass=d2.ClassName AND methodinfo.MethodName=d1.MethodName AND methodinfo.MethodOfClass=D1.MethodOfClass AND d1.MethodOfClass!=d1.ClassName;";
		PreparedStatement pstmt = (PreparedStatement)conn.prepareStatement(sql);
		pstmt.execute();
		conn.close();
	}
	
	//对全名进行分词处理并只保留主名中的前五个字符
	public String[] cutoff(String str) {
		String str_after[] = new String[5];
		int index =str.lastIndexOf(".");
		str = str.substring(index+1,str.length());
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
		for (int i=0;i<5;i++) {
			if(flags.isEmpty()) {
				temp = str.substring(last_index,str.length());
				str_after[i] = temp;
				for(int j=i+1;j<5;j++)
					str_after[j]="*";
				break;
			}
			temp = str.substring(last_index,flags.firstElement());
			str_after[i] = temp;
			last_index = flags.firstElement();
			flags.remove(0);
		}
		
		return str_after;
	}
	
	public void predict() throws Exception{
		ExtractClassName.consoleStream.println("----Data Preprocess");
		// 加载模型
		String path = "C:\\Users\\Administrator\\Desktop\\ReadMe\\Algorithm\\my_model_weights.pb";
//		Graph graph = new Graph();
//		graph.importGraphDef(Files.readAllBytes(Paths.get(path)));
		
				
		if(conn.isClosed()){
			conn = getConn();
		}
		// 获取所有methodId
		Vector<Integer> methodIds = new Vector<Integer>();
		String sql = "SELECT DISTINCT methodid FROM projectinfo";
		PreparedStatement pstmt = (PreparedStatement)conn.prepareStatement(sql);
	    ResultSet rs = pstmt.executeQuery();
	    while(rs.next()){
	    	methodIds.add(rs.getInt("methodid"));
	    }
	    
	    // 获取用例个数
	    sql = "SELECT count(*) FROM projectinfo";
	    pstmt = (PreparedStatement)conn.prepareStatement(sql);
	    rs = pstmt.executeQuery();
	    int numOfCases = 0;
	    if(rs.next()){
	    	numOfCases = rs.getInt("count(*)");
	    }
	    
	    // id.txt:用于记录methodid
	    File id =new File("C:\\Users\\Administrator\\git\\FeatrueEnvyPlugin\\src\\data\\id.txt");
        if(id.exists()){
        	id.delete();
        }
        id.createNewFile();
        FileWriter id_fileWritter = new FileWriter(id,true);
        
        // 一些常量，每个method对应两个文件，一个是id_distances一个是id_names
        String dir = "C:\\Users\\Administrator\\git\\FeatrueEnvyPlugin\\src\\data\\";
        String distancesPostfix = "_distances.txt";
        String namesPostfix = "_names.txt";		
	    
	    // 对每个用例进行处理，从1开始
	    int cur = 1;
	    for (int methoId: methodIds) {
	    	// 将当前methodid写入文件
	    	id_fileWritter.write(String.valueOf(methoId)+"\n");
	    	
	    	// 新建id_distances和id_names
	    	String filename = dir+String.valueOf(methoId)+distancesPostfix;
	    	File id_distances =new File(filename);
	        if(id_distances.exists()){
	        	id_distances.delete();
	        }
	        id_distances.createNewFile();
	        FileWriter id_distances_fileWritter = new FileWriter(id_distances,true);
	        filename = dir+String.valueOf(methoId)+namesPostfix;
	        File id_names =new File(filename);
	        if(id_names.exists()){
	        	id_names.delete();
	        }
	        id_names.createNewFile();
	        FileWriter id_names_fileWritter = new FileWriter(id_names,true);
	        
	    	// 单词和数量映射表
			HashMap<String, Integer > map=new HashMap<String,Integer>();
			// 未转文本时的名字合集
			Vector<String[]> x_names_raw = new Vector<String[]>();
			Vector<int[]> x_names = new Vector<int[]>();
			// 距离合集
			Vector<float[]> x_distances = new Vector<float[]>();
			 
			while(true) {
				sql = "SELECT * FROM projectinfo WHERE infoid=?";
				pstmt = (PreparedStatement)conn.prepareStatement(sql);
		    	pstmt.setString(1, String.valueOf(cur));
		    	rs = pstmt.executeQuery();
		    	if(rs.next()){
		    		if(rs.getInt("MethodId")==methoId) {
		    			cur++;
		    			String MethodName[] = cutoff(rs.getString("MethodName"));
				    	String ClassName[] = cutoff(rs.getString("ClassName"));
				    	String TargetClassName[] = cutoff(rs.getString("TargetClassName"));
				    	String Distance1 = rs.getString("Distance1");
				    	String Distance2 = rs.getString("Distance2");
				    	// 名字信息全都加入x_names_raw中
				    	x_names_raw.add(MethodName);
				    	x_names_raw.add(ClassName);
				    	x_names_raw.add(TargetClassName);
				    	// 名字信息全都加入id_names
				    	id_names_fileWritter.write(MethodName[0]+" "+MethodName[1]+" "+MethodName[2]+" "+MethodName[3]+" "+MethodName[4]+" ");
				    	id_names_fileWritter.write(ClassName[0]+" "+ClassName[1]+" "+ClassName[2]+" "+ClassName[3]+" "+ClassName[4]+" ");
				    	id_names_fileWritter.write(TargetClassName[0]+" "+TargetClassName[1]+" "+TargetClassName[2]+" "+TargetClassName[3]+" "+TargetClassName[4]+"\n");
				    	
				    	// 距离信息全都加入x_distances
				    	float Distance1_d = Float.valueOf(Distance1.trim()).floatValue();
				    	float Distance2_d = Float.valueOf(Distance2.trim()).floatValue();
				    	float[] temp = new float[2];
	    	        	temp[0] = Distance1_d;
	    	        	temp[1] = Distance2_d;
	    	        	x_distances.add(temp);
	    	        	// 距离信息全都加入id_names
	    	        	id_distances_fileWritter.write(Distance1+" "+Distance2+"\n");
	    	        	
				    	// 遍历统计字符中出现的次数
				    	for (int i=0;i<5;i++) {
				    		if (!map.containsKey(MethodName[i]))  
				                map.put(MethodName[i], 1);  
				            else  
				                map.put(MethodName[i], (map.get(MethodName[i])+1));  
				    		if (!map.containsKey(ClassName[i]))  
				                map.put(ClassName[i], 1);  
				            else  
				                map.put(ClassName[i], (map.get(ClassName[i])+1));  
				    		if (!map.containsKey(TargetClassName[i]))  
				                map.put(TargetClassName[i], 1);  
				            else  
				                map.put(TargetClassName[i], (map.get(TargetClassName[i])+1));
				    	} 
		    		}else {	//到下一个方法了
		    			List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
		    			// 次数按照降序排序，如果次数相同，则按照键值的字母升序排序
		    	        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
		    	            public int compare(Map.Entry<String, Integer> mapping2, Map.Entry<String, Integer> mapping1) {
		    	                return mapping1.getValue().compareTo(mapping2.getValue());
		    	            }
		    	        });
		    	        Map.Entry<String, Integer> mapping = null;
		    	        // 转化参考字典
		    	        HashMap<String, Integer > doc =new HashMap<String,Integer>();
		    	        // 这里的i即表示文本向量化后该单词的位置
		    	        for (int i = 0 ;i<list.size() ;i++) {
		    	        	mapping = list.get(i);
		    	        	doc.put(mapping.getKey(), i);
		    	            //System.out.println(mapping.getKey() + "=" + mapping.getValue());
		    	        }
		    	        // 使用转化参考字典进行文本向量化
		    	        for (String[] name : x_names_raw) {
		    	        	int[] temp = new int[5];
		    	        	for (int i = 0; i < 5; i++) {
		    	        		temp[i] = doc.get(name[i]);
		    	        	}
		    	        	x_names.add(temp);
		    	        }
		    	        /* 使用libtensorflow预测
		    	        x_val.addAll(x_names);
		    	        x_val.addAll(x_distances);
		    	        Tensor x_names_tensor = Tensor.create(x_names);
		    	        Tensor x_distances_tensor = Tensor.create(x_distances);
		    	        Tensor x_tensor = Tensor.create(x_val);
		    	        Session sess = new Session(graph);
		    	        Tensor result = sess.runner()
		    	                .feed("ori_quest_embedding", x_tensor)//输入你自己的数据
		    	                .fetch("quest_out") //和上面python保存模型时的output_node_names对应
		    	                .run().get(0);*/
		    	        break;
		    		}
			    }else {	//没有语句了
			    	// 同上
			    	List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
	    	        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
	    	            public int compare(Map.Entry<String, Integer> mapping2, Map.Entry<String, Integer> mapping1) {
	    	                return mapping1.getValue().compareTo(mapping2.getValue());
	    	            }
	    	        });
	    	        Map.Entry<String, Integer> mapping = null;
	    	        HashMap<String, Integer > doc =new HashMap<String,Integer>();
	    	        for (int i = 0 ;i<list.size() ;i++) {
	    	        	mapping = list.get(i);
	    	        	doc.put(mapping.getKey(), i);
	    	            //System.out.println(mapping.getKey() + "=" + mapping.getValue());
	    	        }
	    	        for (String[] name : x_names_raw) {
	    	        	int[] temp = new int[5];
	    	        	for (int i = 0; i < 5; i++) {
	    	        		temp[i] = doc.get(name[i]);
	    	        	}
	    	        	x_names.add(temp);
	    	        }
			    	break;
			    }
			}
			
			id_distances_fileWritter.close();
			id_names_fileWritter.close();
	    	
	    }
	    
	    id_fileWritter.close();
	    
	    ExtractClassName.consoleStream.println("----Connect to keras");
	    
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
            	
            	sql = "select * from projectinfo where MethodId = ?;";
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
	
	/*
	public static void printToConsole(String message, boolean activate) {
		MessageConsoleStream printer = ConsoleFactory.getConsole()
				.newMessageStream();
		printer.setActivateOnWrite(activate);
		printer.println(message);
	}*/

}
