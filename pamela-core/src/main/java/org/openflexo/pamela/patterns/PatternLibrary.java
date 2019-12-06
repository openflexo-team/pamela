package org.openflexo.pamela.patterns;

import java.util.ArrayList;

public class PatternLibrary {

    public static ArrayList<Class> getClassHierarchy(Class baseClass){
        ArrayList<Class> returned = new ArrayList<>();
        Class currentClass = baseClass;
        while (currentClass != null){
            returned.add(currentClass);
            PatternLibrary.searchInterfaces(currentClass, returned);
            currentClass = currentClass.getSuperclass();
        }
        return returned;
    }

    private static void searchInterfaces(Class baseClass, ArrayList<Class> list){
        for (Class interf : baseClass.getInterfaces()){
            list.add(interf);
            searchInterfaces(interf, list);
        }
    }
}
