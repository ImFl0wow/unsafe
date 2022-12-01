package tools.unsafe.reflection.field.resolved;

import tools.unsafe.reflection.UnsafeInvocationException;
import tools.unsafe.reflection.clazz.ClassRef;
import tools.unsafe.reflection.field.types.DynamicObjectFieldRef;
import tools.unsafe.reflection.object.ObjectRef;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

public abstract class AbstractDynamicObjectFieldRef<C,T> extends AbstractFieldRef<C> implements DynamicObjectFieldRef<C, T> {

    public AbstractDynamicObjectFieldRef(ClassRef<C> classRef, Field field) {
        super(classRef, field);
    }

    public abstract T get(C instance) throws UnsafeInvocationException;

    @Override
    @Nonnull
    public ObjectRef<T> objectRef(C instance) throws UnsafeInvocationException {
        T value = get(instance);
        Class<T> clazz;
        if (null == value) {
            //noinspection unchecked
            clazz = (Class<T>) field.getType();
        } else {
            //noinspection unchecked
            clazz = (Class<T>) value.getClass();
        }
        return new ObjectRef<T>(
                new ClassRef<T>(clazz),
                value
        );
    }


    // TODO: implement the same in StaticFieldRef
    public T getNotNull(C instance, T defaultValue) throws UnsafeInvocationException {
        T value = get(instance);
        return null == value ? defaultValue : value;
    }

    @Override
    public abstract void set(C instance, T value) throws UnsafeInvocationException;


    public void copy(C from, C to) throws UnsafeInvocationException {
        set(to, get(from));
    }

}
