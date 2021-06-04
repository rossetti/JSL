package examples.jobshop;

import jsl.simulation.ModelElement;
import jsl.modeling.elements.variable.ResponseVariable;

import java.util.Objects;

public class JobType extends ModelElement {

    private final String myName;

    private final Sequence mySequence;

    private final ResponseVariable mySystemTime;

    JobType(ModelElement parent, String name, Sequence sequence){
        super(parent);
        Objects.requireNonNull(name, "The name was null");
        Objects.requireNonNull(sequence, "The sequence was null");
        myName = name;
        mySequence = sequence;
        mySystemTime = new ResponseVariable(this, name + ":SystemTime");
    }

    public Sequence getSequence(){
        return mySequence;
    }

    public ResponseVariable getSystemTime(){
        return mySystemTime;
    }

}
