package en.actionsofproject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import en.actionsofproject.database.ActionsAboutDB;
import en.actionsofproject.database.ui.MethodInfo;
import en.actionsofproject.database.ui.Relations;
import en.entitys.Entity;


/**  */
public class MethodAndItsRelationedClass {
	
	IProject iProject;
	public TypeDeclaration typeDeclarationOfCurrentClass;
	List<IMethod> allMethods = new ArrayList<IMethod>(); //the iJavaproject's all allMethods
	List<IType> types = new ArrayList<IType>();
	Map<IMethod, IVariableBinding> methodAndTarget = new HashMap();//method and it's one target
//	Map<IMethod, MethodDeclaration> methodAndItsMethodDeclaration =new HashMap();
	
	
	public MethodAndItsRelationedClass(List<IType> types, List<IMethod> allMethods){
		this.allMethods = allMethods;
		this.types = types;
	}
	public void getRelationsClass(IType type){
		Entity entity = new Entity(type);
		for(IMethod method : entity.getMethods()){
			System.out.println("method of name---------///////////////////////"+method.getElementName());
			//addAllRelations(method);
		}	
	}
	/** 添加method信息到数据库 */
	public int addMethodInfo(IMethod method, MethodDeclaration methodDeclaration) throws Exception{
		if(!method.isConstructor()){
			ActionsAboutDB actionsAboutDB = new ActionsAboutDB();
			int maxTableRow = actionsAboutDB.getTableMaxRow(2)+1;
			String methodName = method.getElementName();
			String methodOfClass = method.getDeclaringType().getFullyQualifiedName();
			
			String methodParameters = methodParameters(methodDeclaration);
			MethodInfo methodInfo = new MethodInfo(maxTableRow, methodName, methodParameters, methodOfClass);
			int i = actionsAboutDB.insertMethodInfo(methodInfo);
			return i;
		}
		else
			return 0;
		
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addAllRelations() throws JavaModelException{
		System.out.println("start into relations-----------");
		 for(int j = 0 ;j < types.size();j ++){
			 IType type = types.get(j);
			 // 这里是针对每一个类中的每一个函数进行处理，对每个函数都会新构造一个OneMethodAboutRelationedClass
			 for(IMethod method : type.getMethods()){
				 if(!method.isConstructor()){	// 过滤掉构造函数
					 OneMethodAboutRelationedClass relatedClass = new OneMethodAboutRelationedClass();
					 // 传入参数是每个method和它所在类
					 relatedClass.getRelationsClass(type, method);
					 List<String> classNames = new ArrayList<String>();
					 classNames = relatedClass.methodAndItsdestinations.get(method);
					 Entity entity = new Entity(method);
					 MethodDeclaration methodDeclaration = (MethodDeclaration) entity.getAssociatedNode();
					 if(!classNames.isEmpty())
						try {
							addRelations(method, methodDeclaration, classNames);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} 
				 }
			 }
		 }
	}
	public void addRelations(IMethod method, MethodDeclaration methodDeclaration, List<String> classNames) throws Exception{

		ActionsAboutDB actionsAboutDB = new ActionsAboutDB();
		String parameters = methodParameters(methodDeclaration);
		// 三个值分别对应methodName,parameters,className
		int methodId = actionsAboutDB.getRelationsMethodID(method.getElementName(), parameters, method.getDeclaringType().getFullyQualifiedName());
		int classItselfId = actionsAboutDB.getRelationsClassID(method.getDeclaringType().getFullyQualifiedName());
		int maxTableRow = actionsAboutDB.getTableMaxRow(1)+1;
		System.out.println("methodid----------"+methodId);
		Relations relations1 = new Relations(maxTableRow, methodId,classItselfId,0);	// 这条记录是这个方法和它所在的类的，最后一个值为0表示这个方法在这个类中
		
		if(methodId != 0)
			actionsAboutDB.insertRelations(relations1);
		maxTableRow++;
		if(methodId !=0 && !classNames.isEmpty())
			for(String className :classNames){
				//System.out.println("ClassQualifiedName -------------------------" + className);
				int classId = actionsAboutDB.getRelationsClassID(className);
				//System.out.println("classId*********************"+ classId);
				if(classId != 0){
					Relations relations = new Relations(maxTableRow, methodId,classId,1); // 这条记录是这个方法和之前获得的classNames的，最后一个值为1表示这个方法不在这个类中
					actionsAboutDB.insertRelations(relations);
					maxTableRow++;
				}
			}
	}

	/** 获取method的参数的QualifiedName（如java.util.Collection），并返回成一串字符串，每个name中间用逗号隔开 */
	public String methodParameters(MethodDeclaration methodDeclaration){
		ITypeBinding[] parameters = methodDeclaration.resolveBinding().getParameterTypes();
		List<String> parameterList = new ArrayList<String>();
		if(parameters.length!=0)
			for (ITypeBinding parameter : parameters){
				parameterList.add(parameter.getQualifiedName());
				//System.out.println("parameters------------"+parameter.getQualifiedName());
			}
		StringBuilder sb = new StringBuilder();
		if(!parameterList.isEmpty()){
			for(String parameter : parameterList)
				sb.append(parameter).append(",");
		}
		else
			sb.append("0");
		
		return sb.toString();
	}

}
