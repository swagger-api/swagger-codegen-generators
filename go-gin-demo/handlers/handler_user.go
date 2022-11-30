package handlers

import (
	"net/http"

	"dummy.swagger.io/models"
	"github.com/gin-gonic/gin"
)

func handler_User(c *gin.Context) {
	var user models.UserReq
	var resp *models.UserResp
	var err error
	err = c.ShouldBind(&user)
	if err == nil {

		resp, err = handler_User_impl(&user)
		if err != nil {

			c.AbortWithStatusJSON(http.StatusExpectationFailed, models.CommonResp{http.StatusExpectationFailed, err.Error()})
		}
	} else {
		c.AbortWithStatusJSON(http.StatusExpectationFailed, models.CommonResp{http.StatusExpectationFailed, err.Error()})
	}

	c.JSON(http.StatusOK, resp)
}
