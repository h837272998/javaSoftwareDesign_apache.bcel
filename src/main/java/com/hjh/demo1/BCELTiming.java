package com.hjh.demo1;


import com.sun.org.apache.bcel.internal.Constants;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @Description: 给指定类的指定方法添加运行时间
 * @Author: HJH
 * @Date: 2019-06-04 15:58
 */
public class BCELTiming {
    /**
     * @Description:
     * @Author: HJH
     * @Date: 2019-06-04 15:59
     * @Param: []
     * @Return: void
     */
    private static void addWrapper(ClassGen classGen, Method method){
        //set up the construction tools
        InstructionFactory instructionFactory = new InstructionFactory(classGen); //指令工厂
        InstructionList instructionList = new InstructionList();  //指令列表
        ConstantPoolGen poolGen = classGen.getConstantPool(); //常量池
        String className = classGen.getClassName(); //获得类名
        MethodGen wrapGen = new MethodGen(method,className,poolGen);  //创建包装方法（原方法）
        wrapGen.setInstructionList(instructionList);

        //rename a copy of the original method
        MethodGen methodGen = new MethodGen(method,className,poolGen);  //"复制"原方法
        classGen.removeMethod(method);  //删除原方法
        String iname = methodGen.getName() + "$impl";
        methodGen.setName(iname);
        classGen.addMethod(methodGen.getMethod()); //添加原方法 （改名：原+$impl）
        Type result = methodGen.getReturnType(); //获得原方法的返回类型

        //compute the size of the calling parameters
        Type[] types = methodGen.getArgumentTypes();  //方法输入参数类型
        int slot = methodGen.isStatic() ? 0:1;  //堆栈帧槽
        for (int i = 0; i < types.length; i++) {
            slot += types[i].getSize();
        }

        //save time prior to invocation
        instructionList.append(instructionFactory.createInvoke(
                "java.lang.System", "currentTimeMillis", Type.LONG, Type.NO_ARGS,
                Constants.INVOKESTATIC));
        instructionList.append(InstructionFactory.createStore(Type.LONG,slot));

        //call the wrapped method
        int offset = 0 ;
        short invoke = Constants.INVOKESTATIC;
        if (!methodGen.isStatic()) {
            instructionList.append(InstructionFactory.createLoad(Type.OBJECT, 0));
            offset = 1;
            invoke = Constants.INVOKEVIRTUAL;
        }
        for (int i = 0; i < types.length; i++) {
            Type type = types[i];
            instructionList.append(InstructionFactory.createLoad(type, offset));
            offset += type.getSize();
        }
        instructionList.append(instructionFactory.createInvoke(className, iname, result, types, invoke));

        // store result for return later
        if (result != Type.VOID) {
            instructionList.append(InstructionFactory.createStore(result, slot + 2));
        }

        // print time required for method call
        instructionList.append(instructionFactory.createFieldAccess("java.lang.System", "out", new ObjectType("java.io.PrintStream"),
                Constants.GETSTATIC));
        instructionList.append(InstructionConst.DUP);
        instructionList.append(InstructionConst.DUP);
        String text = "Call to method " + methodGen.getName() + " took ";
        instructionList.append(new PUSH(poolGen, text));
        instructionList.append(instructionFactory.createInvoke("java.io.PrintStream", "print", Type.VOID, new Type[] { Type.STRING },
                Constants.INVOKEVIRTUAL));
        instructionList.append(instructionFactory.createInvoke("java.lang.System", "currentTimeMillis", Type.LONG, Type.NO_ARGS,
                Constants.INVOKESTATIC));
        instructionList.append(InstructionFactory.createLoad(Type.LONG, slot));
        instructionList.append(InstructionConst.LSUB);
        instructionList.append(instructionFactory.createInvoke("java.io.PrintStream", "print", Type.VOID, new Type[] { Type.LONG },
                Constants.INVOKEVIRTUAL));
        instructionList.append(new PUSH(poolGen, " ms."));
        instructionList.append(instructionFactory.createInvoke("java.io.PrintStream", "println", Type.VOID, new Type[] { Type.STRING },
                Constants.INVOKEVIRTUAL));

        // return result from wrapped method call
        if (result != Type.VOID) {
            instructionList.append(InstructionFactory.createLoad(result, slot + 2));
        }
        instructionList.append(InstructionFactory.createReturn(result));

        // finalize the constructed method
        wrapGen.stripAttributes(true);
        wrapGen.setMaxStack();
        wrapGen.setMaxLocals();
        classGen.addMethod(wrapGen.getMethod());
        instructionList.dispose();
    }

    public static void main(String[] argv) {
        // if (argv.length == 2 && argv[0].endsWith(".class")) {
        //argv[0] = "target/classes/com/hjh/demo1/StringBuilder.class";
        String classPath = "target/classes/com/hjh/demo1/StringBuilder.class";
        String methodName = "buildString";
        try {
            JavaClass jclas = new ClassParser(classPath).parse();
            ClassGen cgen = new ClassGen(jclas);
            Method[] methods = jclas.getMethods();
            for (int i = 0; i < methods.length; i++) {
                System.out.println(methods[i].toString());
            }
            int index;
            for (index = 0; index < methods.length; index++) {
                if (methods[index].getName().equals(methodName)) {
                    break;
                }
            }
            if (index < methods.length) {
                addWrapper(cgen, methods[index]);
                FileOutputStream fos = new FileOutputStream(classPath);
                cgen.getJavaClass().dump(fos);
                System.out.println("finish");
                fos.close();
            } else {
                System.err.println("Method " + argv[1] + " not found in " + argv[0]);
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }

    }
}
