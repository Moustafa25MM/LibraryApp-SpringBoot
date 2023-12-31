package com.luv2code.springbootlibrary.controller;

import com.luv2code.springbootlibrary.requestmodels.AddBookRequest;
import com.luv2code.springbootlibrary.service.AdminService;
import com.luv2code.springbootlibrary.utils.ExtractJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("https://localhost:3000")
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private AdminService adminService;

    @Autowired
    private AdminController(AdminService adminService){
        this.adminService = adminService;
    }
    @PostMapping("/secure/add/book")
    public void postBook(@RequestHeader(value = "Authorization")String token,
                         @RequestBody AddBookRequest addBookRequest)throws Exception{
        String admin = ExtractJWT.extractUserType(token);
        if(admin == null || !admin.equals("admin")){
            throw new Exception("Admins Only!");
        }
        adminService.postBook(addBookRequest);
    }
    @DeleteMapping("/secure/delete/book")
    public void deleteBook(@RequestHeader(value = "Authorization")String token,
                           @RequestParam Long bookId) throws Exception {
        String admin = ExtractJWT.extractUserType(token);
        if(admin == null || !admin.equals("admin")){
            throw new Exception("Admins Only!");
        }
        adminService.deleteBook(bookId);
    }

    @PutMapping("/secure/increase/book/quantity")
    public void increaseBookQuantity(@RequestHeader(value = "Authorization")String token,
                                     @RequestParam Long bookId) throws Exception {
        String admin = ExtractJWT.extractUserType(token);
        if(admin == null || !admin.equals("admin")){
            throw new Exception("Admins Only!");
        }
        adminService.increaseBookQuantity(bookId);
    }
    @PutMapping("/secure/decrease/book/quantity")
    public void decreaseBookQuantity(@RequestHeader(value = "Authorization")String token,
                                     @RequestParam Long bookId) throws Exception {
        String admin = ExtractJWT.extractUserType(token);
        if(admin == null || !admin.equals("admin")){
            throw new Exception("Admins Only!");
        }
        adminService.decreaseBookQuantity(bookId);
    }
}
