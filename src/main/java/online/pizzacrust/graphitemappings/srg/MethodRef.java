package online.pizzacrust.graphitemappings.srg;

import lombok.Data;

@Data
public class MethodRef {

    private final String className;
    private final String methodName;
    private final String methodDesc;

    @Override
    public String toString() {
        return className + "/" + methodName + " " + methodDesc;
    }

}
