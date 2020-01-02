package en.actionsofproject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import en.actionsofproject.SearchAllTranferClass.VisitorForMethod;
import en.entitys.Entity;
import en.movemethod.MoveMethodNode;

public class OneMethodAboutRelationedClass {
	
	public List<ITypeBinding> typeBindingCanMove = new ArrayList<>();//the classes can be moved
	public List<MoveMethodNode> refactorNodes = new ArrayList<>();//�м��������ƶ�����Ŀ�ĵ��࣬���м����ڵ�
	public List<IVariableBinding> IVarbindsCanMove = new ArrayList<>();
	public Map<IMethod, List<IVariableBinding>> methodAndTargets = new HashMap();
	public Map<IMethod, List<String>> methodAndItsdestinations = new HashMap();
	public TypeDeclaration typeDeclarationOfCurrentClass;
	Map<IMethod, IVariableBinding> methodAndTarget = new HashMap();
	List<String> classNames = new ArrayList<String>();
	public ITypeBinding currentClass;
	public IMethod currentIMethod;
	
	public void getRelationsClass(IType type, IMethod method){
		Entity entity = new Entity(type);
		// 由于这是一个类，getTypeDeclaration()和getAssociatedNode()是一样的
		typeDeclarationOfCurrentClass = entity.getTypeDeclaration();
		currentClass = typeDeclarationOfCurrentClass.resolveBinding();
		
		//System.out.println("method name --------------------"+method.getElementName());
		Entity methodEntity = new Entity(method);
		MethodDeclaration methodDeclaration = (MethodDeclaration) methodEntity.getAssociatedNode();
		if(methodDeclaration != null){
			IVarbindsCanMove.clear();
			calVarbingdsCanMove(methodDeclaration, method);
			currentIMethod = method;
			VisitorForMethod vis = new VisitorForMethod();
			typeDeclarationOfCurrentClass.accept(vis); // accept(ASTVisitor visitor),允许这一visitor访问该节点
			
			// 将method的参数的className存到classNames
			classNames.clear();
			for(int j = 0; j < IVarbindsCanMove.size(); j++){
				String className = IVarbindsCanMove.get(j).getType().getQualifiedName();
				//System.out.println("relations-----------------"+method.getElementName()+"--------------"+IVarbindsCanMove.get(j).getType().getQualifiedName());
				if(!classNames.contains(className))
					classNames.add(className);
			}
			/** 为什么这么做？classNames只是它对应的参数呀，而且没必要使用map，因为不是只有一个method吗 */
			methodAndItsdestinations.put(method, classNames);
		}
	}
	
	/** 对于该方法的所有参数并且类型不是当前class所在的这个类的，添加这个变量的binding到IVarbindsCanMove，添加构造的MoveMethodNode节点到refactorNodes */
	public void calVarbingdsCanMove(MethodDeclaration method, IMethod currentIMethod){
		
		// 获得method的传入传出参数
		List<SingleVariableDeclaration> parameters = (List<SingleVariableDeclaration>)method.parameters();
		if(parameters == null || parameters.size() == 0){
			return;
		}
		List<IVariableBinding> targets = new ArrayList<>();
		boolean canMove = false;
		// 对于每个参数
		for(Iterator<SingleVariableDeclaration> it = parameters.iterator(); it.hasNext(); ){
			/*ITypeBinding: A type binding represents fully-resolved type, ex. a class - represents the class declaration; possibly with type parameters; a type variable - represents the declaration of a type variable
			  IVariableBinding: A variable binding represents either a field of a class or interface, or a local variable declaration (including formal parameters, local variables, and exception variables).
			  https://www.ibm.com/support/knowledgecenter/SSCLKU_7.5.5/org.eclipse.wst.jsdt.doc/reference/api/org/eclipse/wst/jsdt/core/dom/ITypeBinding.html
			    什么，你说你还是不知道为什么这里要resolveBinding喔，那你他娘的不会自己去查啊？*/
			IVariableBinding bind = it.next().resolveBinding();
			
			// isFromSource检测是源码中定义的类（即用户自定义的类型）并且不是当前所在的class这个类
			if(bind.getType().isFromSource() && !bind.getType().equals(currentClass)){
				// 添加这个变量的binding到targets，添加这个变量的种类到typeBindingCanMove
				targets.add(bind);
				if(!typeBindingCanMove.contains(bind.getType()))
					typeBindingCanMove.add(bind.getType());
				
				canMove = true;
				boolean visited = false;
				// 这个循环的目的是看这个变量的种类是否已经在refactorNodes中，如果有了，就标记为已访问，并将这个bind加入到这个refactorNodes中的variableBindings（第一次执行时，refactorNodes是空的，然后随着遍历各个方法的参数而往里添加）
				for(MoveMethodNode node : refactorNodes){
					if(node.typeBinding.equals(bind.getType())){
						visited = true;
						node.variableBindings.add(bind);
						break;
					}
				}
				// 如果这个变量的种类还不在refactorNodes中，加进去
				if(visited == false){ 
					MoveMethodNode node2 = new MoveMethodNode();
					node2.typeBinding = bind.getType();	
					List<IVariableBinding> vars = new ArrayList<>();
					vars.add(bind); // 这个参数的类型
					node2.variableBindings = vars; // 这个参数的bind
					node2.method = currentIMethod;
					node2.targetTypeName = bind.getName(); // 类型的名字
					refactorNodes.add(node2);
				}
			}
		}
		if(canMove == false)
			return;
		IVarbindsCanMove.addAll(targets);
	}
	class VisitorForMethod extends ASTVisitor{
		
		public boolean visit(TypeDeclaration node){
			if(!node.resolveBinding().equals(currentClass)){
				return false;
			}
			return true;
		}
		public boolean visit(EnumDeclaration node){
			return false;
		}
		
		public boolean visit(EnumConstantDeclaration node){
			return false;
		}
		
		public boolean visit(FieldDeclaration node){
				for (Object obj: node.fragments()) {  
		            VariableDeclarationFragment v = (VariableDeclarationFragment)obj;  
		            if(v.resolveBinding().getType().isFromSource() && !v.resolveBinding().getType().equals(currentClass)){
		            	IVarbindsCanMove.add(v.resolveBinding());
		            	MoveMethodNode node2 = new MoveMethodNode();
		            	
		            	if(v.resolveBinding().getType().isEnum())
		            		return true;
		            	if(!typeBindingCanMove.contains(v.resolveBinding().getType())){
		            		typeBindingCanMove.add(v.resolveBinding().getType());
		            	}
		            	
		            	
		            	boolean visited = false;
		            	for(MoveMethodNode nod: refactorNodes){
		            		if(nod.typeBinding.equals(v.resolveBinding().getType())){
		            			visited = true;
		            			nod.variableBindings.add(v.resolveBinding());
		            			break;
		            		}
		            	}
		            	
		            	if(visited == false){
		            		node2.typeBinding = v.resolveBinding().getType();
							List<IVariableBinding> vars = new ArrayList<>();
							vars.add(v.resolveBinding());
							node2.variableBindings = vars;
							node2.method = currentIMethod;
							node2.targetTypeName = v.resolveBinding().getType().getName();
							refactorNodes.add(node2);
		            	}
		            }
		        }
			return true;
		}
	}
}
