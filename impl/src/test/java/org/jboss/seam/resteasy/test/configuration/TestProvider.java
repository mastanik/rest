package org.jboss.seam.resteasy.test.configuration;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.jboss.seam.resteasy.test.Student;

@Provider
@Produces("foo/bar")
public class TestProvider implements MessageBodyWriter<Student>
{
   public long getSize(Student t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return -1;
   }

   public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return mediaType.equals(MediaType.valueOf("foo/bar"));
   }

   public void writeTo(Student t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException
   {
      OutputStreamWriter writer = new OutputStreamWriter(entityStream); 
      writer.write(t.getName() + " Hartinger");
      writer.flush();
   }
}
