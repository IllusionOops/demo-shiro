package com.wyj.test;

import com.wyj.utils.ArraySortUtil;

/**
 * @ClassName SortTest
 * @Description: TODO
 * @Author yjwu
 * @Date 2020/1/2 17:23
 **/
public class SortTest {
    public static void main(String[] args) {
        int[] arr=new int[]{9,8,7,6,5,4,3,2,1,0};
        ArraySortUtil.quickSort(arr,0,9);
        for (int i:
             arr) {
            System.out.print(i+",");
        }
    }
}
