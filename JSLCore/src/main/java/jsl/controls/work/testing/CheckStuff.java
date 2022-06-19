package jsl.controls.work.testing;

public class CheckStuff {

    public static void main(String[] args) {
        Class<?> xClass = java.lang.Long.class;
        Class<?> yClass = java.lang.Long.class;
        if (xClass.isAssignableFrom(yClass)){
            System.out.println("isInstance true");
        } else {
            System.out.println("isInstance false");
        }
    }
}
