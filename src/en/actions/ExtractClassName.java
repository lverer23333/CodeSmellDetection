package en.actions;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.packageview.PackageFragmentRootContainer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import en.actionsofproject.FeatureEnvyDetect;
import en.actionsofproject.ProjectEvolution;
import en.actionsofproject.database.ActionsAboutDB;
import en.actionsofproject.database.InsertDataIntoDistanceValue;
import en.actionsofproject.ep.AddEntityPlacementIntoDB;

/** 获取一些值->projectEvolution.run->InsertDataIntoDistance，项目入口点*/

public class ExtractClassName implements IWorkbenchWindowActionDelegate {
	public static MessageConsole console = null;
    public static MessageConsoleStream consoleStream = null;
    public static IConsoleManager consoleManager = null;
    public static final String CONSOLE_NAME = "Console";

	@Override
	public void run(IAction action) {
		
		// 加载控制台
    	consoleManager = ConsolePlugin.getDefault().getConsoleManager();
	    IConsole[] consoles = consoleManager.getConsoles();
	    if(consoles.length > 0){
	        console = (MessageConsole)consoles[0];
	    } else{
	        console = new MessageConsole(CONSOLE_NAME, null);
	        consoleManager.addConsoles(new IConsole[] { console });
	    }
	    consoleStream = console.newMessageStream();
	    ExtractClassName.consoleStream.println("\n\n************************ Featrue Envy Detection *********************");
	    ExtractClassName.consoleStream.println("**************************** Version:1.0.1 **************************");
	    ExtractClassName.consoleStream.println("********************** Recently Update:2020.4.8 *********************");
	    ExtractClassName.consoleStream.println("**** Beijing Institute of Technolog: College of Computer Science ****\n\n");
	    ExtractClassName.consoleStream.println("Featrue Envy Detection Start!");
	    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		String s = simpleDateFormat.format(date);
		ExtractClassName.consoleStream.println("Featrue Envy Detection Start time: " + s);
	    
		// Java插件中用于表示当前项目的IJavaProject和IProject
		final IJavaProject selectedProject = JavaCore.create(getProject());
		// 得到整个 Workspace 的根目录：
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject selectedIProject = null;
		// 获得当前RCP应用活动的workbenchWindow shell
		final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		if(selectedProject == null){
			// 如果没有选中的JavaProject，异步执行以下语句报错
			Display.getDefault().asyncExec(new Runnable() {
	            @Override
	            public void run() {
	            	MessageBox dialog=new MessageBox(shell,SWT.OK|SWT.ICON_INFORMATION);
	 		        dialog.setText("Warning"); 
	 		        dialog.setMessage("Please select a JavaProject to ExtractName!");
	 		        dialog.open();
	 		        return;
	            }
		 });
		}
		else{
			// System.out.println("Project   "+selectedProject.getElementName());
			for (IProject iProject : root.getProjects()) {
				if(iProject.getName().equals((selectedProject.getElementName()))){
					selectedIProject = iProject;
					break;
				}	
			 }		
			System.out.println("IProject's  name----"+selectedIProject.getName());
			
			// 建表
			ExtractClassName.consoleStream.println("--Restructuring Database...");
		    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			date = new Date();
			s = simpleDateFormat.format(date);
			ExtractClassName.consoleStream.println("--Database Refactor Start time: " + s);
			ExtractClassName.consoleStream.println("----Target database: root.featureenvy");
			ActionsAboutDB actionsAboutDB = new ActionsAboutDB();
			try {
				actionsAboutDB.createTables();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			ExtractClassName.consoleStream.println("--Database Restructured");
		    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			date = new Date();
			s = simpleDateFormat.format(date);
			ExtractClassName.consoleStream.println("--Database Refactor End time: " + s);
			
			// ep中自定义的类
			ExtractClassName.consoleStream.println("--Project Evolveing...");
		    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			date = new Date();
			s = simpleDateFormat.format(date);
			ExtractClassName.consoleStream.println("--Project Evolvlution Start time: " + s);
			
			ProjectEvolution projectEvolution = new ProjectEvolution(selectedProject, selectedIProject);
			try {
				projectEvolution.run();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			ExtractClassName.consoleStream.println("----Insert Data Into Distance Value");
			InsertDataIntoDistanceValue insertDataIntoDistanceValue = new InsertDataIntoDistanceValue();
			try {
				insertDataIntoDistanceValue.AddDistanceMatric(selectedProject);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ExtractClassName.consoleStream.println("--Project Evolved");
		    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			date = new Date();
			s = simpleDateFormat.format(date);
			ExtractClassName.consoleStream.println("--Project Evolvlution End time: " + s);
			
			ExtractClassName.consoleStream.println("--Detecting...");
		    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			date = new Date();
			s = simpleDateFormat.format(date);
			ExtractClassName.consoleStream.println("--Detection Start time: " + s);
			
			FeatureEnvyDetect featureEnvyDetect = new FeatureEnvyDetect();
			
			ExtractClassName.consoleStream.println("--Detected");
		    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			date = new Date();
			s = simpleDateFormat.format(date);
			ExtractClassName.consoleStream.println("--Detection End time: " + s);
			
			ExtractClassName.consoleStream.println("\nFeatrue Envy Detection End!");
			simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			date = new Date();
			s = simpleDateFormat.format(date);
			ExtractClassName.consoleStream.println("Featrue Envy Detection End time: " + s);
//			AddEntityPlacementIntoDB addEntityPlacementIntoDB = new AddEntityPlacementIntoDB();
//			try {
//				addEntityPlacementIntoDB.AddEntityPlacement(selectedProject);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		

	}
	
	/* 获取当前项目 */
	public IProject getProject(){  
		IProject project = null;  
		// IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();  
		
		// 获得当前RCP应用活动的workbenchWindow选中的选项（即选择的是哪个插件）
		ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();    
		ISelection selection = selectionService.getSelection();    
		if(selection instanceof IStructuredSelection) { 
			/**为什么FirstElement是类型？IStructuredSelection定义了什么?*/
			Object element = ((IStructuredSelection)selection).getFirstElement();    
			
			//IResource中定义了IProject, IFolder,IFile这些资源类型的通用操作接口
			if (element instanceof IResource) {    
				project= ((IResource)element).getProject();    
			} else if (element instanceof PackageFragmentRootContainer) {    
				IJavaProject jProject =     
						((PackageFragmentRootContainer)element).getJavaProject();    
				project = jProject.getProject();    
			} else if (element instanceof IJavaElement) {    
				IJavaProject jProject= ((IJavaElement)element).getJavaProject();    
				project = jProject.getProject();    
			}  
		}     
		
		return project;  
	} 

	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IWorkbenchWindow arg0) {
		// TODO Auto-generated method stub

	}

}
