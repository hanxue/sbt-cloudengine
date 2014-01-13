package sbtcloudengine

import sbt._
import scala.io.Source

object Plugin extends sbt.Plugin {
  import Keys._
  import Def.Initialize

  
  object CloudEngineKeys {
    lazy val login    = InputKey[Unit]("gcloud auth login", "Authenticate and login to GCE.")
    lazy val listinstances = InputKey[Unit]("gcutil listinstances", "List active Compute Engine instances.")
    // lazy val devServer      = InputKey[revolver.AppProcess]("appengine-dev-server", "Run application through development server.")
    //lazy val stopDevServer  = TaskKey[Unit]("appengine-stop-dev-server", "Stop development server.")
    
    
    lazy val sdkVersion     = SettingKey[String]("cloudengine-sdk-version")
    lazy val sdkPath        = SettingKey[File]("cloudengine-sdk-path")
    lazy val classpath      = SettingKey[Classpath]("appengine-classpath")
    lazy val binPath        = SettingKey[File]("cloudengine-bin-path")
    lazy val libPath        = SettingKey[File]("cloudengine-lib-path")
    lazy val libUserPath    = SettingKey[File]("cloudengine-lib-user-path")
    lazy val gcloudName     = SettingKey[String]("cloudengine-gcloud-name")
    lazy val gcloudPath     = SettingKey[File]("cloudengine-gcloud-path")
    lazy val gcutilName     = SettingKey[String]("cloudengine-gcutil-name")
    lazy val gcutilPath     = SettingKey[File]("cloudengine-gcutil-path")
    lazy val overridePath   = SettingKey[File]("cloudengine-override-path")
    lazy val emptyFile      = TaskKey[File]("cloudengine-empty-file")
  }
  private val gce = CloudEngineKeys
  
  object CloudEngine {
    // see https://github.com/jberkel/android-plugin/blob/master/src/main/scala/AndroidHelpers.scala
    def gcloudTask(action: String,
                   depends: TaskKey[File] = gce.emptyFile, outputFile: Option[String] = None): Initialize[InputTask[Unit]] =
      Def.inputTask {
        import complete.DefaultParsers._
        val input: Seq[String] = spaceDelimited("<arg>").parsed
        val x = depends.value
        gcloudTaskCmd(gce.gcloudPath.value, input, action, streams.value)
      }
    
    def gcloudTaskCmd(gcloudPath: sbt.File, args: Seq[String],
                              params: String, s: TaskStreams) = {
        val gcloud: Seq[String] = Seq(gcloudPath.absolutePath.toString) ++ args :+ params
        //val gcloud: Seq[String] = temp :+ params
        s.log.debug(gcloud.mkString(" "))
        val out = new StringBuffer
        val exit = Process(gcloud)!<

        if (exit != 0) {
          s.log.error(out.toString)
          sys.error("error executing gcloud")
        }
        else s.log.info(out.toString)
        ()
      }

    def gcutilTask(action: String,
                   depends: TaskKey[File] = gce.emptyFile, outputFile: Option[String] = None): Initialize[InputTask[Unit]] =
      Def.inputTask {
        import complete.DefaultParsers._
        val input: Seq[String] = spaceDelimited("<arg>").parsed
        val x = depends.value
        gcutilTaskCmd(gce.gcutilPath.value, input, Seq(action), streams.value)
      }

    def gcutilTaskCmd(gcutilPath: sbt.File, args: Seq[String],
                      params: Seq[String], s: TaskStreams) = {
      val gcutil: Seq[String] = Seq(gcutilPath.absolutePath.toString) ++ args ++ params
      s.log.debug(gcutil.mkString(" "))
      val out = new StringBuffer
      val exit = Process(gcutil)!<

      if (exit != 0) {
        s.log.error(out.toString)
        sys.error("error executing gcutil")
      }
      else s.log.info(out.toString)
      ()
    }
    
    def buildCloudengineSdkPath: File = {
      val sdk = System.getenv("CLOUDENGINE_SDK_HOME")
      if (sdk == null) sys.error("You need to set CLOUDENGINE_SDK_HOME")
      new File(sdk)
    }

    def buildSdkVersion(libUserPath: File): String = {
      Source.fromFile(new File(buildCloudengineSdkPath.toString + "/lib/google/cloud/sdk/core/VERSION")).mkString
    }

    def isWindows = System.getProperty("os.name").startsWith("Windows")
    def osBatchSuffix = if (isWindows) ".cmd" else ".sh"
  }

  lazy val baseCloudEngineSettings: Seq[Def.Setting[_]] = Seq(

        
    //gce.requestLogs     := CloudEngine.gcloudTask("request_logs", outputFile = Some("request.log")).evaluated,
    gce.login           := CloudEngine.gcloudTask("auth login").evaluated,
    gce.listinstances   := CloudEngine.gcutilTask("listinstances").evaluated,


    gce.sdkVersion <<= (gce.libUserPath) { (dir) => CloudEngine.buildSdkVersion(dir) },
    gce.sdkPath := CloudEngine.buildCloudengineSdkPath,
    
    gce.binPath <<= gce.sdkPath(_ / "bin"),
    gce.libPath <<= gce.sdkPath(_ / "lib"),
    gce.gcloudName := "gcloud",
    gce.gcloudPath <<= (gce.binPath, gce.gcloudName) { (dir, name) => dir / name },
    gce.gcutilName := "gcutil",
    gce.gcutilPath <<= (gce.binPath, gce.gcutilName) { (dir, name) => dir / name },
    gce.emptyFile := file("")
  )

  lazy val cloudengineSettings = baseCloudEngineSettings

}
