import java.time.Clock

import play.api.{Configuration, Environment}
import services.{ApplicationTimer, AtomicCounter, Counter}
import play.api.inject.{Binding, Module}


/**
 * This class is a Guice module that tells Guice how to bind several
 * different types. This Guice module is created when the Play
 * application starts.

 * Play will automatically use any class called `Module` that is in
 * the root package. You can create modules in other locations by
 * adding `play.modules.enabled` settings to the `application.conf`
 * configuration file.
 */
class AppModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration): collection.Seq[Binding[_]] = Seq(
    // Use the system clock as the default implementation of Clock
    bind[Clock].toInstance(Clock.systemDefaultZone),
    // Ask Guice to create an instance of ApplicationTimer when the
    // application starts.
    bind[ApplicationTimer].toSelf.eagerly(),
    // Set AtomicCounter as the implementation for Counter.
    bind[Counter].to[AtomicCounter]
  )
}
