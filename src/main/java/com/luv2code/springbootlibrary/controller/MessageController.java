package com.luv2code.springbootlibrary.controller;

import com.luv2code.springbootlibrary.entity.Message;
import com.luv2code.springbootlibrary.requestmodels.AdminQuestionRequest;
import com.luv2code.springbootlibrary.service.MessageService;
import com.luv2code.springbootlibrary.utils.ExtractJWT;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("https://localhost:3000")
@RestController
@RequestMapping("api/messages")
public class MessageController {

    private MessageService messageService;

    public MessageController(MessageService messageService){
        this.messageService = messageService;
    }

    @PostMapping("/secure/add/message")
    public void postMessage(@RequestHeader(value = "Authorization")String token,
                            @RequestBody Message messageRequest){
        String userEmail = ExtractJWT.payloadJWTExtraction(token);
        messageService.postMessage(messageRequest,userEmail);
    }
    @PutMapping("/secure/admin/message")
    public void putMessage(@RequestHeader(value = "Authorization")String token,
                           @RequestBody AdminQuestionRequest adminQuestionRequest) throws Exception{
        String userEmail = ExtractJWT.payloadJWTExtraction(token);
        String admin = ExtractJWT.extractUserType(token);
        if(admin == null || !admin.equals("admin")){
            throw new Exception("Admins Only!");
        }
        messageService.putMessage(adminQuestionRequest,userEmail);
    }
}
