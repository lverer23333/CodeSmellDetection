package en.actionsofproject;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.core.DocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;


public class CreateTargetField {

	private static ICompilationUnit iUnit;
	private static CompilationUnit unit;//�?在的单元
	public static IDocument doc;
	static String targetClassName;
	private static ITypeBinding targetTypeBinding;
	private static TypeDeclaration typeDeclaration;
	private static String typeDeclarationName;
	private static MethodDeclaration methodDeclaration;
	private static String newFiledName;
	private static IVariableBinding newFiledVariableBinding;
	
	
	public static IVariableBinding startCreateTarget(CompilationUnit unitx, TypeDeclaration typeDeclarationx, MethodDeclaration methoddeclaration, ITypeBinding typeTarget){
		IMethod method = (IMethod)methoddeclaration.resolveBinding().getJavaElement();
		unit = unitx;
//		targetTypetBinding = typeTarget;
		targetTypeBinding = typeTarget;
		targetClassName = targetTypeBinding.getName();
		typeDeclaration = typeDeclarationx;
		typeDeclarationName = typeDeclaration.getName().toString();
		methodDeclaration = methoddeclaration;
		iUnit = method.getCompilationUnit();

		//要去加一个目标类字段声明
		createNewTargetField();
		
		//然后去找要移动的方法
		findTargetMethodAndTargetVariable();
		
		return newFiledVariableBinding;
		
	}
	
	
	private static void findTargetMethodAndTargetVariable(){
		ASTParser parser2 = ASTParser.newParser(AST.JLS4);
		parser2.setSource(iUnit); //iUnit就是�?个java文件
		parser2.setKind(ASTParser.K_COMPILATION_UNIT);
		parser2.setResolveBindings(true);
		parser2.setBindingsRecovery(false); 
		CompilationUnit unitx = (CompilationUnit) parser2.createAST(null);
		unitx.recordModifications();
		
		TypeDeclaration typeDeclaration = null;
		List<TypeDeclaration> typeDeclarations = unitx.types();
		for(TypeDeclaration  typeDeclarationx : typeDeclarations){
			if(typeDeclarationx.getName().toString().equals(typeDeclarationName)){
				typeDeclaration = typeDeclarationx;
				break;
			}
		}
		

		
		//ivariable
		for(FieldDeclaration fe: typeDeclaration.getFields()){
			for(Object obj: fe.fragments()){
				
				VariableDeclarationFragment v = (VariableDeclarationFragment)obj;
				if(v.getName().toString().equals(newFiledName)){
					newFiledVariableBinding = v.resolveBinding();
				}
			}
		}
	}
	
	private static boolean isSameMethod(MethodDeclaration m1, MethodDeclaration m2){
		
		if(!m1.getName().toString().equals(m2.getName().toString()))
			return false;
		//返回类型
		List<SingleVariableDeclaration> parameters = m1.parameters();
		List<String> parameters1 = new ArrayList<>();
		List<String> parameters2 = new ArrayList<>();
		
		for(SingleVariableDeclaration singleVariableDeclaration: parameters){
			parameters1.add(singleVariableDeclaration.getType().toString());
		}
		
		parameters = m2.parameters();
		for(SingleVariableDeclaration singleVariableDeclaration: parameters){
			parameters2.add(singleVariableDeclaration.getType().toString());
		}
		
		if(parameters1.size() != parameters2.size())
			return false;
		for(int i = 0; i < parameters1.size(); i++){
			if(!parameters1.get(i).equals(parameters2.get(i)))
				return false;
		}
		
		return true;
	}
	
	private static void createNewTargetField(){
		
		try {
			iUnit.becomeWorkingCopy(new NullProgressMonitor());
			IBuffer buffer = iUnit.getBuffer();
			doc = new DocumentAdapter(buffer);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		unit.recordModifications();
		
		
		AST ast = typeDeclaration.getAST();
		FieldDeclaration newField = fieldDecCreate(ast, createType(targetClassName, ast));
		typeDeclaration.bodyDeclarations().add(0, newField);
		
		boolean needCreateImport = true;
		String newImportName = targetTypeBinding.getQualifiedName();
		
		final List<ImportDeclaration> importDeclarations = new ArrayList<>();
		unit.accept(new ASTVisitor() {
			public boolean visit(TypeDeclaration node){
				if(!node.equals(typeDeclaration)){
					return false;
				}
				return true;
			}
			public boolean visit(ImportDeclaration node){
				importDeclarations.add(node);
				return true;
			}
		});
		
		
		//判断有没有想要的import语句
		for(ImportDeclaration imports: importDeclarations){
			if(imports.getName().toString().equals(newImportName)){
				needCreateImport = false;
				break;
			}
		}
		
		if(needCreateImport == true){
			ImportDeclaration im = ast.newImportDeclaration();
			im.setName(ast.newName(newImportName));
			unit.imports().add(im);
		}
		
		//下面这两个try顺序�?定不能颠倒，否则会出现IType �?  TypeDeclaration不同步的现象
		try {
			unit.rewrite(doc, JavaCore.getOptions()).apply(doc);
		} catch (MalformedTreeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (org.eclipse.jface.text.BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			iUnit.save(new NullProgressMonitor(), true);
			iUnit.commitWorkingCopy(true, new NullProgressMonitor());
		} catch (JavaModelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	
	private static Type createType(String typeName, AST ast){
		Type type = ast.newSimpleType(ast.newName(typeName));
		return type;
		
	}
	
	private static SimpleName VariableNameCreate(AST ast, Type targetClass){
		String name = targetClass.toString().toLowerCase();
		newFiledName = name;
		SimpleName variableName = ast.newSimpleName(name);
		return variableName;
	}
	
	private static FieldDeclaration fieldDecCreate(AST ast, Type targetClass){
		
		VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
		fragment.setName(VariableNameCreate(ast, targetClass));
		fragment.setInitializer(null);
		FieldDeclaration field = ast.newFieldDeclaration(fragment);
		field.setType(targetClass);
		Modifier fieldModifier = ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
		field.modifiers().add(fieldModifier);
		return field;
}

}
