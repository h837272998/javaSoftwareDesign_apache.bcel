package com.hjh.demo1;

/**
 * @Description:
 * @Author: HJH
 * @Date: 2019-06-04 15:52
 */
public class StringBuilder {
    private String buildString(int length){
        String result = "";
        for (int i = 0;i<length;i++){
            result += (char)(i%26+ 'a');
        }
        return result;
    }

    public static void main(String[] args) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            String result = stringBuilder.buildString(Integer.parseInt(args[i]));
            System.out.println("第" + (i + 1) + "个输入生成的字符串长度: "
                    + result.length());
        }
    }
}
