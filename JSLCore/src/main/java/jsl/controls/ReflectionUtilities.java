package jsl.controls;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * inspired by <a href="http://www.java2s.com/Code/Java/Reflection/Methodsignature.htm">...</a>
 * simplified and modified for purpose
 * <p>
 * methods to return signature (and components thereof) of methods as a string
 */

public class ReflectionUtilities {
    private ReflectionUtilities() {
        // can't instantiate this class
    }

    public static String signatureAsString(Method method) {
        return
                modifiersAsString(method) + " "
                        + returnTypeAsString(method) + " "
                        + method.getName() + "("
                        + parametersAsString(method) + ")";
    }

    public static String modifiersAsString(Method method) {
        StringBuilder sb = new StringBuilder();
        if (Modifier.isPublic(method.getModifiers()))
            sb.append("public ");
        if (Modifier.isProtected(method.getModifiers()))
            sb.append("protected ");
        if (Modifier.isPrivate(method.getModifiers()))
            sb.append("private ");
        if (Modifier.isFinal(method.getModifiers()))
            sb.append("final ");
        if (Modifier.isStatic(method.getModifiers()))
            sb.append("static ");
        // remove the trailing blank
        if (sb.length() > 0)
            sb.delete(sb.length() - 1, sb.length());
        return sb.toString();
    }

    public static String returnTypeAsString(Method method) {
        return method.getReturnType().getSimpleName();
    }

    public static String parametersAsString(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 0) return "";
        StringBuilder paramString = new StringBuilder();
        paramString.append(parameterTypes[0].getSimpleName());
        for (int i = 1; i < parameterTypes.length; i++) {
            paramString.append(",").append(parameterTypes[i].getSimpleName());
        }
        return paramString.toString();
    }
}
