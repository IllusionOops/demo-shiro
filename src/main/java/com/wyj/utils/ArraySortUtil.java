package com.wyj.utils;

/**
 * @ClassName ArraySortUtil
 * @Description: TODO
 * @Author yjwu
 * @Date 2020/1/2 15:38
 **/
public  class ArraySortUtil {

    public static void BubbleSort(int[] arr) {
        if (arr.length == 0) {
            return;
        }
        int temp = 0;
        for (int i = 0; i > arr.length - 1; i++) {
            for (int j = 0; j < arr.length - 1 - i; j++) {
                if (arr[j] > arr[j + 1]) {
                    temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }


    public static void selectionSort(int[] arr) {
        if (arr.length == 0) {
            return;
        }
        int minIndex = 0;
        int temp = 0;
        for (int i = 0; i < arr.length; i++) {
            minIndex = i;
            for (int j = i; j < arr.length; j++) {
                if (arr[j] < arr[minIndex]) {
                    minIndex = j;
                }
            }
            temp = arr[minIndex];
            arr[minIndex] = arr[i];
            arr[i] = temp;
        }
    }


    public static void insertionSort(int[] arr) {
        if (arr.length == 0) {
            return;
        }
        int current;
        int preIndex;
        for (int i = 1; i < arr.length; i++) {
            //i-1 i i+1
            current = arr[i];
            preIndex = i - 1;
            while (current < arr[preIndex] && preIndex >= 0) {
                arr[preIndex + 1] = arr[preIndex];
                preIndex--;
            }
            arr[preIndex + 1] = current;
        }
    }


    public static void quickSort(int[] arr, int left, int right) {
        int begin = left, end = right, key = right, temp;
        if (left>right){
            return;
        }
        while (begin < end) {
            //从左往右，是否小于arr[key]
            while (begin < end && arr[begin] <= arr[key]) {
                begin++;
            }
            //从右往左
            while (begin < end && arr[end] > arr[key]) {
                end--;
            }
            //此时，左右两边都找到了不合要求的数，交换位置
            if (begin < end) {
                temp = arr[end];
                arr[end] = arr[begin];
                arr[begin] = temp;
            }
        }
        //此时，begin和end重合。交换基准为begin和end位置的数字交换。
        System.out.println(begin+","+end);
        arr[right] = arr[begin];
        arr[begin]=arr[key];
        //
        quickSort(arr, left, begin - 1);
        quickSort(arr, begin + 1, right);
    }

}
