package jsl.controls;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines the set of valid control types
 */
public enum ControlType {
    DOUBLE(Double.class), INTEGER(Integer.class), LONG(Long.class),
    FLOAT(Float.class), SHORT(Short.class), BYTE(Byte.class), BOOLEAN(Boolean.class);

    private final Class<?> clazz;

    ControlType(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Class<?> asClass() {
        return clazz;
    }

    public static final EnumSet<ControlType> CONTROL_TYPE_SET = EnumSet.of(ControlType.DOUBLE, ControlType.INTEGER,
            ControlType.LONG, ControlType.FLOAT, ControlType.SHORT, ControlType.BYTE, ControlType.BOOLEAN);

//        public static final EnumMap<ControlType, Class<?>> validTypesToClassMap = new EnumMap<>(ControlType.class);

    public static final Map<Class<?>, ControlType> classTypesToValidTypesMap = new HashMap<>();

    static {
//            validTypesToClassMap.put(ControlType.DOUBLE, Double.class);
//            validTypesToClassMap.put(ControlType.INTEGER, Integer.class);
//            validTypesToClassMap.put(ControlType.LONG, Long.class);
//            validTypesToClassMap.put(ControlType.FLOAT, Float.class);
//            validTypesToClassMap.put(ControlType.SHORT, Short.class);
//            validTypesToClassMap.put(ControlType.BYTE, Byte.class);
//            validTypesToClassMap.put(ControlType.BOOLEAN, Boolean.class);

        classTypesToValidTypesMap.put(Double.class, ControlType.DOUBLE);
        classTypesToValidTypesMap.put(Integer.class, ControlType.INTEGER);
        classTypesToValidTypesMap.put(Long.class, ControlType.LONG);
        classTypesToValidTypesMap.put(Float.class, ControlType.FLOAT);
        classTypesToValidTypesMap.put(Short.class, ControlType.SHORT);
        classTypesToValidTypesMap.put(Byte.class, ControlType.BYTE);
        classTypesToValidTypesMap.put(Boolean.class, ControlType.BOOLEAN);
    }
}
