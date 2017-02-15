package online.pizzacrust.graphitemappings.srg;

import lombok.Data;

@Data
public class FieldRef {

    private final String className;
    private final String name;

    @Override
    public String toString() {
        return className + "/" + name;
    }

}
