package jslx.tabularfiles;

public class Cell<T> {

    private T thing;

    T getValue(){return thing;}

    void setValue(T value){
        thing = value;
    }

    public Cell(T thing) {
        this.thing = thing;
    }

    public static void main(String[] args) {
        Cell<Boolean> cell = new Cell<>(false);

        Boolean aBoolean = cell.getValue();


    }
}
