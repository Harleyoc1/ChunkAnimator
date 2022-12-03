package lumien.chunkanimator.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class OptiFineUniform3fWrapper implements UniformWrapper<Object> {

    private static final Method SET_VALUE_METHOD;

    static {
        try {
            Class<?> uniform3fClass = Class.forName("net.optifine.shaders.uniform.ShaderUniform3f");
            SET_VALUE_METHOD = uniform3fClass.getMethod("setValue", float.class, float.class, float.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private final Object uniform;

    public OptiFineUniform3fWrapper(Object uniform) {
        this.uniform = uniform;
    }

    @Override
    public void set(float x, float y, float z) {
        try {
            SET_VALUE_METHOD.invoke(uniform, x, y, z);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
