package handlers

import (
	"fmt"

	"dummy.swagger.io/models"
)

func handler_User_impl(u *models.UserReq) (*models.UserResp, error) {
	return &models.UserResp{Msg: fmt.Sprintf("Hello, %s", u.Name)}, nil
}
