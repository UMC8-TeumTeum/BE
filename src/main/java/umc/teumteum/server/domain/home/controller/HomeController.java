package umc.teumteum.server.domain.home.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import umc.teumteum.server.domain.home.dto.TodyTeumResponseDTO;
import umc.teumteum.server.global.apiPayload.ApiResponse;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
@Tag(name = "home", description = "home domain API")
public class HomeController {

    @GetMapping("/teum-time")
    @Operation(summary = "빈틈 시간 조회 API",description = "지금까지 채운 빈틈 시간 조회 API입니다.",security = { @SecurityRequirement(name = "BearerAuth")})
    public ApiResponse<String> getTuemTime(){
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/today")
    @Operation(summary = "오늘의 빈틈 조회 API",description = "오늘의 빈틈 시간을 조회하는 API입니다. query string으로 am 또는 pm을 입력주세요.",security = { @SecurityRequirement(name = "BearerAuth")})
    public ApiResponse<TodyTeumResponseDTO> getTodayTeum(@RequestParam("period") String period){
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/calendar")
    @Operation(summary = "캘린더 조회 API",description = "오늘의 빈틈 시간을 조회하는 API입니다. query string으로 시작날짜와 종료날찌를 입력주세요.",security = { @SecurityRequirement(name = "BearerAuth")})
    public ApiResponse<String> getCalendar(@RequestParam("startDate") LocalDate startDate, @RequestParam("endDate") LocalDate endDate){
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/todolist")
    @Operation(summary = "투두리스트 조회 API",description = "특정날찌의 투두를 조회하는 API입니다. query string으로 날짜를 입력주세요.",security = { @SecurityRequirement(name = "BearerAuth")})
    public ApiResponse<String> getTodolist(@RequestParam("startDate") LocalDate date){
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/user-reminds")
    @Operation(summary = "리마인드 알림 정보 조회 API",description = "유저의 리마인드 알림 설정 정보를 조회하는 API입니다",security = { @SecurityRequirement(name = "BearerAuth")})
    public ApiResponse<String> getUserRemindI(){
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/todo")
    @Operation(summary = "투두 등록 API",description = "새로운 투두를 업로드하는 API입니다.",security = { @SecurityRequirement(name = "BearerAuth")})
    public ApiResponse<String> createTodo(){
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/todo")
    @Operation(summary = "특정 투두 정보 조회 API",description = "특정투두의 상세정보를 조회하는 API입니다. query string으로 투두ID를 입력주세요.",security = { @SecurityRequirement(name = "BearerAuth")})
    public ApiResponse<String> getTodo(@RequestParam("todoId") Long todoId){
        return ApiResponse.onSuccess(null);
    }

    @PatchMapping("/todo")
    @Operation(summary = "특정 투두 정보 조회 API",description = "특정투두의 상세정보를 조회하는 API입니다. query string으로 투두ID를 입력주세요.",security = { @SecurityRequirement(name = "BearerAuth")})
    public ApiResponse<String> updateTodo(@RequestParam("todoId") Long todoId){
        return ApiResponse.onSuccess(null);
    }

    @DeleteMapping("/todo")
    @Operation(summary = "특정 투두 삭제 API",description = "특정투두의 삭제하는 API입니다. query string으로 투두ID를 입력주세요.",security = { @SecurityRequirement(name = "BearerAuth")})
    public ApiResponse<String> deleteTodo(@RequestParam("todoId") Long todoId){
        return ApiResponse.onSuccess(null);
    }
}
