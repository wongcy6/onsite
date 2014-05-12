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
    "INSERT INTO CheckDB (Code,Name,CheckInTime) values (10000, 'Richard Wong', NULL)",
    "INSERT INTO CheckDB (Code,Name,CheckInTime) values (20000, 'Derek Shen', NULL)",
    "INSERT INTO CheckDB (Code,Name,CheckInTime) values (30000, 'Jeff Weiner', NULL)",
    "INSERT INTO CheckDB (Code,Name,CheckInTime) values (40000, 'Kevin Scott', NULL)",
    "INSERT INTO CheckDB (Code,Name,CheckInTime) values (50000, 'Robin Zhang', NULL)"
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

      val result:List[Int~String~Option[java.util.Date]] = {
        SQL("select * from CheckDB").as(get[Int]("Code")~get[String]("Name")~get[Option[java.util.Date]]("CheckInTime")*)
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

  def index = Action {
    Ok(views.html.index("请输入验证码", true))
  }

  def update = Action { request =>

    val code : Int = try {request.body.asFormUrlEncoded.get("code")(0).toInt} catch {case _: java.lang.NumberFormatException => -1}

    DB.withConnection{ implicit c =>
      val result: Int = SQL("UPDATE CheckDB SET CheckInTime={time} WHERE Code={code} AND CheckInTime IS NULL").on(
        "time" -> new java.sql.Timestamp(System.currentTimeMillis()),
        "code" -> code).executeUpdate()

      if (result == 0) {
        val count: Long = SQL("SELECT COUNT(*) from CheckDB where Code={code}").on("code"->code).as(scalar[Long].single)
        if (count == 0) {
          Ok(views.html.index("验证失败. 验证码 " + code.toString + "不存在", false))
        } else {
          Ok(views.html.index("验证失败。验证码 " + code.toString + "已使用", false))
        }
      } else {
        Ok(views.html.index("验证成功", true))
      }
    }
  }

}