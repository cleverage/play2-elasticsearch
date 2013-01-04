package com.github.cleverage.elasticsearch.controllers

import play.api.mvc.{Action, Controller}

/**
 * FakeController needed to make "publish-local" work correctly 
 * see : https://play.lighthouseapp.com/projects/82401-play-20/tickets/898
 *
 * Can be removed when this issue will be fixed
 * 
 */
object FakeController extends Controller {

  def index = Action {
    Ok("Ok")
  }
}
