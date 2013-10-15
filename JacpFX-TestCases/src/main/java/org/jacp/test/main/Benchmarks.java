package org.jacp.test.main;

/**
 * Created with IntelliJ IDEA.
 * User: ady
 * Date: 15.10.13
 * Time: 10:19
 * To change this template use File | Settings | File Templates.
 */

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Benchmarks {

    private static Unsafe getUnsafe() {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return (Unsafe) f.get(null);
        } catch (Exception e) {
            return null;
        }
    }


    public static class Employee {

        private String name = "Rick";

        public String getName() {
            return name + "##";
        }

        public void setName(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }

    }

    public static void main(String[] arg) throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Class thisClass = lookup.lookupClass();  // (who am I?)
        Employee employee = new Employee();
        Field fieldName = null;
        String name;
        MethodType methodType;
        MethodHandle methodHandle;

        for (Field field : Employee.class.getDeclaredFields()) {
            if (field.getName().equals("name")) {
                fieldName = field;
                fieldName.setAccessible(true);
                break;
            }
        }
        MethodHandle methodHandleFieldDirect = lookup.unreflectGetter(fieldName);
        name = (String) methodHandleFieldDirect.invokeExact(new Employee());
        System.out.println("method handle for field direct " + name);

        //Lookup invoke dynamic
        methodType = MethodType.methodType(String.class);
        methodHandle = lookup.findVirtual(Employee.class, "getName", methodType);
        name = (String) methodHandle.invokeExact(new Employee());
        System.out.println("invoke dynamic " + name);

        //Lookup reflection
        Method method = Employee.class.getMethod("getName", new Class[]{});
        name = (String) method.invoke(new Employee());
        System.out.println("reflection " + name);

        //Now let's be unsafe
        Unsafe unsafe = getUnsafe();
        long offset = unsafe.objectFieldOffset(fieldName);
        name = (String) unsafe.getObject(new Employee(), offset);


        long[] timesArray = {1_000L,
                10_1000L,
                100_000L,
                1_000_000L,
                10_000_000L,
                100_000_000L
        };

        for (long times : timesArray) {
            long start = 0;
            long end = 0;
            long regularTime;
            long invokeDynamicTime;
            long reflectionTime;
            long invokeDynamicTimeUsingField;
            long fieldDirect;
            long unsafeField;
            long direct;

            System.out.printf("<h4> %,d</h4> \n", times);
            //warm up
            for (int index = 0; index < times; index++) {
                employee.getName();
                name = (String) methodHandle.invokeExact(employee);
                name = (String) method.invoke(employee);
                name = (String) methodHandleFieldDirect.invokeExact(employee);
                name = (String) fieldName.get(employee);
                name = (String) unsafe.getObject(new Employee(), offset);

            }

            System.out.printf("\n    \n");


            start = System.nanoTime();
            for (int index = 0; index < times; index++) {
                name = employee.getName();
            }
            end = System.nanoTime();
            regularTime = end - start;
            System.out.printf("\n    \n" +
                    "                  \n" +
                    "                      \n" +
                    "                      \n" +
                    "                  \n", regularTime / times);

            start = System.nanoTime();
            for (int index = 0; index < times; index++) {
                name = (String) methodHandle.invokeExact(employee);
            }
            end = System.nanoTime();
            invokeDynamicTime = end - start;

            System.out.printf("\n    \n" +
                    "                  \n" +
                    "                      \n" +
                    "                      \n" +
                    "                  \n", invokeDynamicTime / times);


            start = System.nanoTime();
            for (int index = 0; index < times; index++) {
                name = (String) method.invoke(employee);
            }
            end = System.nanoTime();
            reflectionTime = end - start;
            System.out.printf("\n    \n" +
                    "                  \n" +
                    "                      \n" +
                    "                      \n" +
                    "                  \n", reflectionTime / times);


            start = System.nanoTime();
            for (int index = 0; index < times; index++) {
                name = (String) methodHandleFieldDirect.invokeExact(employee);
            }
            end = System.nanoTime();
            invokeDynamicTimeUsingField = end - start;
            System.out.printf("\n    \n" +
                    "                  \n" +
                    "                      \n" +
                    "                      \n" +
                    "                  \n", invokeDynamicTimeUsingField / times);


            //old school reflection
            start = System.nanoTime();
            for (int index = 0; index < times; index++) {
                name = (String) fieldName.get(employee);
            }
            end = System.nanoTime();
            fieldDirect = end - start;
            System.out.printf("\n    \n" +
                    "                  \n" +
                    "                      \n" +
                    "                      \n" +
                    "                  \n", fieldDirect / times);


            //unsafe refection
            start = System.nanoTime();
            for (int index = 0; index < times; index++) {
                name = (String) unsafe.getObject(employee, offset);
            }
            end = System.nanoTime();
            unsafeField = end - start;
            System.out.printf("\n    \n" +
                    "                  \n" +
                    "                      \n" +
                    "                      \n" +
                    "                  \n", unsafeField / times);

            //unsafe refection
            start = System.nanoTime();
            for (int index = 0; index < times; index++) {
                name = employee.name;
            }
            end = System.nanoTime();
            direct = end - start;
            System.out.printf("\n    \n" +
                    "                  \n" +
                    "                      \n" +
                    "                      \n" +
                    "                  \n", direct / times);


            // System.out.printf("\n <table><tbody><tr><th>description</th><th>duration in nanoseconds</th></tr><tr><td>regular method call time</td><td>%d</td></tr> <tr><td>invoke dynamic method call time</td><td>%d</td></tr><tr><td>reflection method call time</td><td>%d</td></tr><tr><td>field method invoke dynamic call time</td><td>%d</td></tr><tr><td>field method invoke reflection call time</td><td>%d</td></tr><tr><td>unsafe field access time</td><td>%d</td></tr><tr><td>direct</td><td>%d</td></tr></tbody></table>\n");

        }
    }
}