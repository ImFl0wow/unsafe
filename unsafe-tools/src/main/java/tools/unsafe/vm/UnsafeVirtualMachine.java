package tools.unsafe.vm;

import tools.unsafe.reflection.UnsafeException;
import tools.unsafe.reflection.UnsafeInvocationException;
import tools.unsafe.reflection.clazz.UnresolvedClassRef;
import tools.unsafe.reflection.object.ObjectRef;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import static tools.unsafe.fluent.Fluent.$;

public class UnsafeVirtualMachine {

    private final /*com.sun.tools.attach.VirtualMachine*/ Object virtualMachine;

    public UnsafeVirtualMachine(/*VirtualMachine*/ Object virtualMachine) {
        this.virtualMachine = virtualMachine;
    }

    // TODO: move to InternalUnsafe
    public static int getJavaVersion() {
        String version = System.getProperty("java.version");

        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        if (version.contains("-")) {
            version = version.substring(0, version.indexOf("-"));
        }
        return Integer.parseInt(version);
    }

    public static int getPid() {
        try {

            if (getJavaVersion() >= 9) {
                return UnresolvedClassRef.of("java.lang.ProcessHandle").method(Long.TYPE, "pid").invoke(
                        UnresolvedClassRef.of("java.lang.ProcessHandle").staticMethod(Integer.TYPE, "current").invoke()
                ).intValue();
                //return (int) ProcessHandle.current().pid();
            } else {
                return ObjectRef.<RuntimeMXBean>of(ManagementFactory.getRuntimeMXBean()).field("jvm").objectRef().invoke(Integer.TYPE, "getProcessId", new Class<?>[0], new Object[0]);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static UnsafeVirtualMachine attachToSelf() throws UnsafeException {

        if (getJavaVersion() <= 8) {
            if (!$("com.sun.tools.attach.VirtualMachine").isResolved()) {
                try {
                    // TODO: should it be ${java.home}/../Classes/classes.jar on Mac ?
                    // TODO: it doesn't work on JRE
                    // TODO: it doesn't work on JDK 1.5
                    File toolsJar = new File(System.getProperty("java.home") + "/../lib/tools.jar");
                    if (!toolsJar.exists()) throw new RuntimeException(toolsJar.getAbsolutePath() + " does not exist");

                    $(URLClassLoader.class).method("addURL", URL.class).invoke(
                            (URLClassLoader) ClassLoader.getSystemClassLoader(),
                            toolsJar.toURI().toURL()
                    );

                } catch (MalformedURLException e) {
                    throw new UnsafeException(e);
                } catch (InvocationTargetException e) {
                    throw new UnsafeException(e);
                } catch (UnsafeInvocationException e) {
                    throw new UnsafeException(e);
                }
            }
        }


        UnresolvedClassRef.of("sun.tools.attach.HotSpotVirtualMachine").staticBooleanField("ALLOW_ATTACH_SELF").trySet(true);
        UnresolvedClassRef<Object> vmClassRef = UnresolvedClassRef.of("com.sun.tools.attach.VirtualMachine");
        Object vm = null;
        try {
            vm = vmClassRef.staticMethod(Object.class, "attach", String.class).invoke(String.valueOf(UnsafeVirtualMachine.getPid()));
        } catch (UnsafeInvocationException e) {
            throw new UnsafeException(e);
        } catch (InvocationTargetException e) {
            throw new UnsafeException(e);
        }
        return new UnsafeVirtualMachine(/*(com.sun.tools.attach.VirtualMachine)*/ vm);
    }

}
