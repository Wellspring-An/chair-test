package com.chair.chairdada;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CodeTest {

    @Test
    public void test() {
        List<String> listId = new ArrayList<>();

        listId.add("1");
        listId.add("2");
        listId.add("3");
        listId.add("3");

        Set<String> setId = new HashSet<>(listId);
        System.out.println(setId);
    }
}
