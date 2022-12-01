package io.sniffy.unsafe;

import io.sniffy.unsafe.reflection.Unsafe;
import io.sniffy.unsafe.reflection.clazz.UnresolvedClassRef;
import io.sniffy.unsafe.reflection.field.StaticFieldRef;
import sun.security.jca.ProviderList;
import sun.security.jca.Providers;
//import sun.security.jca.ProviderList;
//import sun.security.jca.Providers;

import javax.net.ssl.SSLContext;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.util.concurrent.Future;

import static io.sniffy.unsafe.reflection.Unsafe.$;

// TODO: hide illegal reflect access warning
public class Main {

    // TODO: candidate for public API
    private static int getPid() {
        try {

            if (Unsafe.getJavaVersion() >= 9) {
                return $("java.lang.ProcessHandle").getNonStaticMethod(Long.TYPE, "pid").invoke(
                        $("java.lang.ProcessHandle").getStaticMethod("current").invoke()
                ).intValue();
            } else {
                return $(ManagementFactory.getRuntimeMXBean()).$("jvm").invoke(Integer.TYPE, "getProcessId", new Class<?>[0], new Object[0]);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {

        SSLContext.getDefault();

        long start = System.currentTimeMillis();

        Future<Instrumentation> instrumentationFuture = Unsafe.getInstrumentationFuture();

        Instrumentation instrumentation = instrumentationFuture.get();

        long end = System.currentTimeMillis();

        System.out.println("Instrumentation obtained in " + (end - start) + " milliseconds");

        assert null != instrumentation;

        UnresolvedClassRef<Object> classRef = $("sun.security.jca.Providers");
        classRef.getModuleRef().tryAddOpens("sun.security.jca");

        StaticFieldRef<ThreadLocal<ProviderList>> threadLists = classRef.<ThreadLocal<ProviderList>>getStaticField("threadLists").resolve();
        StaticFieldRef<Integer> threadListsUsed = classRef.<Integer>getStaticField("threadListsUsed").resolve();

        final ProviderList IT = ProviderList.newList();

        threadListsUsed.set(1);
        threadLists.set(new ThreadLocal<ProviderList>() {

            @Override
            protected ProviderList initialValue() {
                System.out.println("initialValue()");
                return IT;
            }

            @Override
            public ProviderList get() {
                System.out.println("get()");
                return IT;
            }

            @Override
            public void set(ProviderList value) {
                System.out.println("set(" + value + ")");
            }

            @Override
            public void remove() {
                System.out.println("remove");
            }
        });

        $(Providers.class).retransform();

        ProviderList providerList = Providers.getProviderList();

        System.out.println(providerList);

        assert providerList.size() == 0;
    }

}