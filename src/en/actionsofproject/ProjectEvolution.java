package en.actionsofproject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import en.actions.ExtractClassName;
import en.actionsofproject.database.ActionsAboutDB;
import en.actionsofproject.database.ui.ClassInfo;
import en.entitys.Entity;


/**
 * ProjectEvolution：进行项目的处理，提取当前项目中的methodclass和其对应的class，并将class插入数据库
 * */

public class ProjectEvolution {
	
	// 针对这个项目进行处理
	protected IJavaProject iJavaproject = null;
	protected IProject iProject = null;
	// ICompilationUnit表示Java源(.java)文件
	public List<ICompilationUnit> compilationUnits = new ArrayList<ICompilationUnit>();
	// IType表示class
	public List<IType> types = new ArrayList<IType>();
	public List<IMethod> allMethods = new ArrayList<IMethod>();
	public Map<IMethod, IType> MethodAndItsClass = new HashMap<IMethod, IType>();
	public Map<IMethod,MethodDeclaration> methodAndItsMehthodDeclaration = new HashMap<IMethod, MethodDeclaration>();
	
	public ProjectEvolution(IJavaProject selectedProject, IProject selectedIProject){
		this.iJavaproject = selectedProject;
		this.iProject = selectedIProject;
		compilationUnits = getAllCompilationUnits();
		
		}
	public void run() throws Exception{
//		init();
		long begin = System.currentTimeMillis();
		ExtractClassName.consoleStream.println("----Get All Types And All Methods");
		
		getAllITypesAndAllIMethods();
		
//		print();
//		writeNameList();
		System.out.println("all class's sum ------" + types.size());
		ExtractClassName.consoleStream.println("------All class's sum: " + types.size());
		ExtractClassName.consoleStream.println("----Insert Classinfo Into DB");
		
		insertClassinfoIntoDB();
		
		System.out.println("all method's sum ------" + allMethods.size());
		ExtractClassName.consoleStream.println("------All method's sum: " + allMethods.size());
		ExtractClassName.consoleStream.println("----Get All Method And All Method Declaration");
		
		GetAllIMethodAndAllMethodDeclaration();
		
		// 更新class中的allMethod信息
		ActionsAboutDB actionsAboutDB = new ActionsAboutDB();
		actionsAboutDB.addAllMethods();
		
		MethodAndItsRelationedClass relatedClass = new MethodAndItsRelationedClass(types, allMethods);
		ExtractClassName.consoleStream.println("----Add All Relations");
		
		relatedClass.addAllRelations();
		
//		MethodAndItsRelationedClass relatedClass = new MethodAndItsRelationedClass(types, allMethods);
//		for(int i = 0; i < types.size(); i++){
//			System.out.println("the " + i + " class------------------------------");
//			IType type= types.get(i);
//			try {
//				if(type.isClass()){
//					relatedClass.getRelationsClass(types.get(i));
//				}
//			} catch (JavaModelException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		System.out.printf("took %.4f seconds", (System.currentTimeMillis()-begin)/1000.0);
	}
	void init(){
		MethodAndItsClass.clear();
		allMethods.clear();
		methodAndItsMehthodDeclaration.clear();
		types.clear();
	}
	
