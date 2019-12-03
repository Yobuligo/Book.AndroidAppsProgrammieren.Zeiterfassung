package com.yobuligo.zeiterfassung;

import java.util.ArrayList;

public class DemoData {
    private ArrayList<String> data = new ArrayList<>();

    public void addName(String name) {
        this.data.add(name);
    }

    public String getNamebyIndex(int index) {
        return data.get(index);
    }

    public int getSize() {
        return data.size();
    }
}
