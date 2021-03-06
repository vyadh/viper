import com.typesafe.sbt.SbtProguard._
import com.typesafe.sbt.SbtProguard.ProguardKeys._

proguardSettings

//javaOptions in (Proguard, proguard) := Seq("-Xmx512m")

ProguardKeys.proguardVersion in Proguard := "5.0"

ProguardKeys.options in Proguard ++= Seq(
  // Don't obfuscate, which means a much bigger JAR, but avoids exception on stream dump
  "-dontobfuscate",
  // Optimise doesn't work with Glazed Lists
  "-dontoptimize",
  "-dontwarn scala.**",
  "-dontwarn viper.ui.UI$$anonfun$initLookAndFeel$1",
  "-dontwarn viper.ui.UI$$anonfun$initLookAndFeel$2",
  "-dontskipnonpubliclibraryclasses",
  "-dontskipnonpubliclibraryclassmembers",
  "-dontnote scala.Enumeration",
  "-keep class * implements org.xml.sax.EntityResolver",
  "-keepattributes InnerClasses",
  "-keepattributes Signature",
  "-keepclassmembers class * { ** MODULE$; }",
  """-keepclassmembernames class scala.concurrent.forkjoin.ForkJoinPool {
    long ctl;
    long stealCount;
    int plock;
    int indexSeed;
  }""",
  """-keepclassmembernames class scala.concurrent.forkjoin.ForkJoinWorkerThread {
    ** MODULE$;
  }""",
  "-keepclassmembernames class scala.concurrent.forkjoin.ForkJoinTask { int status; }",
  "-keepclassmembernames class scala.concurrent.forkjoin.ForkJoinPool$WorkQueue { int qlock; }",
  """-keepclassmembernames class scala.concurrent.forkjoin.LinkedTransferQueue {
    scala.concurrent.forkjoin.LinkedTransferQueue$Node head;
    scala.concurrent.forkjoin.LinkedTransferQueue$Node tail;
    int sweepVotes;
  }""",
  """-keepclassmembernames class scala.concurrent.forkjoin.LinkedTransferQueue$Node {
    java.lang.Object item;
    scala.concurrent.forkjoin.LinkedTransferQueue$Node next;
    java.lang.Thread waiter;
  }""",
  // Glazed Lists uses reflection, just keep all for now
  "-keep class ca.odell.glazedlists.** { public protected static *;}",
  "-dontwarn ca.odell.glazedlists.**",
  // SVG Salamander warns on some Ant integration classes, just ignore
  "-dontwarn com.kitfox.svg.**",
  // Woodstox
  "-dontwarn com.ctc.wstx.**",
  "-dontwarn com.ctc.osgi.**"
)

// Specify library JARs, different for OSs and JVMs
ProguardKeys.options in Proguard ++= {
  if (sys.props("java.vendor").startsWith("Apple Inc"))
    Seq("-libraryjars <java.home>/../Classes/classes.jar")
  else
    Seq("-libraryjars <java.home>/lib/rt.jar")
}

ProguardKeys.options in Proguard += ProguardOptions.keepMain("viper.Viper")

// Override the default filter to exclude manifests, as we only have our one anyway
ProguardKeys.inputFilter in Proguard := { file =>
  file.name match {
    case n if n.startsWith("root_") => Some("META-INF/**")
    case _ => Some("!META-INF/**")
  }
}
