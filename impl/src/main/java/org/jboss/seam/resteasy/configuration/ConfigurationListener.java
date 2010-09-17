package org.jboss.seam.resteasy.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ExceptionMapper;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.seam.resteasy.validation.ValidationExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationListener implements ServletContextListener
{
   @Inject
   private SeamResteasyConfiguration configuration;
   private Dispatcher dispatcher;
   private ResteasyProviderFactory factory;
   private Registry registry;

   private static final Logger log = LoggerFactory.getLogger(ConfigurationListener.class);

   public void contextInitialized(ServletContextEvent sce)
   {
      if (sce.getServletContext().getAttribute(Dispatcher.class.getName()) == null)
      {
         // RESTEasy has not been started yet, let's start it
         bootstrapResteasy(sce.getServletContext());
      }

      dispatcher = (Dispatcher) sce.getServletContext().getAttribute(Dispatcher.class.getName());
      factory = dispatcher.getProviderFactory();
      registry = dispatcher.getRegistry();

      log.info("Processing seam-resteasy configuration.");

      registerProviders();
      registerResources();
      unregisterResources();
      dispatcher.setLanguageMappings(configuration.getLanguageMappings());
      registerMediaTypeMappings();
      registerExceptionMappings();
   }

   /**
    * Bootstrap RESTEasy
    */
   protected void bootstrapResteasy(ServletContext servletContext)
   {
      log.info("Starting RESTEasy.");
      SeamResteasyBootstrap bootstrap = new SeamResteasyBootstrap(servletContext);
      ResteasyDeployment deployment = bootstrap.createDeployment();
      deployment.start();

      servletContext.setAttribute(ResteasyProviderFactory.class.getName(), deployment.getProviderFactory());
      servletContext.setAttribute(Dispatcher.class.getName(), deployment.getDispatcher());
      servletContext.setAttribute(Registry.class.getName(), deployment.getRegistry());
   }

   /**
    * Register resources specified in {@link SeamResteasyConfiguration}.
    */
   protected void registerResources()
   {
      for (Class<?> clazz : configuration.getResources())
      {
         log.info("Adding root resource {}.", clazz);
         registry.addPerRequestResource(clazz);
      }
   }
   
   /**
    * Unregister resources specified in {@link SeamResteasyConfiguration}.
    * This method is used for explicit exclusion of resources that would be
    * registered by auto-scanning.
    */
   protected void unregisterResources()
   {
      for (Class<?> clazz : configuration.getExcludedResources())
      {
         log.info("Excluding root resource {}.", clazz);
         registry.removeRegistrations(clazz);
      }
   }

   /**
    * Register providers specified in {@link SeamResteasyConfiguration}.
    */
   protected void registerProviders()
   {
      for (Class<?> clazz : configuration.getProviders())
      {
         log.info("Adding provider {}.", clazz);
         factory.registerProvider(clazz);
      }
   }

   /**
    * Register exception mappers based on {@link SeamResteasyConfiguration}.
    */
   protected void registerExceptionMappings()
   {
      // basic exception mapping (exception -> status code)
      for (Entry<Class<? extends Throwable>, Integer> item : configuration.getBasicExceptionMappings().entrySet())
      {
         if (Throwable.class.isAssignableFrom(item.getKey()))
         {
            Class<? extends Throwable> exceptionType = item.getKey();
            int status = item.getValue();
            ExceptionMapper<? extends Throwable> provider = new BasicExceptionMapper<Throwable>(status);
            log.info("Adding basic exception mapping {} -> {}", exceptionType, status);
            try
            {
               factory.addExceptionMapper(provider, exceptionType);
            }
            catch (NoSuchMethodError e)
            {
               log.warn("You are using old version of RESTEasy. Exception mapper for {} will not be registered.", exceptionType);
            }
         }
         else
         {
            log.warn("{} is not an exception. Skipping mapping of the exception to {} status code.", item.getKey(), item.getValue());
         }
      }
      
      // exception mapping (exception -> status code, http body, mime type)
      for (ExceptionMapping mapping : configuration.getExceptionMappings())
      {
         SeamExceptionMapper provider = new SeamExceptionMapper(mapping);
         log.info("Adding exception mapping {} -> {}", mapping.getExceptionType(), mapping.getStatusCode());
         try
         {
            factory.addExceptionMapper(provider, mapping.getExceptionType());
         }
         catch (NoSuchMethodError e)
         {
            log.warn("You are using old version of RESTEasy. Exception mapper for {} will not be registered.", mapping.getExceptionType());
         }
      }
      

      // register ExceptionMapper for Bean Validation integration
      if (configuration.isRegisterValidationExceptionMapper())
      {
         factory.addExceptionMapper(ValidationExceptionMapper.class);
      }
   }

   /**
    * Register media mappings based on {@link SeamResteasyConfiguration}.
    */
   protected void registerMediaTypeMappings()
   {
      Map<String, MediaType> mediaTypeMappings = new HashMap<String, MediaType>();
      for (Entry<String, String> entry : configuration.getMediaTypeMappings().entrySet())
      {
         mediaTypeMappings.put(entry.getKey(), MediaType.valueOf(entry.getValue()));
      }
      dispatcher.setMediaTypeMappings(mediaTypeMappings);
   }

   public void contextDestroyed(ServletContextEvent sce)
   {
   }
}