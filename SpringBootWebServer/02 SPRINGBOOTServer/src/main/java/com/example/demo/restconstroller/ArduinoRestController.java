package com.example.demo.restconstroller;


import com.fazecast.jSerialComm.SerialPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.*;

@RestController  // 이 클래스가 RESTful 웹 서비스의 컨트롤러임을 나타낸다
@Slf4j  // Lombok 의 로깅 기능 사용
@RequestMapping("/arduino")  // 이 클래스의 모든 요청 매핑은 "/arduino" 로 시작한다
public class ArduinoRestController {

    private SerialPort serialPort;  // 시리얼 포트 객체
    private OutputStream outputStream;  // 출력 스트림 객체
    private InputStream inputStream;  // 입력 스트림 객체

    private String LedLog;  // LED 로그 문자열
    private String TmpLog;  // 온도 로그 문자열
    private String LightLog;  // 조도 로그 문자열
    private String DistanceLog;  // 거리 로그 문자열

    @GetMapping("/connection/{COM}")  // GET 요청을 "/arduino/connection/{COM}" 에 매핑
    public ResponseEntity<String> setConnection(@PathVariable("COM") String COM, HttpServletRequest request) {
        log.info("GET /arduino/connection " + COM + " IP: " + request.getRemoteAddr());  // 로그에 요청 정보 기록

        if (serialPort != null) {  // 시리얼 포트 객체에 데이터가 존재한다면
            serialPort.closePort();  // 기존 시리얼 포트 닫기
            serialPort = null;  // 시리얼 포트 객체 초기화
        }

        //Port Setting
        serialPort = SerialPort.getCommPort(COM);  // 지정된 COM 포트로 시리얼 포트 객체 생성

        //Connection Setting
        serialPort.setBaudRate(9600);  // 보드레이트 설정
        serialPort.setNumDataBits(8);  // 데이터 비트 수 설정
        serialPort.setNumStopBits(0);  // 정비 비트 수 설정
        serialPort.setParity(0);  // 패리티 비트 설정
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING,2000,0);  // 타임아웃 설정

        boolean isOpen = serialPort.openPort();  // 포트 열기
        log.info("isOpen ? " + isOpen);  // 포트 열기 성공 여부 로그 출력


        if (isOpen) {  // 포트 열기에 성공했다면
            this.outputStream = serialPort.getOutputStream();  // 출력 스트림 설정
            this.inputStream = serialPort.getInputStream();  // 입력 스트림 설정

            //----------------------------------------------------------------
            //수신 스레드 동작
            //----------------------------------------------------------------
            Worker worker = new Worker();  // 수신 스레드 객체 생성
            Thread th = new Thread(worker);  // 스레드 객체 생성
            th.start();  // 스레드 시작


            return new ResponseEntity("Connection Success!", HttpStatus.OK);  // 연결 성공 응답 반환
        } else {
            return new ResponseEntity("Connection Fail...!", HttpStatus.BAD_GATEWAY);  // 연결 실패 응답 반환
        }


    }

    @GetMapping("/led/{value}")  // GET 요청을 "/arduino/led/{value}" 에 매핑
    public void led_Control(@PathVariable String value, HttpServletRequest request) throws IOException {
        log.info("GET /arduino/led/value : " + value + " IP : " + request.getRemoteAddr());  // 로그에 요청 정보 기록
        if (serialPort.isOpen()) {  // 시리얼 포트가 열려있는 경우
            outputStream.write(value.getBytes());  // LED 제어 명령 전송
            outputStream.flush();  // 출력 스트림 비우기
        }
    }

    @GetMapping("/message/led")  // GET 요청을 "/arduino/message/led" 에 매핑
    public String led_Message() throws InterruptedException {
        return LedLog;  // LED 로그 반환
    }

    @GetMapping("/message/tmp")  // GET 요청을 "/arduino/message/tmp" 에 매핑
    public String tmp_Message() {
        return TmpLog;
    }  // 온도 로그 반환

    @GetMapping("/message/light")  // GET 요청을 "/arduino/message/light" 에 매핑
    public String light_Message() {
        return LightLog;
    }  // 조도 로그 반환

    @GetMapping("/message/distance")  // GET 요청을 "/arduino/message/distance" 에 매핑
    public String distance_Message() {
        return DistanceLog;
    }  // 거리 로그 반환




    //----------------------------------------------------------------
    // 수신 스레드 클래스
    //----------------------------------------------------------------
    class Worker implements Runnable {
        DataInputStream din;  // 데이터 입력 스트림 객체
        @Override
        public void run() {
            din = new DataInputStream(inputStream);  // 데이터 입력 스트림 초기화
            try{
                while(!Thread.interrupted()) {  // 스레드가 인터럽트되지 않은 동안 반복
                    if (din != null) {  // 데이터 입력 스트림이 null 이 아닌 경우
                        String data = din.readLine();  // 한 줄 읽기
                        System.out.println("[DATA] : " + data);  // 데이터 출력
                        String[] arr = data.split("_"); //LED ,TMP , LIGHT , DIS // 3,4     데이터를 구분자로 분할
                        try {
                            if (arr.length > 3) {  // 배열 길이가 3 보다 큰 경우
                                LedLog = arr[0];  // 첫 번째 요소를 LED 로그로 설정
                                TmpLog = arr[1];  // 두 번째 요소를 온도 로그로 설정
                                LightLog = arr[2];  // 세 번째 요소를 조도 로그로 설정
                                DistanceLog = arr[3];  // 네 번째 요소를 거리 로그로 설정
                            } else {  // 배열 길이가 3 이하인 경우
                                TmpLog = arr[0];  // 첫 번째 요소를 온도 로그로 설정
                                LightLog = arr[1];  // 두 번째 요소를 조도 로그로 설정
                                DistanceLog = arr[2];  // 세 번째 요소를 거리 로그로 설정
                            }
                        }catch(ArrayIndexOutOfBoundsException e1){e1.printStackTrace();}  // 배열 인덱스 초과 예외 처리
//                        if(data.startsWith("LED:")){
//                            LedLog = data;
//                        }else if(data.startsWith("TMP:")) {
//                            TmpLog = data;
//                        }else if(data.startsWith("LIGHT:")){
//                            LightLog = data;
//                        }else if(data.startsWith("DIS:")){
//                            DistanceLog = data;
//                        }




                    }
                    Thread.sleep(200);  // 200 밀리초 대기
                }
            }catch(Exception e){
                e.printStackTrace();  // 예외 처리
            }


        }
    }
}