package umc.teumteum.server.domain.home.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import umc.teumteum.server.domain.home.dto.request.TodoRequestDto;
import umc.teumteum.server.domain.home.dto.response.HomeResponseDto;
import umc.teumteum.server.domain.home.dto.response.TodoIdResponseDto;
import umc.teumteum.server.domain.home.dto.response.TodoInfoResponseDto;
import umc.teumteum.server.domain.home.dto.response.TodayScheduleResponseDto;
import umc.teumteum.server.domain.home.exception.status.HomeSuccessStatus;
import umc.teumteum.server.domain.home.service.HomeService;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.annotation.CurrentUser;
import umc.teumteum.server.global.apiPayload.ApiResponse;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
@Tag(name = "Home", description = "Home 관련 API")
public class HomeController {

    private final HomeService homeService;

    @GetMapping(value = "/teum-time", produces = "application/json")
    @Operation(summary = "빈틈 시간 조회 API",description = "지금까지 채운 빈틈 시간 조회 API입니다.")
    public ApiResponse<HomeResponseDto.TeumTimeDto> getTeumTime(
            @CurrentUser @Parameter(hidden = true) User user){
        HomeResponseDto.TeumTimeDto response = homeService.getTeaumTime(user);
        return ApiResponse.of(HomeSuccessStatus._TEUMTIME_LOADED, response);
    }

    @GetMapping(value = "/teum", produces = "application/json")
    @Operation(summary = "오늘의 빈틈 조회 API",description = "오늘의 빈틈 시간을 조회하는 API입니다. query string으로 오늘 날짜를 입력해주세요.")
    public ApiResponse<List<TodayScheduleResponseDto>> getTodayTeum(
            @Parameter(name = "date", description = "조회할 날짜", example = "2025-07-24") @RequestParam("date") LocalDate date,
            @CurrentUser @Parameter(hidden = true) User user){
        List<TodayScheduleResponseDto> response = homeService.getTodaySchedule(date,user);
        return ApiResponse.of(HomeSuccessStatus._TODAY_SCHEDULE,response);
    }

    @GetMapping(value = "/calendar", produces = "application/json")
    @Operation(summary = "캘린더 조회 API",description = "오늘의 빈틈 시간을 조회하는 API입니다. query string으로 시작날짜와 종료날찌를 입력주세요.")
    public ApiResponse<List<HomeResponseDto.CalendarDto>> getCalendar(
            @Parameter(name= "startDate", description = "시작날짜", example = "2025-07-01") @RequestParam("startDate") LocalDate startDate,
            @Parameter(name= "endDate", description = "종료날짜", example = "2025-07-31") @RequestParam("endDate") LocalDate endDate,
            @CurrentUser @Parameter(hidden = true) User user){
        List<HomeResponseDto.CalendarDto> response = homeService.getCalendar(startDate,endDate,user);
        return ApiResponse.of(HomeSuccessStatus._CALENDAR_LOADED,response);
    }

    @GetMapping(value = "/todolist", produces = "application/json")
    @Operation(summary = "투두리스트 조회 API",description = "특정날찌의 투두를 조회하는 API입니다. query string으로 날짜를 입력주세요.")
    public ApiResponse<List<HomeResponseDto.TodolistDto>> getTodolist(
            @Parameter(name= "date", description = "날짜", example = "2025-07-31") @RequestParam("date") LocalDate date,
            @CurrentUser @Parameter(hidden = true) User user){
        List<HomeResponseDto.TodolistDto> response = homeService.getTodolist(date,user);
        return ApiResponse.of(HomeSuccessStatus._TODOLIST_LOADED,response);
    }

    @GetMapping(value = "/user-reminds", produces = "application/json")
    @Operation(summary = "리마인드 알림 정보 조회 API",description = "유저의 리마인드 알림 설정 정보를 조회하는 API입니다")
    public ApiResponse<HomeResponseDto.ReminderDto> getUserRemind(
            @CurrentUser @Parameter(hidden = true) User user
    ){
        HomeResponseDto.ReminderDto response = homeService.getUserRemind(user);
        return ApiResponse.of(HomeSuccessStatus._REMINDER_LOADED,response);
    }

    @PostMapping(value = "/todo",consumes = "application/json", produces = "application/json")
    @Operation(summary = "투두 등록 API",description = "새로운 투두를 등록 API입니다.")
    public ApiResponse<TodoIdResponseDto> createTodo(
            @RequestBody @Valid TodoRequestDto request,
            @CurrentUser @Parameter(hidden = true) User user){
        TodoIdResponseDto response = homeService.createTodo(request, user);
        return ApiResponse.of(HomeSuccessStatus._TODO_CREATED,response);
    }

    @GetMapping(value = "/todo/{todoId}", produces = "application/json")
    @Operation(summary = "특정 투두 정보 조회 API",description = "특정투두의 상세정보를 조회하는 API입니다. path variable로 투두ID를 입력주세요.")
    public ApiResponse<TodoInfoResponseDto> getTodo(
            @Parameter(name= "todoId", description = "조회할 todo ID", example = "123") @PathVariable("todoId") Long todoId){
        TodoInfoResponseDto response = homeService.getTodoInfo(todoId);
        return ApiResponse.of(HomeSuccessStatus._TODO_LOADED,response);
    }

    @PutMapping(value = "/todo/{todoId}",consumes = "application/json", produces = "application/json")
    @Operation(summary = "특정 투두 정보 수정 API",description = "특정투두의 상세정보를 수정하는 API입니다. path variable로 투두ID를 입력주세요.")
    public ApiResponse<TodoIdResponseDto> updateTodo(
            @Parameter(name= "todoId", description = "수정할 todo ID", example = "123") @PathVariable("todoId") Long todoId,
            @RequestBody @Valid TodoRequestDto request,
            @CurrentUser @Parameter(hidden = true) User user){
        TodoIdResponseDto response = homeService.updateTodoInfo(request,todoId,user);
        return ApiResponse.of(HomeSuccessStatus._TODO_UPDATED,response);
    }

    @DeleteMapping(value = "/todo/{todoId}")
    @Operation(summary = "특정 투두 삭제 API",description = "특정투두의 삭제하는 API입니다. path variable로 투두ID를 입력주세요.")
    public ApiResponse<String> deleteTodo(
            @Parameter(name= "todoId", description = "삭제할 todo ID", example = "123") @PathVariable("todoId") Long todoId){
        homeService.deleteTodo(todoId);
        return  ApiResponse.of(HomeSuccessStatus._TODO_DELETED,null);
    }
}
