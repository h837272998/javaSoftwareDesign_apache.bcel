package com.hjh.demo2;

import sun.applet.Main;

/**
 * @Description:
 * @Author: HJH
 * @Date: 2019-06-05 20:28
 */
public class Name {

    public void getName(){
        System.out.println("print Name");
    }

    public static void main(String[] args) {
        Name name = new Name();
        name.getName();
    }
}
