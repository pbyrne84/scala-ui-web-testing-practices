package pbyrne84.browsertesting.routes

class SiteUrls(baseUrl: String) {

  object path {
    val homePage: String = "/"
  }

  object fullPath {
    val homePage: String = baseUrl + path.homePage
  }

}
