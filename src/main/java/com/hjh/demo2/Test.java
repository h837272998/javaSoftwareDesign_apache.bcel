package com.hjh.demo2;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;

/**
 * @Description:
 * @Author: HJH
 * @Date: 2019-06-05 20:39
 */
public class Test {

    public static void main(String[] args) throws ClassNotFoundException {
        JavaClass clazz = Repository.lookupClass("target/classes/com/hjh/demo2/Name");
        ClassGen classGen = new ClassGen(clazz);
        ConstantPoolGen cPoolGen = classGen.getConstantPool();

    }
}
