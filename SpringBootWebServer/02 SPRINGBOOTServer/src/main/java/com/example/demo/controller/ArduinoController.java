package com.example.demo.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller  // 이 클래스가 Spring MVC 의 컨트롤러임을 나타낸다
@Slf4j  // Lombok 의 @Slf4j 어노테이션, 로깅
@RequestMapping("/arduino")  // 이 클래스의 모든 요청 매핑은 "/arduino" 로 시작
public class ArduinoController {  // ArduinoController 클래스 정의 시작

    @GetMapping("/index") // Http Get 요청을 "/arduino/index" 경로와 매핑 
    public void index(){
        log.info("GET/arduino/index");
    }  // index 메서드 정의, 반환 타입은 void, 로그에 메시지 출력
}
