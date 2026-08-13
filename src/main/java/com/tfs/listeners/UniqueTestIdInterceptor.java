package com.tfs.listeners;

import com.tfs.annotations.TestInfo;
import org.testng.*;
import java.lang.reflect.Method;
import java.util.*;

public class UniqueTestIdInterceptor implements IMethodInterceptor {
    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        Set<String> ids = new HashSet<>();
        for (IMethodInstance instance : methods) {
            Method m = instance.getMethod().getConstructorOrMethod().getMethod();
            TestInfo i = m.getAnnotation(TestInfo.class);
            if (i == null) throw new IllegalStateException("Missing @TestInfo: " + m.getName());
            if (!ids.add(i.id())) throw new IllegalStateException("Duplicate Test Case ID in run: " + i.id());
        }
        return methods;
    }
}
