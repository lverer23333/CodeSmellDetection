package en.entitys;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;


/** 对类或方法进行class的定义 */
public class Entity {
	private CompilationUnit unit;
	private ICompilationUnit iUnit;
	
	private ASTNode associatedNode = null;
	private IResource resource;
	private TypeDeclaration typeDeclaration;
	//private IType type;
	private List<IMethod> methods = new ArrayList<IMethod>();
	
	public List<IMethod> getMethods() {
		return methods;
	}
	public void setMethods(List<IMethod> methods) {
		this.methods = methods;
	}
	public Entity(IJavaElement element){
		//this.setType(element);
		setCompilationUnit(element);
		setAssociatedNode(element, unit);	
		setMethods(element);
	}
	public CompilationUnit getUnit() {
		return unit;
	}
	public void setUnit(CompilationUnit unit) {
		this.unit = unit;
	}
	public ICompilationUnit getiUnit() {
		return iUnit;
	}
	public void setiUnit(ICompilationUnit iUnit) {
		this.iUnit = iUnit;
	}
	public IResource getResource() {
		return resource;
	}
	public void setResource(IResource resource) {
		this.resource = resource;
	}
	
	/* 针对类，获取该类中所有方法并将其存在methods中 */
	private void setMethods(IJavaElement element){
		if(element instanceof IType){
			IType type = (IType) element;
			IMethod[] approches = null;
			try {
				approches = type.getMethods();
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(IMethod method : approches)
				methods.add(method);	
		}
		
	}
	
	/* 获得element所在的源文件，赋值iUnit；解析该源文件，赋值unit；获得该文件的UnderlyingResource并赋值resource */
	private void setCompilationUnit(IJavaElement element){
		if(element instanceof IMember){
			IMember member = (IMember)element;
			// 获取这个element所在的ICompilationUnit（.java源文件）
			ICompilationUnit iUnit = member.getCompilationUnit();
			// 构建解析器，解析的表示为unit
			ASTParser parser = ASTParser.newParser(AST.JLS4);
			parser.setSource(iUnit);
			parser.setResolveBindings(true);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			CompilationUnit unit = (CompilationUnit) parser.createAST(null);
			this.setUnit(unit);
			this.setiUnit(iUnit);
			
			// getUnderlyingResource()方法返回一个IResource，包含它基于的文件的文件名和路径的各种形式是什么等等
			try {
				this.setResource(member.getUnderlyingResource());
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/* 对于class，获取Declaration并存在associatedNode和typeDeclaration中；对于method，获取其methodDec并存在associatedNode，再将它所在的类的Declaration存到typeDeclaration */
	private void setAssociatedNode(IJavaElement element, CompilationUnit unit){
		// 这个element是个class
		if(element instanceof IType){
			IType type = (IType) element;
			List<TypeDeclaration> typeDeclarations = unit.types();
			for(TypeDeclaration  typeDeclarationx : typeDeclarations){
				if(typeDeclarationx.getName().toString().equals(type.getElementName())){
					this.associatedNode = typeDeclarationx;
					this.setTypeDeclaration(typeDeclarationx);
					break;
				}
			}
		// 这个element是个method
		}else if(element instanceof IMethod){
			IMethod method = (IMethod) element;
			// 获得method所在的class
			IType type = method.getDeclaringType();
			
			List<TypeDeclaration> typeDeclarations = unit.types();
			for(TypeDeclaration  typeDeclarationx : typeDeclarations){
				if(typeDeclarationx.getName().toString().equals(type.getElementName())){
					
					MethodDeclaration[] methodDecs = typeDeclarationx.getMethods();
					if(methodDecs.length != 0)
						for(MethodDeclaration methodDec:methodDecs){
							 if(((IMethod)methodDec.resolveBinding().getJavaElement()).equals(method)){
								 this.associatedNode = methodDec;
								 break;
							 }
						}
					this.setTypeDeclaration(typeDeclarationx);
					break;
				}
			}
		}
	}
	
	
	private CompilationUnit setCompilationUnit(IMethod element){
		IMethod method = (IMethod)element;
		ICompilationUnit iUnit = method.getCompilationUnit();
		//IType belongType = member.getDeclaringType();
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(iUnit);
		parser.setResolveBindings(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		CompilationUnit unit1 = (CompilationUnit) parser.createAST(null);
		
		return unit1;
	}
	public ASTNode getAssociatedNode() {
		return associatedNode;
	}
	public void setAssociatedNode(ASTNode associatedNode) {
		this.associatedNode = associatedNode;
	}
	public TypeDeclaration getTypeDeclaration() {
		return typeDeclaration;
	}
	public void setTypeDeclaration(TypeDeclaration typeDeclaration) {
		this.typeDeclaration = typeDeclaration;
	}
//	public IType getType() {
//		return type;
//	}
//	public void setType(IJavaElement element) {
//			type = (IType) element;
//	}

}
