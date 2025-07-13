package umc.teumteum.server.domain.home.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @GetMapping(value = "/teum-time", produces = "application/json")
    @Operation(summary = "빈틈 시간 조회 API",description = "지금까지 채운 빈틈 시간 조회 API입니다.")
    public ApiResponse<String> getTeumTime(){
        return ApiResponse.onSuccess(null);
    }

    @GetMapping(value = "/today", produces = "application/json")
    @Operation(summary = "오늘의 빈틈 조회 API",description = "오늘의 빈틈 시간을 조회하는 API입니다. query string으로 am 또는 pm을 입력주세요.")
    public ApiResponse<TodyTeumResponseDTO> getTodayTeum(
            @Parameter(name = "period", description = "조회 시간대 (오전: am, 오후: pm)", example = "am")
            @RequestParam("period") String period){
        return ApiResponse.onSuccess(null);
    }

    @GetMapping(value = "/calendar", produces = "application/json")
    @Operation(summary = "캘린더 조회 API",description = "오늘의 빈틈 시간을 조회하는 API입니다. query string으로 시작날짜와 종료날찌를 입력주세요.")
    public ApiResponse<String> getCalendar(
            @Parameter(name= "startDate", description = "시작날짜", example = "2025-07-01") @RequestParam("startDate") LocalDate startDate,
            @Parameter(name= "endDate", description = "종료날짜", example = "2025-07-31") @RequestParam("endDate") LocalDate endDate){
        return ApiResponse.onSuccess(null);
    }

    @GetMapping(value = "/todolist", produces = "application/json")
    @Operation(summary = "투두리스트 조회 API",description = "특정날찌의 투두를 조회하는 API입니다. query string으로 날짜를 입력주세요.")
    public ApiResponse<String> getTodolist(
            @Parameter(name= "date", description = "날짜", example = "2025-07-31") @RequestParam("date") LocalDate date){
        return ApiResponse.onSuccess(null);
    }

    @GetMapping(value = "/user-reminds", produces = "application/json")
    @Operation(summary = "리마인드 알림 정보 조회 API",description = "유저의 리마인드 알림 설정 정보를 조회하는 API입니다")
    public ApiResponse<String> getUserRemind(){
        return ApiResponse.onSuccess(null);
    }

    @PostMapping(value = "/todo",consumes = "application/json", produces = "application/json")
    @Operation(summary = "투두 등록 API",description = "새로운 투두를 등록 API입니다.")
    public ApiResponse<String> createTodo(){
        return ApiResponse.onSuccess(null);
    }

    @GetMapping(value = "/todo/{todoId}", produces = "application/json")
    @Operation(summary = "특정 투두 정보 조회 API",description = "특정투두의 상세정보를 조회하는 API입니다. path variable로 투두ID를 입력주세요.")
    public ApiResponse<String> getTodo(
            @Parameter(name= "todoId", description = "조회할 todo ID", example = "123") @PathVariable("todoId") Long todoId){
        return ApiResponse.onSuccess(null);
    }

    @PatchMapping(value = "/todo/{todoId}",consumes = "application/json", produces = "application/json")
    @Operation(summary = "특정 투두 정보 수정 API",description = "특정투두의 상세정보를 수정하는 API입니다. path variable로 투두ID를 입력주세요.")
    public ApiResponse<String> updateTodo(
            @Parameter(name= "todoId", description = "수정할 todo ID", example = "123") @PathVariable("todoId") Long todoId){
        return ApiResponse.onSuccess(null);
    }

    @DeleteMapping(value = "/todo/{todoId}")
    @Operation(summary = "특정 투두 삭제 API",description = "특정투두의 삭제하는 API입니다. path variable로 투두ID를 입력주세요.")
    public ApiResponse<String> deleteTodo(
            @Parameter(name= "todoId", description = "삭제할 todo ID", example = "123") @PathVariable("todoId") Long todoId){
        return ApiResponse.onSuccess(null);
    }
}
