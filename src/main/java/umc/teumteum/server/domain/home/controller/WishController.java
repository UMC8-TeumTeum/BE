package umc.teumteum.server.domain.home.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import umc.teumteum.server.domain.home.dto.request.WishAssignRequestDto;
import umc.teumteum.server.domain.home.dto.request.WishDeleteRequestDto;
import umc.teumteum.server.domain.home.dto.response.CategoryResponseDto;
import umc.teumteum.server.domain.home.dto.response.WishInfoResponseDto;
import umc.teumteum.server.domain.home.dto.request.WishRequestDto;
import umc.teumteum.server.domain.home.exception.status.HomeSuccessStatus;
import umc.teumteum.server.domain.home.service.HomeService;
import umc.teumteum.server.domain.home.dto.response.WishlistResponseDto;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.annotation.CurrentUser;
import umc.teumteum.server.global.apiPayload.ApiResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishes")
@Tag(name = "Wish", description = "위시 관련 API")
public class WishController {

    private final HomeService homeService;

    @GetMapping(value = "/wishlist", produces = "application/json")
    @Operation(summary = "위시리스트 정보 조회 API",description = "위시 목록을 조회하는 API입니다. query string으로 조회 기간과 page 번호를 입력주세요.")
    public ApiResponse<WishlistResponseDto> getWishlist(
            @Parameter(name= "duration", description = "조회기간 (all | 10m | 20m | 30m | 1h)", example = "30m") @RequestParam("duration") String duration,
            @Parameter(name= "page", description = "페이지 번호는 1부터 시작", example = "1") @RequestParam("page") Integer page,
            @CurrentUser @Parameter(hidden = true) User user){

        WishlistResponseDto response = homeService.getWishlist(duration, page, user);
        return ApiResponse.of(HomeSuccessStatus._WISHLIST_LOADED, response);
    }

    @PostMapping(value = "", consumes = "application/json", produces = "application/json")
    @Operation(summary = "위시 등록 API",description = "새로운 위시를 생성하는 API입니다.")
    public ApiResponse<String> createWish(
            @RequestBody @Valid WishRequestDto request,
            @CurrentUser @Parameter(hidden = true) User user
            ){
        homeService.createWish(request,user);
        return ApiResponse.of(HomeSuccessStatus._WISH_CREATED, null);
    }

    @GetMapping(value = "/{wishId}", produces = "application/json")
    @Operation(summary = "특정 위시 정보 조회 API",description = "특정위시의 상세정보를 조회하는 API입니다.")
    public ApiResponse<WishInfoResponseDto> getWish(
            @Parameter(name= "wishId", description = "조회할 위시 ID", example = "123") @PathVariable("wishId") Long wishId){
        WishInfoResponseDto response = homeService.getWishInfo(wishId);
        return ApiResponse.of(HomeSuccessStatus._WISH_LOADED,response);
    }

    @PatchMapping(value = "/{wishId}", consumes = "application/json", produces = "application/json")
    @Operation(summary = "특정 위시 정보 수정 API",description = "특정위시의 상세정보를 수정하는 API입니다.")
    public ApiResponse<String> updateWish(
            @Parameter(name= "wishId", description = "수정할 위시 ID", example = "123") @PathVariable("wishId") Long wishId,
            @RequestBody @Valid WishRequestDto request,
            @CurrentUser @Parameter(hidden = true) User user){
        homeService.updateWishInfo(request, wishId, user);
        return ApiResponse.of(HomeSuccessStatus._WISH_UPDATED, null);
    }

    @GetMapping(value = "/categories", produces = "application/json")
    @Operation(summary = "카테고리 정보 조회 API",description = "위시의 카테고리 정보를 조회하는 API입니다.")
    public ApiResponse<List<CategoryResponseDto>> getCategory(){
        List<CategoryResponseDto> response = homeService.getCategory();
        return ApiResponse.of(HomeSuccessStatus._CATEGORY_LOADED,response);
    }

    @DeleteMapping(value = "", consumes = "application/json")
    @Operation(summary = "위시 삭제 API",description = "위시를 삭제하는 API입니다.")
    public ApiResponse<String> deleteWish(
            @RequestBody @Valid WishDeleteRequestDto request
    ){
        homeService.deleteWishByIds(request);
        return ApiResponse.of(HomeSuccessStatus._WISH_DELETED, null);
    }

    @PostMapping(value = "/{wishId}/assign", consumes = "application/json", produces = "application/json")
    @Operation(summary = "위시 투두 등록 API",description = "위시를 투두로 등록하는 API입니다.")
    public ApiResponse<String> assignWish(
            @Parameter(name= "wishId", description = "투두로 등록할 위시 ID", example = "123") @PathVariable("wishId") Long wishId,
            @RequestBody @Valid WishAssignRequestDto request,
            @CurrentUser @Parameter(hidden = true) User user){
        homeService.assignWish(wishId, request, user);
        return ApiResponse.of(HomeSuccessStatus._WISH_ASSIGNED,null);
    }

}