	/* 获取项目中所有的class（即types）和methods，并将其赋值给types，allMethods，MethodAndItsClass */
	public void getAllITypesAndAllIMethods(){
		for(ICompilationUnit compilationUnit : compilationUnits){
			if(!compilationUnit.exists())
				continue;
			IType[] classes = null;
			try {
				classes = compilationUnit.getTypes();
			} catch (JavaModelException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			for(IType type : classes){
				try {
					if(type.isClass()){
						types.add(type);
						IMethod[] approches = null;
						approches = type.getMethods();
						for(IMethod method : approches){
							
							allMethods.add(method);
							MethodAndItsClass.put(method, type);
						}	
					}
				} catch (JavaModelException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		}	
	}
	
	/* 获得类的名字并将其插入数据库 */
	public void insertClassinfoIntoDB() throws Exception{
		
		ActionsAboutDB actionsAboutDB = new ActionsAboutDB();
		// maxTableRow为当前数据库中最大的classID+1，作为新一个class的id值
		int maxTableRow = actionsAboutDB.getTableMaxRow(3)+1;
		System.out.println("maxTableRow--------"+maxTableRow);
		for(int i = 0; i<types.size(); i++){//insert classinfo into DB
			IType type= types.get(i);
			try {
				if(type.isClass()){
					// 获得类全名，包含根目录
					String classQualifiedName = type.getFullyQualifiedName();
					// 获得类的名字
					String className = type.getElementName();
					System.out.println("classQualifiedName-----------"+classQualifiedName);
					try {
						
						ClassInfo classinfo = new ClassInfo(maxTableRow, classQualifiedName, className);
						int x = actionsAboutDB.insertClassInfo(classinfo);
						if(x==1)
							maxTableRow++;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("maxTableRow-------------"+maxTableRow);
		}
	}
	
	/* 获取method的Declaration（method类为entity） */
	public void GetAllIMethodAndAllMethodDeclaration() throws Exception{
		for(IMethod method : allMethods){
			System.out.println("method of name---------///////////////////////"+method.getElementName());
			// 构建entity
			Entity entity = new Entity(method);
			// 获得declaration
			MethodDeclaration methodDeclaration = (MethodDeclaration) entity.getAssociatedNode();
			// 添加method信息到数据库
			MethodAndItsRelationedClass relatedClass = new MethodAndItsRelationedClass(types, allMethods);
			relatedClass.addMethodInfo(method, methodDeclaration);
			
			
//			methodAndItsMehthodDeclaration.put(method, methodDeclaration);
		}
	}
	
//	public void writeNameList(){
//		String filePath = "D:\\PrintNameList.xlsx";
//		try {
//			FileOutputStream output = new FileOutputStream(filePath);
//			XSSFWorkbook workbook = new XSSFWorkbook();
//			for(int sheetIndex = 0; sheetIndex < 1; sheetIndex++){
//				XSSFSheet sheet = workbook.createSheet("sheet" + sheetIndex);				
//				for(int rowIndex = 0; rowIndex < allMethods.size(); rowIndex++){				
//					XSSFRow row = sheet.createRow(rowIndex);					
//					if(rowIndex == 0){
//						row.createCell(0).setCellValue("NUM");
//						row.createCell(1).setCellValue("ClassName");
//						row.createCell(2).setCellValue("ClassName");
//					}
//					else{
//						for (int j = 0; j < 3; j ++){
//							if(j == 0){
//								row.createCell(j).setCellValue(rowIndex);
//							}
//							else
//								if(j == 1){
//									IMethod method=allMethods.get(rowIndex);
//									row.createCell(j).setCellValue(method.getElementName());
//								}
//								else
//									if(j == 2 ){
//										IType type = MethodAndItsClass.get(allMethods.get(rowIndex));
//										row.createCell(j).setCellValue(type.getElementName());
//									}	 
//			    		 }	
//					}					
//					output.flush();
//				}
//			}
//			workbook.write(output);
//			output.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		 System.out.println("excel file generation");
//	}
	
	
	public void print(){
		for(IType type : types){
			System.out.println("type of name ---"+type.getElementName());
		}
		for(IMethod method : allMethods){
			System.out.println("method of name ---"+method.getElementName());
		}
	}
	
	/* 获取项目中的所有java源文件 */
	protected List<ICompilationUnit> getAllCompilationUnits() {
		List<ICompilationUnit> allCompilationUnits = new ArrayList<ICompilationUnit>();
		if (iJavaproject == null) {
			return allCompilationUnits;
		}

		IPackageFragment[] packageFragments = getPackageFragments(iJavaproject);
		if (packageFragments == null) {
			return allCompilationUnits;
		}

		for (IPackageFragment packageFragment : packageFragments) {
			ICompilationUnit[] compilationUnits = getCompilationUnits(packageFragment);
			if (compilationUnits == null) {
				continue;
			}

			for (ICompilationUnit compilationUnit : compilationUnits) {
				if(!allCompilationUnits.contains(compilationUnit)){
					allCompilationUnits.add(compilationUnit);
				}
			}
		}
		return allCompilationUnits;
	}

	/* 获取一个项目中的所有package */
	private IPackageFragment[] getPackageFragments(IJavaProject javaProject) {
		IPackageFragment[] packageFragments = null;

		try {
			packageFragments = javaProject.getPackageFragments();
		} catch (JavaModelException e) {
			return null;
		}

		return packageFragments;
	}

	/* 获取一个package中的所有java源文件 */
	private ICompilationUnit[] getCompilationUnits(
			IPackageFragment packageFragment) {
		try {
			if (packageFragment.getKind() != IPackageFragmentRoot.K_SOURCE) {
				return null;
			}
		} catch (JavaModelException e) {
			return null;
		}

		ICompilationUnit[] compilationUnits = null;

		try {
			compilationUnits = packageFragment.getCompilationUnits();
		} catch (JavaModelException e) {
			return null;
		}

		return compilationUnits;
	}
	
	/* 不知道干啥的，反正也没用 */
	protected CompilationUnit createCompilationUnit(ICompilationUnit compilationUnit) {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
	//	parser.setProject(iJavaproject);
		parser.setSource(compilationUnit);
		parser.setProject(compilationUnit.getJavaProject());
		//Config.projectName=compilationUnit.getJavaProject().getElementName();
	//	System.out.println("��Ŀ���ƣ�"+compilationUnit.getJavaProject().getElementName());
		IPath path=compilationUnit.getPath();
		parser.setUnitName(path.toString());
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		CompilationUnit unit=null;
		try
		{
			unit= (CompilationUnit) parser.createAST(null);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return unit;
	}
	
}
