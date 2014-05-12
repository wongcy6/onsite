package controllers

import anorm._
import play.api._
import play.api.mvc._
import play.api.db.DB
import play.api.Play.current
import anorm.SqlParser._

object Application extends Controller {

  def createSchema = Action {
    DB.withConnection{ implicit c =>
      val result: Int = SQL("CREATE TABLE CheckDB (Code int, Name varchar(255) NOT NULL, CheckInTime TimeStamp, PRIMARY KEY (Code))").executeUpdate()
    }
    Ok("Done")
  }

  def addData = Action {

    val dataSqls = Array(
    "INSERT INTO CheckDB (Code,Name,CheckInTime) values (10000, 'Joe Doe', NULL)",
    "INSERT INTO CheckDB (Code,Name,CheckInTime) values (20000, 'Jane Smith', NULL)",
    "INSERT INTO CheckDB (Code,Name,CheckInTime) values (30000, 'Bill Gates', NULL)",
    "INSERT INTO CheckDB (Code,Name,CheckInTime) values (40000, 'Steven Jobs', NULL)",
    "INSERT INTO CheckDB (Code,Name,CheckInTime) values (50000, 'David Chen', NULL)"
      )

    DB.withConnection { implicit c =>
        for (dataSql <- dataSqls) {
          val result: Int = SQL(dataSql).executeUpdate()
        }
    }

    Ok("Done")
  }

  def dumpData = Action {

    DB.withConnection { implicit c =>

      val result:List[(Int,String,Option[java.util.Date])] = {
        SQL("select * from CheckDB")
          .as(get[Int]("Code") ~ get[String]("Name") ~ get[Option[java.util.Date]]("CheckInTime") map(flatten) *)
      }

      Ok(result.toString)
    }
  }

  def dropSchema = Action {
    DB.withConnection{ implicit c =>
      val result: Int = SQL("DROP TABLE CheckDB").executeUpdate()
    }
    Ok("Done")
  }

  // -------------------------------------

  def index = Action {
    Ok(views.html.index("请输入验证码", true))
  }

  def update = Action { request =>

    val code : Int = try {request.body.asFormUrlEncoded.get("code")(0).toInt} catch {case _: java.lang.NumberFormatException => -1}

    DB.withConnection{ implicit c =>
      val result: Int = SQL("UPDATE CheckDB SET CheckInTime={time} WHERE Code={code} AND CheckInTime IS NULL").on(
        "time" -> new java.sql.Timestamp(System.currentTimeMillis()),
        "code" -> code).executeUpdate()

      val record:List[(Int,String,Option[java.util.Date])] = {
        try {
          SQL("SELECT * from CheckDB WHERE code={code}")
            .on("code" -> code)
            .as(get[Int]("Code") ~ get[String]("Name") ~ get[Option[java.util.Date]]("CheckInTime") map (flatten) *)
        } catch {
          case e: Exception => List[(Int,String,Option[java.util.Date])] {(-1,"None",None)}
        }
      }

      if (result == 0) {
        if (record.length == 0) {
          Ok(views.html.index("验证失败. 验证码 " + code.toString + "不存在", false))
        } else if (record.head._1 != -1) {
          Ok(views.html.index("验证失败。验证码 " + code.toString + "已使用。 名字： " + record.head._2.toString + " 时间： " + record.head._3.getOrElse(-1), false))
        } else {
          Ok(views.html.index("验证失败. 请重新输入", false))
        }

      } else {
        Ok(views.html.index("验证码 " + code.toString + "验证成功。名字： " + record.head._2.toString, true))
      }
    }
  }

}