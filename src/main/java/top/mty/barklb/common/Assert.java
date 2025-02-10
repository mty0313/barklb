package top.mty.barklb.common;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

public class Assert {
    public static void notEmpty(String text) {
        if (StringUtils.isEmpty(text)) {
            throw new RuntimeException("param cannot be empty");
        }
    }

    public static void notEmpty(Object o, String name) {
        if (null == o) {
            throw new RuntimeException(name + " cannot be empty");
        }
    }

    public static void notEmpty(Collection<Object> collection, String name) {
        for (Object o : collection) {
            notEmpty(o, name);
        }
    }

    public static void notEmpty(String text, String name) {
        if (StringUtils.isEmpty(text)) {
            throw new RuntimeException(name + " cannot be empty");
        }
    }

    public static void notNull(Object o, String name) {
        if (null == o) {
            throw new RuntimeException(String.format("Object %s cannot be null", name));
        }
    }

    public static void assertTrue(boolean expression, String condition) {
        if (!expression) {
            throw new RuntimeException(String.format("%s is not true", condition));
        }
    }
}
