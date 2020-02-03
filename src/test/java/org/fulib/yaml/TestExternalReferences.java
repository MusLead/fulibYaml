package org.fulib.yaml;

import org.fulib.yaml.testmodel.Student;
import org.fulib.yaml.testmodel.subpackage.University;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestExternalReferences
{
   @Test
   void ensureTestModelIntegrity()
   {
      boolean containsColorSetter = false;
      boolean containsColorGetter = false;

      for (Method method : Student.class.getMethods())
      {
         if ("getUniversity".equals(method.getName()))
         {
            containsColorGetter = true;
         }
         else if ("setUniversity".equals(method.getName()))
         {
            containsColorSetter = true;
         }
      }

      assertThat(containsColorGetter, is(true));
      assertThat(containsColorSetter, is(true));
   }

   @Test
   void testExternalReference()
   {
      Student student = new Student().setUniversity(new University());

      YamlIdMap yim = new YamlIdMap(student.getClass().getPackage().getName());

      RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> yim.encode(student));

      assertThat(runtimeException.getMessage(), containsString("ReflectorMap could not find"));

      YamlIdMap idMap = new YamlIdMap(student.getClass().getPackage().getName(),
                                      University.class.getPackage().getName());

      String yaml = idMap.encode(student);

      assertThat(yaml, notNullValue());
   }
}
