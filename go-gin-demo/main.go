package main

import (
	"io"
	"net/http"
	"os"
	"time"

	"github.com/gin-gonic/gin"
)

func main() {

	// 禁用控制台颜色，将日志写入文件时不需要控制台颜色。
	//gin.DisableConsoleColor()

	// 记录到文件。
	f, _ := os.Create("http.log")
	gin.DefaultWriter = io.MultiWriter(f, os.Stdout)

	router := setupRouter()
	s := &http.Server{
		Addr:         ":8080",
		Handler:      router,
		ReadTimeout:  60 * time.Second,
		WriteTimeout: 60 * time.Second,
	}
	s.ListenAndServe()
}
