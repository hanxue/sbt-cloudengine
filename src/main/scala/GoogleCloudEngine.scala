/**
 * Created by hanxue on 1/14/14.
 */
import sbt._
import Keys._

object GoogleCloudEngine extends Plugin {

  override lazy val settings = Seq(commands ++= Seq(login, listinstances, setproject, setaccount, listcomponents, configlist, configset) )

  def login = Command.command("login") { state =>
    println("OAuth login to Google Cloud Engine")
    val cmd = Seq("gcloud", "auth", "login")
    sbt.Process(cmd) ! logger
    state
  }

  def listinstances = Command.command("listinstances") { state =>
    println("Listing available GCE machines")
    val cmd = Seq("gcutil", "listinstances")
    // cmd.lines ! logger
    sbt.Process(cmd) ! logger

    state
  }

  def setproject = Command.args("set-project", "<project-name>") { (state, args) =>
    val cmd = Seq("gcloud", "config", "set", "project", args(0))
    sbt.Process(cmd) ! logger
    state
  }

  def setaccount = Command.args("set-account", "<account-name>") { (state, args) =>
    val cmd = Seq("gcloud", "config", "set", "account", args(0))
    sbt.Process(cmd) ! logger
    state
  }

  def listcomponents = Command.command("listcomponents") { state =>
    sbt.Process(Seq("gcloud", "components", "list")) ! logger
    state
  }

  def configlist = Command.command("components") { state =>
    sbt.Process(Seq("gcloud", "config", "list")) ! logger
    state
  }

  def configset = Command.args("set-config", "<option> <value>") { (state, args) =>
    val cmd = Seq("gcloud", "config", "set", args(0), args(1))
    sbt.Process(cmd) ! logger
    state
  }

  val logger = new sbt.ProcessLogger {
    def info(info: => String) = println(info)
    def error(err: => String) = { println(err) }
    def buffer[T](f: => T) = f
  }

}
