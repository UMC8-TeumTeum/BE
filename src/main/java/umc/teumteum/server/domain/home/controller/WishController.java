package umc.teumteum.server.domain.home.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import umc.teumteum.server.domain.home.dto.request.WishAssignRequestDto;
import umc.teumteum.server.domain.home.dto.request.WishDeleteRequestDto;
import umc.teumteum.server.domain.home.dto.request.WishRequestDto;
import umc.teumteum.server.domain.home.dto.response.WishResponseDto;
import umc.teumteum.server.domain.home.exception.status.HomeSuccessStatus;
import umc.teumteum.server.domain.home.service.HomeService;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.annotation.CurrentUser;
import umc.teumteum.server.global.apiPayload.ApiResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishes")
@Tag(name = "wish", description = "위시 관련 API")
public class WishController {

    private final HomeService homeService;

    @GetMapping(value = "/wishlist", produces = "application/json")
    @Operation(summary = "위시리스트 정보 조회 API",description = "위시 목록을 조회하는 API입니다. query string으로 조회 기간과 page 번호를 입력주세요.")
    public ApiResponse<WishResponseDto.WishlistDto> getWishlist(
            @Parameter(name= "duration", description = "조회기간 (all | 10m | 20m | 30m | 1h)", example = "30m") @RequestParam("duration") String duration,
            @Parameter(name= "page", description = "페이지 번호는 1부터 시작", example = "1") @RequestParam("page") Integer page,
            @CurrentUser @Parameter(hidden = true) User user){

        WishResponseDto.WishlistDto response = homeService.getWishlist(duration, page, user);
        return ApiResponse.of(HomeSuccessStatus._WISHLIST_LOADED, response);
    }

    @PostMapping(value = "", consumes = "application/json", produces = "application/json")
    @Operation(summary = "위시 등록 API",description = "새로운 위시를 생성하는 API입니다.")
    public ApiResponse<String> createWish(
            @RequestBody @Valid WishRequestDto.CreateDto request,
            @CurrentUser @Parameter(hidden = true) User user
            ){
        homeService.createWish(request,user);
        return ApiResponse.of(HomeSuccessStatus._WISH_CREATED, null);
    }

    @GetMapping(value = "/{wishId}", produces = "application/json")
    @Operation(summary = "특정 위시 정보 조회 API",description = "특정위시의 상세정보를 조회하는 API입니다.")
    public ApiResponse<WishResponseDto.WishInfoDto> getWish(
            @Parameter(name= "wishId", description = "조회할 위시 ID", example = "123") @PathVariable("wishId") Long wishId){
        WishResponseDto.WishInfoDto response = homeService.getWishInfo(wishId);
        return ApiResponse.of(HomeSuccessStatus._WISH_LOADED,response);
    }

    @PatchMapping(value = "/{wishId}", consumes = "application/json", produces = "application/json")
    @Operation(summary = "특정 위시 정보 수정 API",description = "특정위시의 상세정보를 수정하는 API입니다.")
    public ApiResponse<String> updateWish(
            @Parameter(name= "wishId", description = "수정할 위시 ID", example = "123") @PathVariable("wishId") Long wishId,
            @RequestBody @Valid WishRequestDto.CreateDto request){
        homeService.updateWishInfo(request, wishId);
        return ApiResponse.of(HomeSuccessStatus._WISH_UPDATED, null);
    }

    @GetMapping(value = "/categories", produces = "application/json")
    @Operation(summary = "카테고리 정보 조회 API",description = "위시의 카테고리 정보를 조회하는 API입니다.")
    public ApiResponse<List<WishResponseDto.CategoryDto>> getCategory(){
        List<WishResponseDto.CategoryDto> response = homeService.getCategory();
        return ApiResponse.of(HomeSuccessStatus._CATEGORY_LOADED,response);
    }

    @DeleteMapping(value = "", consumes = "application/json")
    @Operation(summary = "위시 삭제 API",description = "위시를 삭제하는 API입니다.")
    public ApiResponse<String> deleteWish(
            @RequestBody @Valid WishRequestDto.WishDeleteDto request
    ){
        homeService.deleteWishByIds(request);
        return ApiResponse.of(HomeSuccessStatus._WISH_DELETED, null);
    }

    @GetMapping(value = "/teum", produces = "application/json")
    @Operation(summary = "위시 등록 가능 시간 조회 API",description = "위시리스트를 틈으로 등록가능한 시간을 조회하는 API입니다. query string으로 am 또는 pm을 입력헤주세요")
    public ApiResponse<String> getEnableTimes(
            @Parameter(name = "period", description = "조회 시간대 (오전: am, 오후: pm)", example = "am") @RequestParam("period") String period){
        return ApiResponse.onSuccess(null);
    }

    @PostMapping(value = "/{wishId}/assign", consumes = "application/json", produces = "application/json")
    @Operation(summary = "위시 투두 등록 API",description = "위시를 투두로 등록하는 API입니다.")
    public ApiResponse<String> assignWish(
            @Parameter(name= "wishId", description = "투두로 등록할 위시 ID", example = "123") @PathVariable("wishId") Long wishId,
            @RequestBody @Valid WishRequestDto.WishAssignDto request,
            @CurrentUser @Parameter(hidden = true) User user){
        homeService.assignWish(wishId, request, user);
        return ApiResponse.of(HomeSuccessStatus._WISH_ASSIGNED,null);
    }

}
