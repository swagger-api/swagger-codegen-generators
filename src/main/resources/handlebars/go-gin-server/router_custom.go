package main

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

func registerCustomRouter(router *gin.Engine) *gin.Engine {

	// add you extra custom router.
	router.Handle("GET", "/demo", handler_demo)

	return router
}

func handler_demo(c *gin.Context) {
	c.String(http.StatusOK, "Custom Handler, you can copy this file and modify it to add you extra route.")
}
