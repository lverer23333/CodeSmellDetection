package en.movemethod;

import java.util.List;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

public class MoveMethodNode {
	
	//这个方法
	public IMethod method; 
	//这个方法的某一个参数，具体看OneMethodAboutRelationedClass对这个的定义：typeBinding = bind.getType();	
	public ITypeBinding typeBinding; 
	//同一个类型的变量的binding，比如一个方法的传入参数是A和B，返回值类型是A，那么其中一个A对应的MoveMethodNode节点中的这个值就会有两个A的binding，一个传入参数的A一个传出参数的A
	public List<IVariableBinding> variableBindings = null;	
	public String targetTypeName; 
	public List<String> relationsClasses; //
	public MoveMethodNode(){
		setRelationsClasses(relationsClasses);
	}
	public List<String> getRelationsClasses() {
		return relationsClasses;
	}
	
	/*别看这垃圾函数了。你是不是以为它想将variableBindings中的类名加到relationsClasses？你高看它了，这代码比你想的蠢多咯！
	 * 这里就是单纯的屁用都没有，因为构造函数在调用这个方法时variableBindings都是空，不过没差，反正这个relationsClasses根本没有用到过！ */
	public void setRelationsClasses(List<String> relationsClasses) {
		if(variableBindings != null)
			for(IVariableBinding iVariableBinding : variableBindings){
				String className = iVariableBinding.getType().getQualifiedName();
				if(!relationsClasses.contains(className))
					relationsClasses.add(className);
			}
		this.relationsClasses = relationsClasses;
	}
}
