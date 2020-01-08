package org.fulib.yaml;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Reflector
{
   // =============== Fields ===============

   private String   className = "";
   private Method   emfCreateMethod;
   private Object   emfFactory;
   private Class<?> eObjectClass;
   private Class<?> clazz;

   private String[] properties; // cache

   // =============== Properties ===============

   public Class<?> getClazz()
   {
      if (this.clazz == null)
      {
         try
         {
            this.clazz = Class.forName(this.className);
         }
         catch (ClassNotFoundException e)
         {
            e.printStackTrace();
         }
      }
      return this.clazz;
   }

   public Reflector setClazz(Class<?> clazz)
   {
      this.clazz = clazz;
      return this;
   }

   public String getClassName()
   {
      return this.className;
   }

   public Reflector setClassName(String className)
   {
      this.className = className;
      return this;
   }

   public Reflector setUseEMF()
   {
      String packageName = this.className;
      // chop simpleClassName
      int pos = packageName.lastIndexOf('.');
      String simpleClassName = packageName.substring(pos + 1);
      simpleClassName = simpleClassName.substring(0, simpleClassName.length() - "Impl".length());
      packageName = packageName.substring(0, pos);

      // chop .impl
      packageName = packageName.substring(0, packageName.length() - ".impl".length());

      pos = packageName.lastIndexOf('.');
      String lastPart = packageName.substring(pos + 1);
      String simpleFactoryName = StrUtil.cap(lastPart) + "Factory";
      try
      {
         Class<?> factoryClass = Class.forName(packageName + "." + simpleFactoryName);
         Field eInstanceField = factoryClass.getField("eINSTANCE");
         this.emfFactory = eInstanceField.get(null);

         this.emfCreateMethod = this.emfFactory.getClass().getMethod("create" + simpleClassName);

         this.eObjectClass = Class.forName("org.eclipse.emf.ecore.EObject");
      }
      catch (Exception e)
      {
         Logger.getGlobal().log(Level.SEVERE, "could not find EMF Factory createXY method", e);
      }

      return this;
   }

   public String[] getProperties()
   {
      if (this.properties != null)
      {
         return this.properties;
      }

      Class<?> clazz = this.getClazz();

      Method[] methods = clazz.getMethods();

      Set<String> fieldNames = new LinkedHashSet<>();
      for (Method method : methods)
      {
         String methodName = method.getName();

         if (methodName.startsWith("get") && !"getClass".equals(methodName) && method.getParameterCount() == 0)
         {
            methodName = methodName.substring(3);

            methodName = StrUtil.downFirstChar(methodName);

            if (!"".equals(methodName.trim()))
            {
               fieldNames.add(methodName);
            }
         }
      }

      this.properties = fieldNames.toArray(new String[] {});

      Arrays.sort(this.properties);

      return this.properties;
   }

   // =============== Methods ===============

   public Object newInstance()
   {
      try
      {
         if (this.emfCreateMethod != null)
         {
            return this.emfCreateMethod.invoke(this.emfFactory);
         }

         Class<?> clazz = this.getClazz();
         return clazz.newInstance();
      }
      catch (Exception e)
      {
         // e.printStackTrace();
      }

      return null;
   }

   public Object getValue(Object object, String attribute)
   {
      if (object == null)
      {
         return null;
      }

      try
      {
         Class<?> clazz = this.getClazz();

         Method method = clazz.getMethod("get" + StrUtil.cap(attribute));

         return method.invoke(object);
      }
      catch (Exception ignored)
      {
      }

      try
      {
         Class<?> clazz = this.getClazz();

         Method method = clazz.getMethod(attribute);

         return method.invoke(object);
      }
      catch (Exception ignored)
      {

      }

      try
      {
         Class<?> clazz = this.getClazz();

         Method method = clazz.getMethod("is" + StrUtil.cap(attribute));

         return method.invoke(object);
      }
      catch (Exception e3)
      {
         // e.printStackTrace();
      }

      return null;
   }

   public Object setValue(Object object, String attribute, Object value, String type)
   {
      if (object == null)
      {
         return null;
      }

      try
      {
         Class<?> clazz = this.getClazz();

         Class<?> valueClass = value.getClass();

         if (this.eObjectClass != null && this.eObjectClass.isAssignableFrom(valueClass))
         {
            valueClass = valueClass.getInterfaces()[0];
         }

         Method method = clazz.getMethod("set" + StrUtil.cap(attribute), valueClass);

         return method.invoke(object, value);
      }
      catch (Exception e)
      {
         // e.printStackTrace();
      }

      // maybe a number
      try
      {
         int intValue = Integer.parseInt((String) value);
         Class<?> clazz = this.getClazz();

         Method method = clazz.getMethod("set" + StrUtil.cap(attribute), int.class);

         method.invoke(object, intValue);

         return true;
      }
      catch (Exception e)
      {
         // e.printStackTrace();
      }

      // maybe a huge number
      try
      {
         long longValue = Long.parseLong((String) value);
         Class<?> clazz = this.getClazz();

         Method method = clazz.getMethod("set" + StrUtil.cap(attribute), long.class);

         method.invoke(object, longValue);

         return true;
      }
      catch (Exception e)
      {
         // e.printStackTrace(); // I don't like this :(
      }

      // maybe a double
      try
      {
         double doubleValue = Double.parseDouble((String) value);
         Class<?> clazz = this.getClazz();

         Method method = clazz.getMethod("set" + StrUtil.cap(attribute), double.class);

         method.invoke(object, doubleValue);

         return true;
      }
      catch (Exception e)
      {
         // e.printStackTrace();
      }

      // maybe a float
      try
      {
         float floatValue = Float.parseFloat((String) value);
         Class<?> clazz = this.getClazz();

         Method method = clazz.getMethod("set" + StrUtil.cap(attribute), float.class);

         method.invoke(object, floatValue);

         return true;
      }
      catch (Exception e)
      {
         // e.printStackTrace();
      }

      // to-many?
      try
      {
         Class<?> clazz = this.getClazz();

         Method method = clazz.getMethod("with" + StrUtil.cap(attribute), Object[].class);

         method.invoke(object, new Object[] { new Object[] { value } });

         return true;
      }
      catch (Exception e)
      {
         // e.printStackTrace();
      }

      try
      {
         if (this.emfCreateMethod != null)
         {
            Class<?> clazz = this.getClazz();

            // its o.getAssoc().add(v)
            Method getMethod = clazz.getMethod("get" + StrUtil.cap(attribute));

            Object collection = getMethod.invoke(object);

            Method addMethod = collection.getClass().getMethod("add", Object.class);

            addMethod.invoke(collection, value);
            return true;
         }
      }
      catch (Exception e)
      {
         // e.printStackTrace();
      }

      return null;
   }

   public void removeObject(Object object)
   {
      // call removeYou if possible
      try
      {
         Class<?> clazz = this.getClazz();
         Method removeYou = clazz.getMethod("removeYou");
         removeYou.invoke(object);
      }
      catch (Exception e)
      {
         // e.printStackTrace();
      }
   }
}
