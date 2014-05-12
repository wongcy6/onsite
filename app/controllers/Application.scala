package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("请输入验证码", true))
  }

  def update = Action { request =>
    if (1==2) {
      Ok(views.html.index("验证成功", true))
    } else {
      if (1==2) {
        Ok(views.html.index("验证失败. 验证码不存在", false))
      } else {
        Ok(views.html.index("验证失败。验证吗已使用", false))
      }
    }
  }

}