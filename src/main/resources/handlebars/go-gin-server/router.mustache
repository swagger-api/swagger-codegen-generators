package main

import (
	"github.com/gin-gonic/gin"
)

type Router struct {
	Path    string
	Method  string
	Handler gin.HandlerFunc
}

var AllRouter = []Router{
	{"/user", "GET", handlers.handler_User},
}

func setupRouter() *gin.Engine {
	router := gin.Default()

	for _, r := range AllRouter {
		router.Handle(r.Method, r.Path, r.Handler)
	}
	router = registerCustomRouter(router)
	return router
}
