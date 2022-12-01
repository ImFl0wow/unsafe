package io.sniffy.unsafe.reflection.module;

import io.sniffy.unsafe.reflection.UnresolvedRefException;
import io.sniffy.unsafe.reflection.UnsafeInvocationException;

import java.lang.reflect.InvocationTargetException;

import static io.sniffy.unsafe.reflection.Unsafe.$;

public class ModuleRef {

    private final /* Module */ Object module;

    public ModuleRef(/* Module */ Object module) {

        assert "java.lang.Module".equals(module.getClass().getName());

        this.module = module;
    }

    public void addOpens(String packageName) throws UnsafeInvocationException, InvocationTargetException {
        try {
            $("java.lang.Module").getNonStaticMethod("implAddOpens", String.class).invoke(module, packageName);
        } catch (UnresolvedRefException e) {
            throw new UnsafeInvocationException(e);
        }
    }

    public boolean tryAddOpens(String packageName) {
        try {
            addOpens(packageName);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

}
