package com.quick.dynamic.util;

import java.lang.reflect.Array;

public class ArrayUtil {

    public static Object combineArrayItem(Object array, Object item) {
        return combineArray(array, new Object[]{item});
    }

    public static Object combineArray(Object arr1, Object arr2) {
        if (!arr1.getClass().isArray() || !arr2.getClass().isArray()) {
            throw new RuntimeException();
        }

        int arr1Length = Array.getLength(arr1);
        int arr2Length = Array.getLength(arr2);
        int totalLength = arr1Length + arr2Length;
        Object resultArr = Array.newInstance(arr1.getClass().getComponentType(), totalLength);

        for (int i = 0; i < totalLength; i++) {
            if (i < arr1Length) {
                Array.set(resultArr, i, Array.get(arr1, i));
            } else {
                int index = i - arr1Length;
                Array.set(resultArr, i, Array.get(arr2, index));
            }
        }

        return resultArr;
    }
}
